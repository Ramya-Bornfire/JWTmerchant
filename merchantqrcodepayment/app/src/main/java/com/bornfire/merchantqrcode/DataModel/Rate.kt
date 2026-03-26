package com.bornfire.merchantqrcode.DataModel

data class Rate(
    val srl: String?,
    val billing_currency: String?,
    val settlement_currency: String?,
    val rate: Double?,
    val effective_date: String?,
    val audit_date: String?,
)
