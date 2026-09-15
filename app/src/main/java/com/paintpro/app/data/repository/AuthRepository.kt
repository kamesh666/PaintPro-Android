package com.paintpro.app.data.repository

import android.content.Context
import com.paintpro.app.data.local.AppDatabase
import com.paintpro.app.data.local.entity.ProfileEntity
import com.paintpro.app.data.remote.SupabaseProvider
import com.paintpro.app.data.remote.dto.ProfileDto
import com.paintpro.app.data.remote.dto.toDto
import com.paintpro.app.data.remote.dto.toEntity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Wraps Supabase Auth (email/password) plus the "profile bootstrap" step PaintPro-Web also does
 * on first sign-up. There is no team-invite flow in this v1: whoever signs up becomes the sole
 * owner of a brand-new org, and `org_id` is simply set to that user's own auth id.
 */
class AuthRepository(private val context: Context) {
    private val client get() = SupabaseProvider.getClient(context)
    private val db get() = AppDatabase.getInstance(context)

    /** Mirrors Supabase's own auth state - Initializing / NotAuthenticated / Authenticated / RefreshFailure. */
    val sessionStatus: StateFlow<SessionStatus> get() = client.auth.sessionStatus

    /** Suspends until the persisted session (if any) has finished loading at app startup. */
    suspend fun awaitInitialization() = client.auth.awaitInitialization()

    fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        businessName: String?,
        role: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val auth = client.auth
            val userInfo = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            } ?: run {
                // Some Supabase projects require email confirmation, in which case
                // signUpWith returns null and there is no session yet.
                auth.currentUserOrNull()
            } ?: error(
                "Account created, but this Supabase project requires email confirmation. " +
                    "Confirm the email, then sign in.",
            )

            val now = Clock.System.now().toString()
            val profile = ProfileEntity(
                id = userInfo.id,
                fullName = fullName,
                businessName = businessName,
                phoneNumber = null,
                role = role,
                orgId = userInfo.id,
                currency = "₹",
                createdAt = now,
            )
            db.profileDao().upsert(profile)
            runCatching { client.postgrest.from("profiles").upsert(profile.toDto()) }
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = client.auth.currentUserOrNull()?.id
                ?: error("Sign-in succeeded but no session was returned - please try again.")

            // Pull the profile down so a fresh device / reinstall has it locally too.
            runCatching {
                val dto = client.postgrest.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingle<ProfileDto>()
                db.profileDao().upsert(dto.toEntity())
            }
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        runCatching { client.auth.signOut() }
        // Single-profile-per-device model: wipe local data, the next sign-in re-syncs it.
        db.clearAllTables()
    }
}
