package com.mobilicos.smotrofon

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class
 */
@HiltAndroidApp
class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}