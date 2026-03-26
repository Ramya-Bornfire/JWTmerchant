package com.bornfire.merchantqrcode.DataModel

data class LoginData(
    val userid: String,
    val password: String,
    val ip_address: String,
    val device_id: String,
    val device_type: String,
    val os_version: String,
    val app_version: String,
)