package com.kaz.playlistify

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseApp
import com.kaz.playlistify.ui.theme.AppNavigation
import android.Manifest
import com.google.android.gms.cast.framework.CastContext


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

        // Primero inicializo Firebase
        FirebaseApp.initializeApp(this)

        // Ahora me aseguro de inicializar CastContext correctamente
        CastContext.getSharedInstance(this)

        // Solicito permisos de ubicación
        solicitarPermisosUbicacion()

        // Finalmente, cargo el contenido de Compose
        setContent {
            AppNavigation()
        }
    }




}
