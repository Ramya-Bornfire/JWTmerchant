package com.bornfire.merchantqrcode.Utils

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.EditText
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hbb20.CountryCodePicker

object PhNumValidation {
    fun updateExpectedLength(countryIso: String, editMobileNum: EditText) {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val bornfireNumber = phoneNumberUtil.getExampleNumberForType(
            countryIso,
            PhoneNumberUtil.PhoneNumberType.MOBILE
        )
        if (bornfireNumber != null) {
            val nationalSignificantNumber =
                phoneNumberUtil.getNationalSignificantNumber(bornfireNumber)
            val expectedLength = nationalSignificantNumber.length
            val inputFilter = InputFilter.LengthFilter(expectedLength)
            editMobileNum.filters = arrayOf(inputFilter)
            val inputLength = editMobileNum.text.toString().length
            if (inputLength != expectedLength) {
                editMobileNum.error = "Expected Length: $expectedLength digits"
            } else {
                editMobileNum.error = null
                editMobileNum.clearFocus()
            }
            if(inputLength==0){
                editMobileNum.error = null
                editMobileNum.clearFocus()
            }
        } else {
            editMobileNum.filters = arrayOf()
            editMobileNum.error = null
        }
    }

    fun setupPhoneNumberValidation(editText: EditText, countryCodePicker: CountryCodePicker) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (editText.text.toString().isNotEmpty()) {
                 updateExpectedLength(countryCodePicker.selectedCountryNameCode, editText)
                }
            }
        })
    }

}