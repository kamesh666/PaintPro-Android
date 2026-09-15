package com.paintpro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paintpro.app.data.local.entity.SiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites WHERE orgId = :orgId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun observeSites(orgId: String): Flow<List<SiteEntity>>

    @Query("SELECT * FROM sites WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SiteEntity?

    @Query("SELECT * FROM sites WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<SiteEntity?>

    @Query("SELECT * FROM sites WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<SiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(site: SiteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sites: List<SiteEntity>)

    @Query("UPDATE sites SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: String)

    /** Hard-remove a row once its pending delete has been confirmed on the server. */
    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("DELETE FROM sites WHERE orgId = :orgId")
    suspend fun clearForOrg(orgId: String)
}
