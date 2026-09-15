package com.paintpro.app.data.repository

import android.content.Context
import com.paintpro.app.data.local.AppDatabase
import com.paintpro.app.data.local.entity.LabourEntity
import com.paintpro.app.data.remote.SupabaseProvider
import com.paintpro.app.data.remote.dto.LabourDto
import com.paintpro.app.data.remote.dto.toDto
import com.paintpro.app.data.remote.dto.toEntity
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Same offline-first read/write pattern as [SiteRepository] - see its doc comment. */
class LabourRepository(private val context: Context) {
    private val client get() = SupabaseProvider.getClient(context)
    private val dao = AppDatabase.getInstance(context).labourDao()

    fun observeLabours(orgId: String): Flow<List<LabourEntity>> = dao.observeLabours(orgId)

    fun observeActiveLabours(orgId: String): Flow<List<LabourEntity>> = dao.observeActiveLabours(orgId)

    fun observeLabour(id: String): Flow<LabourEntity?> = dao.observeById(id)

    suspend fun saveLabour(labour: LabourEntity) = withContext(Dispatchers.IO) {
        val pending = labour.copy(syncState = SyncState.PENDING_UPSERT)
        dao.upsert(pending)
        pushLabour(pending)
    }

    suspend fun deleteLabour(id: String) = withContext(Dispatchers.IO) {
        val existing = dao.getById(id) ?: return@withContext
        val deleted = existing.copy(isDeleted = true, syncState = SyncState.PENDING_DELETE)
        dao.upsert(deleted)
        pushLabour(deleted)
    }

    suspend fun syncPending() = withContext(Dispatchers.IO) {
        dao.getPendingSync().forEach { pushLabour(it) }
    }

    suspend fun refreshFromRemote(orgId: String) = withContext(Dispatchers.IO) {
        runCatching {
            val remote = client.postgrest.from("labours")
                .select { filter { eq("org_id", orgId) } }
                .decodeList<LabourDto>()
            dao.upsertAll(remote.map { it.toEntity() })
        }
    }

    private suspend fun pushLabour(labour: LabourEntity) {
        runCatching {
            if (labour.syncState == SyncState.PENDING_DELETE) {
                client.postgrest.from("labours").delete { filter { eq("id", labour.id) } }
                dao.hardDelete(labour.id)
            } else {
                client.postgrest.from("labours").upsert(labour.toDto())
                dao.markSynced(labour.id)
            }
        }
    }
}
