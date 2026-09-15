package com.paintpro.app.data.remote

import android.content.Context
import com.paintpro.app.BuildConfig

/**
 * "Bring your own Supabase" override, mirroring PaintPro-Web's self-serve model: by default the
 * app talks to the shared demo project baked into [BuildConfig], but a contractor can point it at
 * their own Supabase project from the Settings screen. Stored in plain [android.content.SharedPreferences]
 * rather than DataStore to avoid one more dependency for two small strings.
 */
object SupabaseConfigStore {
    private const val PREFS_NAME = "paintpro_settings"
    private const val KEY_URL = "supabase_url_override"
    private const val KEY_ANON_KEY = "supabase_anon_key_override"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getUrl(context: Context): String =
        prefs(context).getString(KEY_URL, null)?.takeIf { it.isNotBlank() }
            ?: BuildConfig.DEFAULT_SUPABASE_URL

    fun getAnonKey(context: Context): String =
        prefs(context).getString(KEY_ANON_KEY, null)?.takeIf { it.isNotBlank() }
            ?: BuildConfig.DEFAULT_SUPABASE_ANON_KEY

    fun hasOverride(context: Context): Boolean =
        prefs(context).getString(KEY_URL, null)?.isNotBlank() == true

    fun setOverride(context: Context, url: String, anonKey: String) {
        prefs(context).edit()
            .putString(KEY_URL, url.trim())
            .putString(KEY_ANON_KEY, anonKey.trim())
            .apply()
        SupabaseProvider.reset()
    }

    fun clearOverride(context: Context) {
        prefs(context).edit()
            .remove(KEY_URL)
            .remove(KEY_ANON_KEY)
            .apply()
        SupabaseProvider.reset()
    }
}
