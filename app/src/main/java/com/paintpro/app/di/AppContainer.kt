package com.paintpro.app.di

import android.content.Context
import com.paintpro.app.data.repository.AttendanceRepository
import com.paintpro.app.data.repository.AuthRepository
import com.paintpro.app.data.repository.LabourRepository
import com.paintpro.app.data.repository.ProfileRepository
import com.paintpro.app.data.repository.SiteRepository

/**
 * A small hand-rolled service locator. The app is simple enough that pulling in Hilt/Dagger
 * would be one more dependency-version risk for no real benefit - every repository here is a
 * cheap, stateless-ish wrapper around Room + Supabase, safe to share as a singleton.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val authRepository: AuthRepository by lazy { AuthRepository(appContext) }
    val profileRepository: ProfileRepository by lazy { ProfileRepository(appContext) }
    val siteRepository: SiteRepository by lazy { SiteRepository(appContext) }
    val labourRepository: LabourRepository by lazy { LabourRepository(appContext) }
    val attendanceRepository: AttendanceRepository by lazy { AttendanceRepository(appContext) }
}
