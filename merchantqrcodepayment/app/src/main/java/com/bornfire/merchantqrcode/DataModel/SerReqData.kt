package com.bornfire.merchantqrcode.DataModel

data class SerReqData(
    val request_id: String?,
    val merchant_id: String?,
    val request_type: String?,
    val request_description: String?,
    val steps_to_reproduce: String?,
    val error_message: String?,
    val priority: String?,
    val contact_email: String?,
    val contact_phone: String?,
    val countrycode:String?,
    val additional_notes: String?,
    val status: String?,
    val approved_by: String?,
    val assign_to: String?,
    val entry_user: String?,
    val request_date:String?,
    val approved_date: String?,
    val assigned_date: String?,
    val unit_id:String,
    val user_id:String
)
