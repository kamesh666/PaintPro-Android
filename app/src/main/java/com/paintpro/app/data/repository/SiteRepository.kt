package com.paintpro.app.data.repository

import android.content.Context
import com.paintpro.app.data.local.AppDatabase
import com.paintpro.app.data.local.entity.SiteEntity
import com.paintpro.app.data.remote.SupabaseProvider
import com.paintpro.app.data.remote.dto.SiteDto
import com.paintpro.app.data.remote.dto.toDto
import com.paintpro.app.data.remote.dto.toEntity
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Offline-first: every write lands in Room immediately (marked pending) and is then pushed to
 * Supabase best-effort. If the push fails (no network, server error) the row simply stays
 * pending and [syncPending] will retry it later - nothing typed while offline is lost.
 */
class SiteRepository(private val context: Context) {
    private val client get() = SupabaseProvider.getClient(context)
    private val dao = AppDatabase.getInstance(context).siteDao()

    fun observeSites(orgId: String): Flow<List<SiteEntity>> = dao.observeSites(orgId)

    fun observeSite(id: String): Flow<SiteEntity?> = dao.observeById(id)

    suspend fun saveSite(site: SiteEntity) = withContext(Dispatchers.IO) {
        val pending = site.copy(syncState = SyncState.PENDING_UPSERT)
        dao.upsert(pending)
        pushSite(pending)
    }

    suspend fun deleteSite(id: String) = withContext(Dispatchers.IO) {
        val existing = dao.getById(id) ?: return@withContext
        val deleted = existing.copy(isDeleted = true, syncState = SyncState.PENDING_DELETE)
        dao.upsert(deleted)
        pushSite(deleted)
    }

    /** Retries every locally-pending site - call this whenever connectivity is likely back. */
    suspend fun syncPending() = withContext(Dispatchers.IO) {
        dao.getPendingSync().forEach { pushSite(it) }
    }

    /** Pulls the latest rows for [orgId] down from Supabase and merges them into Room. */
    suspend fun refreshFromRemote(orgId: String) = withContext(Dispatchers.IO) {
        runCatching {
            val remote = client.postgrest.from("sites")
                .select { filter { eq("org_id", orgId) } }
                .decodeList<SiteDto>()
            dao.upsertAll(remote.map { it.toEntity() })
        }
    }

    private suspend fun pushSite(site: SiteEntity) {
        runCatching {
            if (site.syncState == SyncState.PENDING_DELETE) {
                client.postgrest.from("sites").delete { filter { eq("id", site.id) } }
                dao.hardDelete(site.id)
            } else {
                client.postgrest.from("sites").upsert(site.toDto())
                dao.markSynced(site.id)
            }
        }
    }
}
