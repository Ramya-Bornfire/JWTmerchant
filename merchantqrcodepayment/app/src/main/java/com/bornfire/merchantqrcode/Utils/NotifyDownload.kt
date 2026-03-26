package com.bornfire.merchantqrcode.Utils

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bornfire.merchantqrcode.R
import java.io.File

object NotifyDownload {
    private const val CHANNEL_ID = "download_channel"
    private const val NOTIFICATION_ID = 1
    private const val REQUEST_CODE_POST_NOTIFICATIONS = 101
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun openExcelFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            // Set the URI for the Documents folder directly
            setDataAndType(Uri.parse("content://com.android.externalstorage.downloads/tree/primary:Download"), "*/*")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Create a PendingIntent with FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Show the notification when clicked
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo) // Your notification icon
            .setContentTitle("Open Documents")
            .setContentText("Tap to open the Documents folder.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATIONS)
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
    fun createNotificationChannel(context: Context) {
        val name = "Download Successful!"
        val descriptionText = "Click to open the downloaded Excel file."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}