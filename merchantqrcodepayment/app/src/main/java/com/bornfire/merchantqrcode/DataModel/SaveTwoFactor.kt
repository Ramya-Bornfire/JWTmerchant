package com.bornfire.merchantqrcode.DataModel

data class  SaveTwoFactor (
        var user_id: String,
        var username: String,
        var password_hash: String,
        var two_fa_enabled: String,
        var preferred_method: String,
        var phone_number: String,
        var email: String,
        var auth_app_secret: String,
        var security_answer_1: String,
        var security_answer_2: String,
        var security_answer_3: String,
        var security_answer_4: String,
        var security_answer_5: String,
        var security_answer_6: String,
        var security_answer_7: String,
        var security_answer_8: String,
        var security_answer_9: String,
        var security_answer_10: String
    )