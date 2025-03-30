package com.kaz.playlistify

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider



class MyCastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        val receiverId = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
        return CastOptions.Builder()
            .setReceiverApplicationId(receiverId)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}
