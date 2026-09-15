package com.paintpro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paintpro.app.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE orgId = :orgId AND date = :date ORDER BY createdAt DESC")
    fun observeForDate(orgId: String, date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE orgId = :orgId ORDER BY date DESC, createdAt DESC")
    fun observeAll(orgId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE orgId = :orgId AND labourId = :labourId ORDER BY date DESC")
    fun observeForLabour(orgId: String, labourId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE labourId = :labourId AND date = :date LIMIT 1")
    suspend fun getForLabourAndDate(labourId: String, date: String): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<AttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<AttendanceEntity>)

    @Query("UPDATE attendance SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: String)

    /**
     * Attendance has no soft-delete column in this MVP - a delete is applied to Supabase
     * first (when online) and then removed locally, or removed locally immediately when
     * offline (best-effort; it will simply not exist to re-push). This keeps the model
     * simple since attendance rows are rarely edited after the fact.
     */
    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("DELETE FROM attendance WHERE orgId = :orgId")
    suspend fun clearForOrg(orgId: String)
}
