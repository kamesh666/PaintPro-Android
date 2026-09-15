package com.paintpro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paintpro.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * There is exactly one signed-in profile on a device at a time, so this DAO always
 * operates on "the" row rather than taking an id in most calls.
 */
@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles LIMIT 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getProfileOnce(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun clear()
}
