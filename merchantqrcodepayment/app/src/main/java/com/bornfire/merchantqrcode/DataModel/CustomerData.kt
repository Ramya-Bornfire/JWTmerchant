package com.bornfire.merchantqrcode.DataModel

data class CustomerData (
    val tran_date: String,
    val sequence_unique_id: String,
    val initiator_bank: String,
    val tran_currency: String,
    val tran_amount: String,
    val tran_status: String,
    val tran_rmks: String,
    val ipsx_account: String,
    val cim_account: String,
    val ipsx_account_name: String,
    val cim_account_name: String,
    val part_tran_type: String,
    val merchant_id: String,
    val reversal_remarks: String,
    val merchant_bill_number: String,
    val reversal_date: String,
    val reversal_amount: Number,
    val auth_user: String,
    val auth_time: String,
    val user_id: String,
    val user_name: String
)