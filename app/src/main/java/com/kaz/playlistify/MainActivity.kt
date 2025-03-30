package com.kaz.playlistify

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
import com.kaz.playlistify.ui.theme.AppNavigation

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        setContent {
            AppNavigation()
        }
    }
}
