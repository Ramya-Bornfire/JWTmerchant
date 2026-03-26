package com.bornfire.merchantqrcode.DataModel

data class DynamicQRRequest(
    val merchant_ID :String,
    val tran_amt :String,
    val mob_num :String,
    val bill_num :String,
    val ref_label:String
)