package com.bornfire.merchantqrcode.Utils

import android.widget.EditText

object NullCheck {

    fun getValidText(value: String?, default: String = ""): String {
        return if (value == null || value.equals("null", ignoreCase = true) || value.isNullOrBlank()) {
            default
        } else {
            value
        }
    }
}