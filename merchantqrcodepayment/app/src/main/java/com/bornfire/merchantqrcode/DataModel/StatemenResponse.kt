package com.bornfire.merchantqrcode.DataModel

data class StatemenResponse(
    val placeData :String?,
    val stateData :String?,
    val phNumData :String?,
    val tranCurrencyData :String?,
    val tranDate :String?,
    val sequenceUniqueId :String?,
    val merchantBillNumber :String?,
    val unitId  :String?,
    val userId :String?,
    val deviceId:String?,
    val bankNameData :String?,
    val tranAmount :String?,
    val tranStatus :String?,
)
