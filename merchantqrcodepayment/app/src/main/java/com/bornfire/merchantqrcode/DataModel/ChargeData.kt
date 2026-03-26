package com.bornfire.merchantqrcode.DataModel

data class ChargeData(
    val tran_date: String?,
    val sequence_unique_id: String?,
    val tran_audit_number: String?,
    val merchant_bill_number: String?,
    val bill_date: String?,
    val bill_amount: Number?,
    val tran_amount:Number?,
    val tran_currency: String?,
    var reversal_date: String?,
    val reversal_amount:Number?,
    val reversal_remarks: String?,
    val auth_time: String?,
    val auth_user: String?
)
