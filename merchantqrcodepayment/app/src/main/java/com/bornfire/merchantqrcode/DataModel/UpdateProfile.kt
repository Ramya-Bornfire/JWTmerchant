package com.bornfire.merchantqrcode.DataModel

data class UpdateProfile(
    val merchant_rep_id:String?,
    val mer_representative_name:String?,
    val merchant_user_id: String?,
    val merchant_name: String?,
    val merchant_corporate_name: String?,
    val user_disable_from_date: String?,
    val user_disable_to_date: String?,
    val no_of_concurrent_users: Int?,
    val no_of_active_devices: Int?,
    val mobile_no: String?,
    val email_address: String?,
    val unit_type:String?,
    val unit_name:String?,
    val countrycode:String?
)

