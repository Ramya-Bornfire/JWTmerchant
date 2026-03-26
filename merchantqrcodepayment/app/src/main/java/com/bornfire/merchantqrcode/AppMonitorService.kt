package com.bornfire.merchantqrcode

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class AppMonitorService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        enqueueLogoutWorker()
        stopSelf()
    }
    private fun enqueueLogoutWorker() {
        val logoutWorkRequest = OneTimeWorkRequest.Builder(LogoutWorker::class.java).build()
        WorkManager.getInstance(this).enqueue(logoutWorkRequest)
        Log.d("AppMonitorService", "LogoutWorker enqueued")
    }
}
