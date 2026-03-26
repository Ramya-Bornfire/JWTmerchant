package com.bornfire.merchantqrcode.DataModel
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
data class OtpData( // "00Success" in case of success
    val mobileNumber: String,
)

