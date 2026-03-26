package com.bornfire.merchantqrcode.Utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat

object EditNonEdit {
    fun setEditTextsNonEditable(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.isEnabled = false
        }
    }
    fun SetCaps(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed during text change
            }
            override fun afterTextChanged(s: Editable?) {
                // Remove the listener to avoid infinite loop
                editText.removeTextChangedListener(this)
                val filteredText = s.toString().filter { it.isLetter() || it.isWhitespace() }.uppercase()
                // Set the filtered text back to the EditText
                editText.setText(filteredText)
                // Move cursor to the end of the text
                editText.setSelection(filteredText.length)
                // Add the listener back
                editText.addTextChangedListener(this)
            }
        })
    }
    fun setEditTextsEditable(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.isEnabled = true
        }
    }
    fun setTextWhiteColor(context: Context,vararg editTexts: EditText) {
        for (editText in editTexts) {
            val whiteColor = ContextCompat.getColor(context, android.R.color.white)
            editText.isEnabled = true
            editText.setTextColor(whiteColor)
        }
    }
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}