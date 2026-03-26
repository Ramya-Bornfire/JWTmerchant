package com.bornfire.merchantqrcode.retrofit

import com.bornfire.merchantqrcode.*
import com.bornfire.merchantqrcode.DataModel.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url
import java.io.File
interface ApplicationApi {
    //Send SMS
    @GET("SendSMS")
    fun sendSms(
        @Query("SenderId") senderId: String,
        @Query("Message") message: String,
        @Query("MobileNumbers") mobileNumbers: String,
        @Query("ApiKey") apiKey: String,
        @Query("ClientId") clientId: String
    ): Call<OtpData>

    @Headers("Content-Type: application/json")
    @POST("api/LoginAndroid")
    fun loginAndroid(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>
    //Logout Api
    @Headers("Accept: application/json")
    @POST("api/LogoutForTab")
    fun logoutTab(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @POST("api/LogoutforMobile")
    fun logoutMobile(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @POST("api/AuthenticationForRep")
    fun saveTwoFactorAuthentication(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @GET("api/CheckTwoFactorAnswer")
    fun checkTwoFactorAnswer(@Query("userId") userid:String, @Query("answerNumber") answerNumber:Int, @Query("answer") answer:String,@Header("PSU_Device_ID") PSU_Device_ID:String):Call<ResponseBody>

    //Otp API
    @GET("api/OtpForMerchant")
    fun getOtpForMerchant(@Query ("merchant_rep_id") merchant_user_id: String, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @GET("api/OtpForAndroid")
    fun getOtpForAndroid(@Query ("merchant_rep_id") merchant_user_id: String, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @GET("api/OtpForUser")
    fun getOtpForUser(@Query ("user_id") user_id: String, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    //Reset Password Api
    @POST("api/ResetUserPassword")
    fun resetPasswordUser(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String) : Call<ResponseBody>

    @POST("api/ResetMerchantPassword")
    fun resetPasswordMerchant(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @POST("api/ResetUserNewPassword")
    fun updateUserNewPassword(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @POST("api/ResetMerchantNewPassword")
    fun updateRepNewPassword(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    //Check Password Api
    @GET("api/CheckPasswordForMobile")
    fun getPasswordForUser(@Query ("merchant_id") user_id: String,@Query("password") password:String,  @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @GET("api/CheckPasswordForTab")
    fun getPasswordForMerchant(@Query ("merchant_id") user_id: String,@Query("password") password:String,  @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    //User Management Api
    @GET("api/AllUserManagementList")
    fun getUserList(@Query ("merchant_user_id") merchant_user_id: String,@Query("unit_id") unitId:String): Call<List<UserData>>

    @POST("api/AddUserData")
    fun addUserData(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST
    fun updateUserData(@Url url: String,@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @POST("api/VerifyUserdetails")
    fun verifyUser(@Header("PSU_Device_ID") PSU_Device_ID:String,@Body encryptedRequest: EncryptedRequest):Call<ResponseBody>

    @POST("api/DeleteUserData")
    fun deleteUser(@Query ("userid") user_id: String,@Query ("remark") remark: String,@Query ("verifyuser") ver_user: String):Call<ResponseBody>

    @GET("api/uniqueUserId")
    fun getUserId(@Query ("merchant_id") mer_id: String): Call<String>

    //Device Management Api
    @GET("api/AllDeviceList")
    fun getDeviceList(@Query ("merchant_user_id") merchant_user_id: String,@Query("unit_id") unitId:String): Call<List<DeviceData>>

    @POST("api/AddDevice")
    fun addDeviceData(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @POST
    fun updateDeviceData(@Url url: String,@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @POST("api/VerifyDevicedetails")
    fun verifyDevice(@Header("PSU_Device_ID") PSU_Device_ID:String,@Body encryptedRequest: EncryptedRequest):Call<ResponseBody>

    @POST("api/DeleteDeviceData")
    fun deleteDevice(@Query ("deviceid") user_id: String,@Query ("remark") remark: String,@Query ("verifyuser") ver_user: String):Call<ResponseBody>

    @GET("api/uniqueDeviceId")
    fun getDeviceId(@Query ("merchant_id") mer_id: String): Call<String>

    @GET("api/UnitList")
    fun getUnitList(@Query ("merchant_id")merchant_id: String?):Call<List<UnitData>>

    //Rate Maintenance Api
    @GET("api/RateMaintenanceList")
    fun getRateMaintaincelist(): Call<List<Rate>>

    @POST("api/AddNewRate")
    fun addRateMaintenance(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @PUT
    fun updateRateMaintenance(@Url url: String,@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    //Customer Transaction Api
    @GET("api/AllCustomerTransactionList")
    fun getCustomerTransactionList(@Query ("merchant_id") user_id: String,@Query("unit_id") unitId:String, @Query ("fromdate") from: String, @Query("todate") to:String,@Query ("type") type:String): Call<List<CustomerData>>

    //Fees and Charges Api
    @GET("api/AllFeesAndChargesList")
    fun getFeesChargesList(@Query ("merchant_id") user_id: String,@Query("unit_id") unitId:String, @Query ("fromdate") from: String, @Query("todate") to:String,@Query ("type") type:String): Call<List<Feesdata>>


    @GET("api/AllChargeBackList")
    fun getChargeBackList(@Query("merchant_id") user_id: String,@Query("unit_id") unitId:String, @Query ("fromdate") from: String, @Query("todate") to:String): Call<List<ChargeData>>

    @GET("api/AllMerchantPendingChargeBack")
    fun getPendingChargebackList(@Query ("merchant_id") user_id: String,@Query("unit_id") unitId:String, @Query ("fromdate") from: String, @Query("todate") to:String): Call<List<ChargeData>>

    @GET("api/AllMerchantRevertedChargeBack")
    fun getRevertedChargebackList(@Query ("merchant_id") user_id: String,@Query("unit_id") unitId:String, @Query ("fromdate") from: String, @Query("todate") to:String): Call<List<ChargeData>>

    @POST("api/InititedChargeBack")
    fun initiateChargeBack(@Query ("userid") user_id: String,@Query ("seqUniqueID") seqUniqueID: String,@Query("merchant_id") merchantId:String):Call<ResponseBody>

    @POST("api/ws/revertMerchantFndTransfer")
    fun approveChargeBack(@Query ("userid") user_id: String,@Query ("seqUniqueID") seqUniqueID: String):Call<ResponseBody>

    @GET("api/AlertListForAdmin")
    fun getAlert():Call<List<AlertData>>

    @GET("api/AllNotificationList")
    fun getNotificationList(@Query("merchant_id") merchant_id:String,@Query("unit_id") unit_id:String ):Call<List<NotifyData>>

    @POST("api/AddNotification")
    fun addNotification(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @GET("api/NotifiParamId")
    fun getNotificationId(): Call<String>

    @GET("api/ServiceRequestId")
    fun getServiceId(): Call<String>

    @GET("api/AllServiceRequestList")
    fun getServiceRequestList(@Query("merchant_id") merchant_id:String,@Query("unit_id") unit_id:String ):Call<List<SerReqData>>

    @POST("api/AddServiceReq")
    fun addServiceRequest(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>


    //Generate Static QR Code
    @POST("api/ws/StaticMaucas")
    fun generateStaticQRCode(@Header("P-ID") p_id:String,@Header("PSU-Device-ID") PSU_Device_ID:String,@Header("PSU-IP-Address") PSU_IP_Address:String,@Header("PSU-ID") PSU_ID:String,@Header("PSU-Channel") PSU_Channel:String, @Header("Merchant_ID") merchant_id:String,@Header("Device_ID")deviceID :String,
                             @Header("Reference_Number") referenceNumber:String, @Header("User-ID") UserID:String, @Header("Unit-ID") unit_id:String,@Body code:ScanQRrequest):Call<StaticResponse>

    @GET("api/getStaticPaydetails")
    fun getStaticPayment(@Query ("merchant_id") mer_id: String,@Query ("device_id") devId:String,@Query("userid") userId:String): Call<List<StaticPaymentData>>

    //Generate Dynamic QR Code
    @POST("api/ws/DynamicMaucas")
    fun generateDynamicQRCode(@Header ("P-ID") P_ID:String,@Header("PSU-Device-ID")PSU_Device_ID:String,@Header("Device-ID") Device_ID:String, @Header("PSU-IP-Address") PSU_IP_Address:String,@Header("PSU-ID") PSU_ID:String,@Header("PSU-Channel") PSU_Channel:String, @Header("Merchant_ID") Merchant_ID: String,@Header("User-ID") User_ID:String,@Header("Unit-ID") unit_id:String,@Body requestBody: DynamicQRRequest):Call<DynamicQRResponse>

    @GET("api/getTranAmountLimit")
    fun getTranAmountLimit(@Query("merchant_id") merchantId:String):Call<ResponseBody>

    @GET("api/getCustomerPaydetails")
    fun getCustomerDetails(@Query("merchant_id") merchantId:String, @Query("device_id") deviceId:String, @Query("reference_number") referenceNumber:String,@Header("PSU_Device_ID") psu_dev_id:String):Call<ResponseBody>

    @POST("api/ws/scanCustomerQRcode")
    fun scanQR(@Header ("P-ID") P_ID:String, @Header("PSU-Device-ID") PSU_Device_ID:String, @Header ("PSU-IP-Address") PSU_IP_Address:String, @Header ("PSU-ID") PSU_ID:String,  @Header ("PSU-Channel") PSU_Channel:String, @Body qrCode:String ):Call<ScanQRResponse>

    @POST("api/ws/InitiateCustomerTransaction")
    fun initiateTransaction(@Header("P-ID") pId:String,@Header("PSU-Device-ID") psu_dev_id:String,@Header("PSU-IP-Address")psu_ip:String,@Header("PSU-ID") psu_id:String,@Header("PSU-Channel") psuChannel:String,@Header("PSU-Resv-Field1") psu_rev_fd1:String,@Header("PSU-Resv-Field2") psu_fd2:String, @Body trandetails:InitPayReq):Call<ResponseBody>

    //Get Transaction Details
    @GET("api/AllTransactionListHistory")
    fun getTransactionDetails(@Query("merchant_id") merchantId:String, @Query ("user_id") user_id: String, @Query ("fromdate") from: String, @Query("todate") to:String,@Query ("type") type:String): Call<List<TransData>>

    @POST("api/UpdateRepresentativeProfile")
    fun updateUserProfile(@Body encryptedRequest: EncryptedRequest, @Header("PSU_Device_ID") PSU_Device_ID:String): Call<ResponseBody>

    @POST("api/upload")
    fun postPoster(@Query ("file") image: File, @Body poster:Posterdata):Call<ResponseBody>

    @GET("images/PosterList")
    fun getPosterlist(@Query ("merchant_user_id") merchantId:String): Call<List<PosterList>>

    @Multipart
    @POST("images/upload")
    fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("unit_name") unitName: RequestBody,
        @Part("date") date: RequestBody,
        @Part("unit_id") unitId: RequestBody,
        @Part("merchant_id") merchantId: RequestBody,
        @Part("merchant_rep_id") merchantRepId: RequestBody,
        @Part("frequency") frequency: RequestBody,
        @Part("from_date") fromDate: RequestBody,
        @Part("to_date") toDate: RequestBody
    ): Call<String>

    //Statement for merchant level
    @GET("api/AccountStatementMerchantWise")
    fun getStatementMerchantWise(@Query("merchant_id") merchantId:String,@Query("fromdate") fromDate:String,@Query("todate") toDate:String):Call<List<List<Any>>>
    //statement for unit wise
    @GET("api/AccountStatementMerchantUnitWise")
    fun getStatementUnitWise(@Query("merchant_id") merchantId:String,@Query("fromdate") fromDate:String,@Query("todate") toDate:String,@Query("unit_id") unitId:String):Call<List<List<Any>>>
    //statement for user wise
    @GET("api/AccountStatementUserWise")
    fun getStatementUserWise(@Query("merchant_id") merchantId:String,@Query("fromdate") fromDate:String,@Query("todate") toDate:String,@Query("unit_id") unitId:String,@Query("user_id") userId:String):Call<List<List<Any>>>
    //statement for device wise
    @GET("api/AccountStatementDeviceWise")
    fun getStatementDeviceWise(@Query("merchant_id") merchantId:String,@Query("fromdate") fromDate:String,@Query("todate") toDate:String,@Query("unit_id") unitId:String,@Query("device_id") deviceId:String):Call<List<List<Any>>>

    @GET("api/CheckDeviceId")
    fun checkDeviceId(@Query("device_id") deviceId:String):Call<ResponseBody>

    @GET("api/ws/infoForEveryScreen")
    fun getHelpInfoLimit(@Query("screen_id") ScreenId:String):Call<HelpInfoData>

    @GET("api/referenceMasterForDropdown")
    fun getReferenceCode(@Query ("ref_type") ref_type: String): Call<List<String>>

}