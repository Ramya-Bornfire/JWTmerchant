package com.bornfire.merchantqrcode

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.view.LayoutInflater
import androidx.core.content.ContextCompat

object NetworkUtils {
    var pd: TransparentProgressDialog? = null
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
       // startStaticSoundBoxService(context)
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
    fun NoInternetAlert(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialogStyle)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.activity_alert_dialog_box, null)
        builder.setView(view)
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        alertDialog.show()
       // stopStaticSoundBoxService(context)
    }
        fun showProgress(context: Context) {
            pd = TransparentProgressDialog(context, R.drawable.bornfire_btm_logo)
            pd?.show()
        }
        fun hideProgress(context: Context) {
            pd?.takeIf { it.isShowing }?.dismiss()
        }

/*    fun startStaticSoundBoxService(context: Context) {
        val serviceIntent = Intent(context, StaticSoundBoxService::class.java)
        context.startService(serviceIntent)
    }
    // Method to stop the StaticSoundBoxService
    fun stopStaticSoundBoxService(context: Context) {
        val serviceIntent = Intent(context, StaticSoundBoxService::class.java)
        context.stopService(serviceIntent)
    }*/
}
