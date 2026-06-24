package com.concept60.app

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Concept60App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val options = FirebaseApp.getInstance().options
        android.util.Log.d("FirebaseInit", "Connected to Project: ${options.projectId}")
        android.util.Log.d("FirebaseInit", "Application ID: ${options.applicationId}")
    }
}
