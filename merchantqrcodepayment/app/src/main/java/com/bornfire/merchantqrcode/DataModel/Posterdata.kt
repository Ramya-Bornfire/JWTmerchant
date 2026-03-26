package com.bornfire.merchantqrcode.DataModel

import java.io.File

data class Posterdata(
    val merchant_id:String,
    val merchant_rep_id:String,
    val poster_id:String,
    val unit_id:String,
    val poster_date: String,
    val file: File,
)
