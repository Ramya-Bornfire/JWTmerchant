package com.bornfire.merchantqrcode.DataModel

data class ScanQRResponse(
    val payloadFormatIndiator :String,
    val pointOfInitiationFormat  :String,
    val payeeAccountInformation : payeeAccountInformation,
    val mcc  :String,
    val currency  :String,
    val trAmt  :String,
    val tipOrConvenienceIndicator  :String,
    val convenienceIndicatorFee  :String,
    val countryCode  :String,
    val customerName  :String,
    val city  :String,
    val additionalDataInformation: AdditionalDataInformation,
)
data class payeeAccountInformation(
    val globalID :String,
    val payeeParticipantCode :String,
    val customerID :String,

)
data class AdditionalDataInformation(
   val billNumber :String,
   val mobileNumber :String,
   val storeLabel :String,
   val deviceID :String,
   val referenceNumber :String,
   val customerLabel :String,
   val terminalLabel :String,
   val purposeOfTransaction :String,
   val addlDataRequest :String,
)
