package com.bornfire.merchantqrcode.Utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.widget.TextView
import com.bornfire.merchantqrcode.MainActivity
import com.bornfire.merchantqrcode.R

object SuccesssDialog {
    private var mediaPlayer: MediaPlayer? = null

    fun showImageAlertDialog(context: Context, updateText: String,activityToStart: Class<*>) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context) // Use the LayoutInflater from the context
        val view = inflater.inflate(R.layout.activity_success_dialog, null)
        val textView = view.findViewById<TextView>(R.id.textview)
        textView.text = updateText
        builder.setCancelable(false)
        builder.setView(view)
        mediaPlayer = MediaPlayer.create(context, R.raw.my_sound)
        playSound()
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(context, activityToStart)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun playSound() {
        mediaPlayer?.start()
    }
}