package com.paintpro.app

import android.app.Application
import com.paintpro.app.di.AppContainer

class PaintProApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
