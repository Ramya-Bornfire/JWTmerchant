package com.bornfire.merchantqrcode

import android.app.Application
import android.widget.Toast

open class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
        // Show toast message when the application is about to terminate
        Toast.makeText(this, "Goodbye!", Toast.LENGTH_SHORT).show()
    }
}