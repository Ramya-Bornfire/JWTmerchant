package com.bornfire.merchantqrcode.Utils

import android.content.Context
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.R

object TextforTable {
    fun createTextView(context: Context, text: String?): TextView {
        val textView = TextView(context).apply {
            this.text = text?.uppercase() ?: ""
            setTextColor(
                if (text.equals("FAILURE"))
                    ContextCompat.getColor(context, R.color.red)
                else
                    ContextCompat.getColor(context, R.color.white)
            )
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(8, 8, 8, 8)
            gravity = android.view.Gravity.START
        }
        return textView
    }

    fun createAmountTextView(context: Context,text:String):TextView{
        val textView = TextView(context)
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
        textView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        textView.setPadding(8, 8, 80, 8)
        textView.gravity = android.view.Gravity.RIGHT
        return textView
    }

    fun formatNumberWithGroups(number: Double): String {
        val numberString = number.toLong().toString()  // Convert the number to string
        val decimalPart = if (number.rem(1) != 0.0) {
            val formattedDecimal = "%.2f".format(number - number.toLong())
            if (formattedDecimal.startsWith("0")) formattedDecimal.substring(1) else formattedDecimal
        } else {
            ".00"
        }
        val formattedIntegerPart = numberString.reversed().chunked(3).joinToString(",").reversed()
        return "$formattedIntegerPart$decimalPart"
    }
}