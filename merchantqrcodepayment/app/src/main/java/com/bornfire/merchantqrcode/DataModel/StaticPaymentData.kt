package com.bornfire.merchantqrcode.DataModel

import java.math.BigDecimal
import java.util.Date

data class StaticPaymentData(
    val notification_id: String,
    val merchant_id: String,
    val user_id: String,
    val device_id: String,
    val tran_date: Date,
    val notification_flag: String,
    val tran_amount: BigDecimal
)
