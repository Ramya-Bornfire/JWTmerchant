package com.bornfire.merchantqrcode.DataModel

data class gridmodel(
    val Name: String,
    val Img: Int
)
data class ScanQRrequest(
    val base64QR : String
)