package com.kaz.playlistify

import android.app.Application

import com.google.firebase.FirebaseApp
import java.util.concurrent.Executors

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)


    }
}
