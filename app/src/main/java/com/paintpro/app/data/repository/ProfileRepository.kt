package com.paintpro.app.data.repository

import android.content.Context
import com.paintpro.app.data.local.AppDatabase
import com.paintpro.app.data.local.entity.ProfileEntity
import com.paintpro.app.data.remote.SupabaseProvider
import com.paintpro.app.data.remote.dto.toDto
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProfileRepository(private val context: Context) {
    private val client get() = SupabaseProvider.getClient(context)
    private val dao = AppDatabase.getInstance(context).profileDao()

    fun observeProfile(): Flow<ProfileEntity?> = dao.observeProfile()

    suspend fun getProfileOnce(): ProfileEntity? = dao.getProfileOnce()

    suspend fun updateProfile(profile: ProfileEntity) = withContext(Dispatchers.IO) {
        dao.upsert(profile)
        runCatching { client.postgrest.from("profiles").upsert(profile.toDto()) }
    }
}
