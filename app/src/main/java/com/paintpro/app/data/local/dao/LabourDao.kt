package com.paintpro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paintpro.app.data.local.entity.LabourEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabourDao {
    @Query("SELECT * FROM labours WHERE orgId = :orgId AND isDeleted = 0 ORDER BY name ASC")
    fun observeLabours(orgId: String): Flow<List<LabourEntity>>

    @Query("SELECT * FROM labours WHERE orgId = :orgId AND isDeleted = 0 AND isActive = 1 ORDER BY name ASC")
    fun observeActiveLabours(orgId: String): Flow<List<LabourEntity>>

    @Query("SELECT * FROM labours WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LabourEntity?

    @Query("SELECT * FROM labours WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<LabourEntity?>

    @Query("SELECT * FROM labours WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<LabourEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(labour: LabourEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(labours: List<LabourEntity>)

    @Query("UPDATE labours SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM labours WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("DELETE FROM labours WHERE orgId = :orgId")
    suspend fun clearForOrg(orgId: String)
}
