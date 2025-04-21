package com.kaz.playlistify

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.FirebaseApp
import com.kaz.playlistify.ui.theme.AppNavigation

class MainActivity : FragmentActivity() {

    private fun solicitarPermisosUbicacion() {
        val permisos = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permisos, 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Firebase
        FirebaseApp.initializeApp(this)


        // Permisos de ubicación (opcional si no lo necesitas)
        solicitarPermisosUbicacion()

        // Carga la UI
        setContent {
            AppNavigation()
        }
    }
}
