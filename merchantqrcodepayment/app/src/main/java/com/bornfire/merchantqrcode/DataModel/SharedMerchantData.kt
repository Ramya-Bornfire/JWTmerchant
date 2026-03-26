package com.bornfire.merchantqrcode.DataModel

data class SharedMerchantData(
    val merchant_rep_id:String?,
    val mer_representative_name:String?,
    val merchant_user_id: String?,
    val merchant_name: String?,
    val merchant_legal_user_id: String?,
    val merchant_corporate_name: String?,
    val password: String?,
    val password_expiry_date: String?,
    val password_life: String?,
    val account_expiry_date: String?,
    val user_disable_flag: String?,
    val user_disable_from_date: String?,
    val user_disable_to_date: String?,
    val del_flag: String?,
    val user_status: String?,
    val login_status: String?,
    val login_channel: String?,
    val mobile_no: String?,
    val alternate_mobile_no: String?,
    val email_address: String?,
    val alternate_email_id: String?,
    val no_of_concurrent_users: Int?,
    val no_of_active_devices: Int?,
    val entry_user: String?,
    val modify_user: String?,
    val verify_user: String?,
    val entry_time: String?,
    val modify_time: String?,
    val verify_time: String?,
    val unit_id:String?,
    val unit_type:String?,
    val unit_name:String?,
    val maker_or_checker: String?,
    val entry_flag:String?,
    val modify_flag :String?,
    val pwlog_flg:String?,
    val authenticationflg:String?,
    val countrycode:String?,
    )
data class usercat(
    val user_category:String?
)

object SharedusercatDataObj {
    var UserCategory: usercat? = null
}
object SharedMerchantDataObj {
    var merchantData: SharedMerchantData? = null
}