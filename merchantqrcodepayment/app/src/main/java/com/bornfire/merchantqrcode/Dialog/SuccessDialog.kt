package com.bornfire.merchantqrcode.Dialog

import android.app.AlertDialog
import android.content.Intent
import android.content.Context

import android.media.MediaPlayer
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.bornfire.merchantqrcode.R

object SuccessDialog {
    private var mediaPlayer: MediaPlayer? = null

    fun callDialog(context: Context, intent: Intent) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.sucessdialog, null)
        builder.setCancelable(false)
        builder.setView(view)
        playSound(context)  // Pass context to playSound

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            context.startActivity(intent)
            if (context is AppCompatActivity) {
                context.finish()
            }
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun playSound(context: Context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.iphone)
        mediaPlayer?.start()
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
