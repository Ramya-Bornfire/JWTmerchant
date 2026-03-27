package com.bornfire.merchantqrcode.DataModel

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class LoginforTabResponse(
    @JsonProperty("Status") val status: String,
    @JsonProperty("data") val data: String?,
    @JsonProperty("Message") val message: String,
    @JsonProperty("token") val token: String?
    )


