package com.kaz.playlistify

import android.app.Application
import com.google.android.gms.cast.framework.CastContext
import com.google.firebase.FirebaseApp
import java.util.concurrent.Executors

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        // Inicializa Google Cast con Executor válido
        CastContext.getSharedInstance(this, Executors.newSingleThreadExecutor())
    }
}
