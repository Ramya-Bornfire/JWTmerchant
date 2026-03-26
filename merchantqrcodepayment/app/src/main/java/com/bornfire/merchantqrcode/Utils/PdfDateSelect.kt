package com.bornfire.merchantqrcode.Utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.*

object PdfDateSelect {
     var fromDate: Calendar = Calendar.getInstance()
     var toDate: Calendar = Calendar.getInstance()
     var tranFrom = ""
     var tranTo = ""
    fun showDatePickerDialog(isFromDate: Boolean, edit1: EditText, edit2: EditText,context:Context,future:Boolean) {
        val currentDate = if (isFromDate) fromDate else toDate
        val today = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                currentDate.set(year, monthOfYear, dayOfMonth)
                if (isFromDate) {
                    updateFromDate(currentDate, edit1)
                } else {
                    updateToDate(currentDate, edit2)
                }
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )
        if(future){
            datePickerDialog.datePicker.maxDate
        }
        else{
            // Restrict future dates
            datePickerDialog.datePicker.maxDate = today.timeInMillis
        }

        if (!isFromDate) {
            // Set min date for toDate as one day after fromDate
            datePickerDialog.datePicker.minDate =
                fromDate.timeInMillis // 86400000 milliseconds = 1 day
        }
        datePickerDialog.show()
    }
     private fun updateFromDate(date: Calendar, edit1: EditText) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        edit1.setText(dateFormat.format(date.time))
        tranFrom = dateFormat.format(date.time)
    }
     private fun updateToDate(date: Calendar, edit1: EditText) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        edit1.setText(dateFormat.format(date.time))
        tranTo = dateFormat.format(date.time)
    }
}