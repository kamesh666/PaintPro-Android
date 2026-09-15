package com.paintpro.app.data.repository

import android.content.Context
import com.paintpro.app.data.local.AppDatabase
import com.paintpro.app.data.local.entity.AttendanceEntity
import com.paintpro.app.data.remote.SupabaseProvider
import com.paintpro.app.data.remote.dto.AttendanceDto
import com.paintpro.app.data.remote.dto.toDto
import com.paintpro.app.data.remote.dto.toEntity
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Same offline-first pattern as [SiteRepository], with one difference: attendance has no
 * soft-delete column, so [deleteAttendance] removes the row locally right away and only
 * best-effort mirrors the delete to Supabase (there is no pending-delete retry for attendance
 * in this MVP - acceptable since attendance is rarely edited after the fact).
 */
class AttendanceRepository(private val context: Context) {
    private val client get() = SupabaseProvider.getClient(context)
    private val dao = AppDatabase.getInstance(context).attendanceDao()

    fun observeForDate(orgId: String, date: String): Flow<List<AttendanceEntity>> =
        dao.observeForDate(orgId, date)

    fun observeAll(orgId: String): Flow<List<AttendanceEntity>> = dao.observeAll(orgId)

    fun observeForLabour(orgId: String, labourId: String): Flow<List<AttendanceEntity>> =
        dao.observeForLabour(orgId, labourId)

    suspend fun getForLabourAndDate(labourId: String, date: String): AttendanceEntity? =
        withContext(Dispatchers.IO) { dao.getForLabourAndDate(labourId, date) }

    suspend fun saveAttendance(attendance: AttendanceEntity) = withContext(Dispatchers.IO) {
        val pending = attendance.copy(syncState = SyncState.PENDING_UPSERT)
        dao.upsert(pending)
        runCatching {
            client.postgrest.from("attendance").upsert(pending.toDto())
            dao.markSynced(pending.id)
        }
    }

    suspend fun deleteAttendance(id: String) = withContext(Dispatchers.IO) {
        runCatching { client.postgrest.from("attendance").delete { filter { eq("id", id) } } }
        dao.hardDelete(id)
    }

    suspend fun syncPending() = withContext(Dispatchers.IO) {
        dao.getPendingSync().forEach { record ->
            runCatching {
                client.postgrest.from("attendance").upsert(record.toDto())
                dao.markSynced(record.id)
            }
        }
    }

    suspend fun refreshFromRemote(orgId: String) = withContext(Dispatchers.IO) {
        runCatching {
            val remote = client.postgrest.from("attendance")
                .select { filter { eq("org_id", orgId) } }
                .decodeList<AttendanceDto>()
            dao.upsertAll(remote.map { it.toEntity() })
        }
    }
}
