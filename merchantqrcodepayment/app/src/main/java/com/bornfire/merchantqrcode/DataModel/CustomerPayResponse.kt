package com.bornfire.merchantqrcode.DataModel

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.util.*

data class CustomerPayResponse (
    var merchant_id: String,
    var device_id: String,
    var tran_status: String,
    var cbs_tran_status: String,
    var amount: BigDecimal,
    var referencelabel: String,
    val tran_id:String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val tran_date: Date,
    val merchant_name:String,
    val merchant_addr:String,
    val merchant_city:String,
    val merchant_terminal:String)