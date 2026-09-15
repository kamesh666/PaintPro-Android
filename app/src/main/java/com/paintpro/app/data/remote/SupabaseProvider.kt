package com.paintpro.app.data.remote

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Holds the single [SupabaseClient] instance the app uses. Rebuilt lazily whenever the
 * configured URL changes (i.e. right after a Settings-screen override), since a
 * [SupabaseClient] is otherwise cheap to keep alive for the whole process lifetime.
 */
object SupabaseProvider {
    @Volatile
    private var client: SupabaseClient? = null

    @Volatile
    private var clientUrl: String? = null

    fun getClient(context: Context): SupabaseClient {
        val url = SupabaseConfigStore.getUrl(context)
        val anonKey = SupabaseConfigStore.getAnonKey(context)

        client?.let { if (clientUrl == url) return it }

        synchronized(this) {
            client?.let { if (clientUrl == url) return it }
            val created = createSupabaseClient(
                supabaseUrl = url,
                supabaseKey = anonKey,
            ) {
                httpEngine = OkHttp.create()
                install(Auth)
                install(Postgrest)
            }
            client = created
            clientUrl = url
            return created
        }
    }

    /** Called by [SupabaseConfigStore] after the Supabase URL/key override changes. */
    fun reset() {
        synchronized(this) {
            client = null
            clientUrl = null
        }
    }
}
