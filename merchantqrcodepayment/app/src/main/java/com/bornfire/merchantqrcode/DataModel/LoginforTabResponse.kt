package com.bornfire.merchantqrcode.DataModel

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class LoginforTabResponse(
    @JsonProperty("Status") val status: String,
    @JsonProperty("Message") val message: String
    )


