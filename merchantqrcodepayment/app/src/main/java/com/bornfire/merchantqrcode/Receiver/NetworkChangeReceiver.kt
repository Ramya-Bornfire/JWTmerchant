package com.bornfire.merchantqrcode.Receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bornfire.merchantqrcode.NetworkUtils

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
            NetworkUtils.NoInternetAlert(context)
        }
    }
}
