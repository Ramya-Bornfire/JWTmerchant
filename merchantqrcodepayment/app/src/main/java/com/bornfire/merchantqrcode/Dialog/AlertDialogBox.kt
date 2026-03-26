package com.bornfire.merchantqrcode.Dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.bornfire.merchantqrcode.R

class AlertDialogBox {
    fun showDialog(context: Context, message: String) {

        val dialog = Dialog(context)
        dialog.window
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.error_dailog_layout)
        dialog.setCancelable(false)
        dialog.show()
        val dialogHeader = dialog.findViewById(R.id.dialog_header) as TextView
        val dialogHint = dialog.findViewById(R.id.dialog_text_hint) as TextView
        val dialogTextnumber = dialog.findViewById(R.id.dialog_text_number) as TextView
        val dialogOK = dialog.findViewById(R.id.dialog_ok) as Button
        // val dialogCancel = dialog.findViewById(R.id.dialog_cancel) as Button

        dialogHeader.text = "Error "
        dialogHint.visibility = View.GONE
        dialogTextnumber.text = message
        // dialogCancel.visibility = View.GONE

        dialogOK.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
    }
}