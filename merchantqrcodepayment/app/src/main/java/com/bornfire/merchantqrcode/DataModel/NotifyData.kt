package com.bornfire.merchantqrcode.DataModel

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date

data class NotifyData(
    val record_srl_no:String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val record_date: String,
    val tran_category:String,
    val notification_event_no:String,
    val notification_event_desc:String,
    val notification_limit:String?,
    val notification_user_1:String?,
    val notification_user_2:String?,
    val notification_user_3:String?,
    val notification_sms_flg:String?,
    val notification_mobile_1:String?,
    val notification_mobile_2:String?,
    val notification_mobile_3:String?,
    val countrycode_1:String?,
    val countrycode_2:String?,
    val countrycode_3:String?,
    val notification_email_flg:String,
    val notification_email_1:String?,
    val notification_email_2:String?,
    val notification_email_3:String?,
    val alert_flg:String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val start_date: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val end_date: String,
    val entity_flg:String,
    val del_flg:String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val entry_time:String,
    val modify_time: String?,
    val verify_time: String?,
    val entry_user:String,
    val modify_user:String,
    val verify_user:String,
    val usercategory:String,
    val channel:String,
    val merchantid:String,
    val unitid:String,
    val frequency:String,
)