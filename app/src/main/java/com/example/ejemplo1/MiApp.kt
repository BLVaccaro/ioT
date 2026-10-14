package com.example.ejemplo1

import android.app.Application

class MiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SyncManager.iniciar(this)
        registerActivityLifecycleCallbacks(EstadoBanner)
    }
}