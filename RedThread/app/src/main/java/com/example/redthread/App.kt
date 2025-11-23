package com.example.redthread

import android.app.Application
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.remote.ApiClient

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        val session = SessionPrefs(this)
        ApiClient.init(session)
    }

}
