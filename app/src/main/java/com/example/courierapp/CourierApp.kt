package com.example.courierapp

import android.app.Application
import com.example.courierapp.BuildConfig
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class CourierApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(
            this,
            BuildConfig.STADIA_API_KEY,
            WellKnownTileServer.MapLibre
        )
    }
}