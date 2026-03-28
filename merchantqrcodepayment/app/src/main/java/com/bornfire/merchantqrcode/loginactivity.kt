package com.bornfire.merchantqrcode

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.*
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Service.StaticSoundBoxService
import com.bornfire.merchantqrcode.Utils.LoginSessionHandling.getIPAddress
import com.bornfire.merchantqrcode.Utils.LoginSessionHandling.getOSVersion
import com.bornfire.merchantqrcode.Utils.NullCheck
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.bornfire.merchantqrcode.retrofit.Encryption.getAndroidId
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.scottyab.rootbeer.RootBeer
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketException
import java.util.*

class LoginActivity : AppCompatActivity(){

    lateinit var userId: EditText
    lateinit var password: EditText
    private lateinit var signInBtn: Button
    private lateinit var forgetPassword: TextView
    val objectMapper = jacksonObjectMapper()
    lateinit var imageServices:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginactivity)
        startAppMonitorService()
        userId = findViewById(R.id.edit_user_name)
        password = findViewById(R.id.edit_password)
        signInBtn = findViewById(R.id.signupbtn)
        forgetPassword = findViewById(R.id.textforget)

        userId.text.clear()
        password.text.clear()

        imageServices = findViewById(R.id.image_services)
        imageServices.setOnClickListener{
            showResponseDialog(getAndroidId(this))
            //logoutAndCloseApp()
        }
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        val checkBoxVisible = findViewById<CheckBox>(R.id.checkVisible)
        checkBoxVisible.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        forgetPassword.setOnClickListener{
            val intent = Intent(this,forgetpasswordactivity::class.java)
            startActivity(intent)
        }
        signInBtn.setOnClickListener{
            validation()
        }

        if (isDeviceRooted()|| isDeviceCompromised(this) || isUsingVPN() || isUsingProxy(this)||checkFridaFiles()) {
            Toast.makeText(this, "Security issue detected! Exiting...", Toast.LENGTH_LONG).show()
            finishAffinity() // Close the app
        } else {
            // Toast.makeText(this, "Device is secure", Toast.LENGTH_SHORT).show()
            checkDeviceId()
        }

        if (isDeviceCompromised(this)) {
            Toast.makeText(this, "Rooted or Frida detected! Exiting...", Toast.LENGTH_LONG).show()
            finishAffinity() // Close app
        }
    }
    fun checkFridaFiles(): Boolean {
        val paths = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/frida-agent.so",
            "/data/local/tmp/libgadget.so",
            "/system/bin/frida-server",
            "/system/xbin/frida-server"
        )

        paths.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                return true
            }
        }
        return false
    }

    fun isFridaRunning(): Boolean {
        val fridaProcesses = listOf("frida", "gum-js", "gadget")
        val mapsFile = File("/proc/self/maps")

        if (mapsFile.exists()) {
            val content = mapsFile.readText()
            fridaProcesses.forEach { process ->
                if (content.contains(process)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Check if the device is connected to a VPN.
     */
    private fun isUsingVPN(): Boolean {
        try {
            val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in networkInterfaces) {
                if (networkInterface.isUp && (networkInterface.name.contains("tun") ||
                            networkInterface.name.contains("ppp") ||
                            networkInterface.name.contains("pptp"))
                ) {
                    return true
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Check if the device is using a proxy.
     */
    private fun isUsingProxy(context: Context): Boolean {
        return try {
            val proxyAddress = System.getProperty("http.proxyHost")
            val proxyPort = System.getProperty("http.proxyPort")?.toIntOrNull()
            if (proxyAddress != null && proxyPort != null && proxyPort != -1) {
                return true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (networkCapabilities != null && networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI)) {
                    val proxyInfo = connectivityManager.getDefaultProxy()
                    return proxyInfo != null
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    private fun isDeviceRooted(): Boolean {
        val rootBeer = RootBeer(this)
        return rootBeer.isRooted
    }
    fun isRootBinaryPresent(): Boolean {
        val rootBinaries = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/xbin/su"
        )

        return rootBinaries.any { File(it).exists() }
    }
    fun isRootManagementAppInstalled(context: Context): Boolean {
        val rootApps = listOf(
            "com.noshufou.android.su", "eu.chainfire.supersu",
            "com.koushikdutta.superuser", "com.zachspong.temprootremovejb"
        )

        return rootApps.any {
            try {
                context.packageManager.getPackageInfo(it, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
    fun isDangerousPropsSet(): Boolean {
        val props = listOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0"
        )

        return props.any { (key, expected) ->
            try {
                val process = Runtime.getRuntime().exec("getprop $key")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val value = reader.readLine()
                value == expected
            } catch (e: Exception) {
                false
            }
        }
    }
    fun hasTestKeys(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }
    fun canExecuteSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            process.inputStream.bufferedReader().readLine() != null
        } catch (e: Exception) {
            false
        }
    }
    fun isFridaPortOpen(): Boolean {
        val ports = listOf(27042, 27043)

        return ports.any {
            try {
                Socket("127.0.0.1", it).use { _ -> true }
            } catch (e: Exception) {
                false
            }
        }
    }
    fun isFridaInjected(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("cat /proc/net/tcp")
            process.inputStream.bufferedReader().useLines { lines ->
                lines.any { it.contains("27042") || it.contains("27043") }
            }
        } catch (e: Exception) {
            false
        }
    }
    fun isDeviceCompromised(context: Context): Boolean {
        return isRootBinaryPresent() ||
                isRootManagementAppInstalled(context) ||
                isDangerousPropsSet() ||
                hasTestKeys() ||
                canExecuteSuCommand() ||
                isFridaRunning() ||
                isFridaPortOpen() ||
                isFridaInjected()
    }
    private fun loginAndroid(userId: String, password: String) {
        val loginTabData = LoginData(userId,password,getIPAddress(this).toString(),getAndroidId(this),"MOBILE",getOSVersion(),getAppVersion().toString())
        val jsonString = objectMapper.writeValueAsString(loginTabData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.loginAndroid(encryptedRequest, psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    try {
                        val loginEntityJsonString: String
                        val encryptedResponse = response.body()?.string()
                        if (encryptedResponse != null) {
                            val decryptedResponse =
                                Encryption.decrypt(encryptedResponse, psuDeviceID)
                            //   println("Decrypted response: $decryptedResponse")
                            val loginResponse = objectMapper.readValue(
                                decryptedResponse,
                                LoginforTabResponse::class.java
                            )
                            if (loginResponse.status == "Success") {
                                val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("jwt_token", loginResponse.token)
                                    apply()
                                }
                                loginEntityJsonString = loginResponse.message
                                if (loginEntityJsonString.contains("LoginEntity")) {

                                    val messageParts = loginEntityJsonString.split(",")

                                    val merchantRepId =
                                        messageParts.find { it.contains("merchant_rep_id") }
                                            ?.split("=")?.get(1)?.trim()
                                    val merchantRepName =
                                        messageParts.find { it.contains("mer_representative_name") }
                                            ?.split("=")?.get(1)?.trim()
                                    val merchantUserId =
                                        messageParts.find { it.contains("merchant_user_id") }
                                            ?.split("=")?.get(1)?.trim()
                                    val merchantName =
                                        messageParts.find { it.contains("merchant_name") }
                                            ?.split("=")?.get(1)?.trim()
                                    val merchantLegalUserId =
                                        messageParts.find { it.contains("merchant_legal_user_id") }
                                            ?.split("=")?.get(1)?.trim()
                                    val merchantCorporateName =
                                        messageParts.find { it.contains("merchant_corporate_name") }
                                            ?.split("=")?.get(1)?.trim()
                                    val passwordResponse =
                                        messageParts.find { it.contains("password") }?.split("=")
                                            ?.get(1)?.trim()
                                    val passwordExpDate1 =
                                        messageParts.find { it.contains("password_expiry_date") }
                                            ?.split("=")?.get(1)?.trim()
                                    val passwordLife =
                                        messageParts.find { it.contains("password_life") }
                                            ?.split("=")?.get(1)?.trim()
                                    val accExpiryDate =
                                        messageParts.find { it.contains("account_expiry_date") }
                                            ?.split("=")?.get(1)?.trim()
                                    val userDisableFlag =
                                        messageParts.find { it.contains("user_disable_flag") }
                                            ?.split("=")?.get(1)?.trim()
                                    val userDisableFromDate =
                                        messageParts.find { it.contains("user_disable_from_date") }
                                            ?.split("=")?.get(1)?.trim()
                                    val userDisableToDate =
                                        messageParts.find { it.contains("user_disable_to_date") }
                                            ?.split("=")?.get(1)?.trim()
                                    val delFlag =
                                        messageParts.find { it.contains("del_flag") }?.split("=")
                                            ?.get(1)?.trim()
                                    val userStatus =
                                        messageParts.find { it.contains("user_status") }
                                            ?.split("=")?.get(1)?.trim()
                                    val loginStatus =
                                        messageParts.find { it.contains("login_status") }
                                            ?.split("=")?.get(1)?.trim()
                                    val loginChannel =
                                        messageParts.find { it.contains("login_channel") }
                                            ?.split("=")?.get(1)?.trim()
                                    val mobileNo =
                                        messageParts.find { it.contains("mobile_no") }?.split("=")
                                            ?.get(1)?.trim()
                                    val alterMobileNo =
                                        messageParts.find { it.contains("alternate_mobile_no") }
                                            ?.split("=")?.get(1)?.trim()
                                    val email = messageParts.find { it.contains("email_address") }
                                        ?.split("=")?.get(1)?.trim()
                                    val alterEmail =
                                        messageParts.find { it.contains("alternate_email_id") }
                                            ?.split("=")?.get(1)?.trim()
                                    val noOfConcurrentUsers =
                                        messageParts.find { it.contains("no_of_concurrent_users") }
                                            ?.split("=")?.get(1)?.trim()
                                    val noOfActiveDevice =
                                        messageParts.find { it.contains("no_of_active_devices") }
                                            ?.split("=")?.get(1)?.trim()
                                    val entryUser =
                                        messageParts.find { it.contains("entry_user") }?.split("=")
                                            ?.get(1)?.trim()
                                    val modifyUser =
                                        messageParts.find { it.contains("modify_user") }
                                            ?.split("=")?.get(1)?.trim()
                                    val verifyUser =
                                        messageParts.find { it.contains("verify_user") }
                                            ?.split("=")?.get(1)?.trim()
                                    val entryTime =
                                        messageParts.find { it.contains("entry_time") }?.split("=")
                                            ?.get(1)?.trim()
                                    val modifyTime =
                                        messageParts.find { it.contains("modify_time") }
                                            ?.split("=")?.get(1)?.trim()
                                    val verifyTime =
                                        messageParts.find { it.contains("verify_time") }
                                            ?.split("=")?.get(1)?.trim()
                                    val unitId =
                                        messageParts.find { it.contains("unit_id") }?.split("=")
                                            ?.get(1)?.trim()
                                    val unitName =
                                        messageParts.find { it.contains("unit_name") }?.split("=")
                                            ?.get(1)?.trim()
                                    val unitType =
                                        messageParts.find { it.contains("unit_type") }?.split("=")
                                            ?.get(1)?.trim()
                                    val makerChecker =
                                        messageParts.find { it.contains("maker_or_checker") }
                                            ?.split("=")?.get(1)?.trim()
                                    val entryFlag =
                                        messageParts.find { it.contains("entry_flag") }?.split("=")
                                            ?.get(1)?.trim()
                                    val modifyFlag =
                                        messageParts.find { it.contains("modify_flag") }
                                            ?.split("=")?.get(1)?.trim()
                                    val pwLog =
                                        messageParts.find { it.contains("pwlog_flg") }?.split("=")
                                            ?.get(1)?.trim()
                                    val userCategory =
                                        messageParts.find { it.contains("user_category") }?.split("=")
                                            ?.get(1)?.trim()
                                    val authenticationFlag=messageParts.find { it.contains("authentication_flg") }?.split("=")
                                        ?.get(1)?.trim()
                                    val countryCode=messageParts.find { it.contains("countrycode") }?.split("=")
                                        ?.get(1)?.trim()
                                    val sharedUserCategory=usercat(
                                        user_category = userCategory
                                    )
                                    SharedusercatDataObj.UserCategory = sharedUserCategory
                                    val shareMerchantData = SharedMerchantData(
                                        merchant_rep_id = merchantRepId ?: "",
                                        mer_representative_name = merchantRepName ?: "",
                                        merchant_user_id = merchantUserId ?: "",
                                        merchant_name = merchantName ?: "",
                                        merchant_legal_user_id = merchantLegalUserId ?: "",
                                        merchant_corporate_name = merchantCorporateName ?: "",
                                        password = passwordResponse ?: "",
                                        password_expiry_date = passwordExpDate1 ?: "",
                                        password_life = passwordLife ?: "",
                                        account_expiry_date = accExpiryDate ?: "",
                                        user_disable_flag = userDisableFlag ?: "",
                                        user_disable_from_date = userDisableFromDate,
                                        user_disable_to_date = userDisableToDate,
                                        del_flag = delFlag ?: "",
                                        user_status = userStatus ?: "",
                                        login_status = loginStatus ?: "",
                                        login_channel = loginChannel ?: "",
                                        mobile_no = mobileNo ?: "",
                                        alternate_mobile_no = alterMobileNo?: "",
                                        email_address = email ?: "",
                                        alternate_email_id = NullCheck.getValidText(alterEmail),
                                        no_of_concurrent_users = noOfConcurrentUsers?.toIntOrNull() ?: 0,
                                        no_of_active_devices = noOfActiveDevice?.toIntOrNull() ?: 0,
                                        entry_user = entryUser,
                                        modify_user = modifyUser,
                                        verify_user = verifyUser,
                                        entry_time = entryTime,
                                        modify_time = modifyTime,
                                        verify_time = verifyTime,
                                        unit_id = NullCheck.getValidText(unitId),
                                        unit_type = NullCheck.getValidText(unitType),
                                        unit_name = NullCheck.getValidText(unitName),
                                        maker_or_checker = NullCheck.getValidText(makerChecker),
                                        entry_flag = NullCheck.getValidText(entryFlag),
                                        modify_flag = NullCheck.getValidText(modifyFlag),
                                        pwlog_flg = NullCheck.getValidText(pwLog),
                                        authenticationflg=NullCheck.getValidText(authenticationFlag),
                                        countrycode=NullCheck.getValidText(countryCode)
                                    )
                                    SharedMerchantDataObj.merchantData = shareMerchantData
                                    if(authenticationFlag == "Y"){
                                        nextScreen()
                                    }
                                    else{

                                        moveToAuthenticationScreen(merchantRepId,merchantRepName,password,mobileNo,email)
                                    }

                                } else {
                                    if (loginEntityJsonString.contains("UserManagementEntity")) {
                                        val userInfoString = loginEntityJsonString.substring(
                                            loginEntityJsonString.indexOf("[") + 1,
                                            loginEntityJsonString.lastIndexOf("]")
                                        )
                                        val keyValuePairs = userInfoString.split(", ")
                                        val map = keyValuePairs.associate {
                                            val (key, value) = it.split("=")
                                            key.trim() to value.trim() // Trim to remove any leading or trailing spaces
                                        }
                                        val merchantUserId = map["merchant_user_id"]
                                        val merchantName = map["merchant_name"]
                                        val merchantLegalUserId = map["merchant_legal_user_id"]
                                        val merchantCorporateName =
                                            map["merchant_corporate_name"]
                                        val employeeUserId = map["user_id"]
                                        val userName = map["user_name"]
                                        val userDesignation = map["user_designation"]
                                        val userRole = map["user_role"]
                                        val password1 = map["password1"]
                                        val passwordExpDate1 = map["password_expiry_date1"]
                                        val passwordLife = map["password_life1"]
                                        val accExpiryDate = map["account_expiry_date1"]
                                        val makeOrChecker = map["make_or_checker"]
                                        val supervisorFlag = map["supervisor_flag"]
                                        val userDisableFlag = map["user_disable_flag1"]
                                        val userDisableFromDate =
                                            map["user_disable_from_date1"]
                                        val userDisableToDate = map["user_disable_to_date1"]
                                        val delFlag = map["del_flag1"]
                                        val userStatus = map["user_status1"]
                                        val loginStatus = map["login_status1"]
                                        val loginChannel = map["login_channel1"]
                                        val mobileNo = map["mobile_no1"]
                                        val alterMobileNo= map["alternate_mobile_no1"]
                                        val email = map["email_address1"]
                                        val alterEmail = map["alternate_email_id1"]
                                        val defaultDeviceId = map["default_device_id"]
                                        val alterDeviceId1 = map["alternative_device_id1"]
                                        val entryUser = map["entry_user"]
                                        val modifyUser = map["modify_user"]
                                        val verifyUser = map["verify_user"]
                                        val entryTime = map["entry_time"]
                                        val modifyTime = map["modify_time"]
                                        val verifyTime = map["verify_time"]
                                        val alterDeviceId2 = map["alternative_device_id2"]
                                        val unitId = map["unit_id_u"] ?:""
                                        val unitType = map["unit_type_u"]
                                        val unitName = map["unit_name_u"]
                                        val entryFlag = map["entry_flag"]
                                        val modifyFlag = map["modify_flag"]
                                        val userCategory = map["user_category"]
                                        val authenticationFlag = map["authentication_flg"]
                                        val countryCode = map["countrycode"]
                                        val sharedUserCategory = usercat(user_category = userCategory)
                                        SharedusercatDataObj.UserCategory = sharedUserCategory
                                        val shareUserData = SharedUserData(merchant_user_id = merchantUserId!!, merchant_name = merchantName!!,
                                            merchant_legal_user_id = merchantLegalUserId!!, merchant_corporate_name = merchantCorporateName!!, user_id = employeeUserId!!,
                                            user_name = userName!!, user_designation = userDesignation!!, user_role = userRole!!,
                                            password1 = password1!!, password_expiry_date1 = passwordExpDate1!!, password_life1 = passwordLife!!,
                                            account_expiry_date1 = accExpiryDate!!, make_or_checker = makeOrChecker!!, supervisor_flag = supervisorFlag!!,
                                            user_disable_flag1 = userDisableFlag!!, user_disable_from_date1 = userDisableFromDate!!, user_disable_to_date1 = userDisableToDate!!,
                                            del_flag1 = delFlag!!, user_status1 = userStatus!!, login_status1 = loginStatus!!,
                                            login_channel1 = loginChannel!!, mobile_no1 = mobileNo!!, alternate_mobile_no1 = alterMobileNo!!,
                                            email_address1 = email!!, alternate_email_id1 = alterEmail!!, default_device_id = defaultDeviceId!!,
                                            alternative_device_id1 = alterDeviceId1!!, entry_user = entryUser!!, modify_user = modifyUser!!,
                                            verify_user = verifyUser!!, entry_time = entryTime!!, modify_time = modifyTime!!,
                                            verify_time = verifyTime!!, alternative_device_id2 = alterDeviceId2!!, unit_id = unitId, unit_name = unitName!!,
                                            unit_type = unitType!!, entryFlag!!, modifyFlag!!, authenticationflag = authenticationFlag!!, countrycode = countryCode!!)
                                        SharedUserDataObj.userData = shareUserData
                                        if (authenticationFlag == "Y") {
                                            nextScreen()
                                        } else {
                                            moveToAuthenticationScreen(userId, userName, password1, mobileNo, email)
                                        }
                                    }
                                    else{
                                        val message = loginResponse.message
                                        if (message == "Your password has expired. Please reset it to continue.") {
                                            showYesNoDialog(userId)
                                        } else {
                                            showResponseDialog(message)
                                        }
                                    }
                                }

                            }
                            else{
                                val message = loginResponse.message
                                if (message == "Your password has expired. Please reset it to continue.") {
                                    showYesNoDialog(userId)
                                } else {
                                    showResponseDialog(message)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        //   e.printStackTrace()
                        //  println("Decryption failed: ${e.message}")
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    // println("Error: ${response.code()}")
                    // println("Response is not successful: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //  t.printStackTrace()
                showResponseDialog("Something Went Wrong at Server End")
                //  println("API call failed: ${t.message}")
            }
        })
    }
    private fun checkDeviceId() {
        val call  = ApiClient.apiService.checkDeviceId(getAndroidId(this))
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    if (responseBody == "Device Found") {
                        imageServices.visibility = View.INVISIBLE
                        if (NetworkUtils.isNetworkAvailable(this@LoginActivity)) {
                            if(userId.text.toString().isNotEmpty()||password.text.toString().isNotEmpty()) {
                                loginAndroid(userId.text.toString(), password.text.toString())
                            }
                        } else {
                            NetworkUtils.NoInternetAlert(this@LoginActivity)
                        }
                    }
                    else{
                        imageServices.visibility=View.VISIBLE
                        AlertDialogBox().showDialog(this@LoginActivity,"Device not register. Please Contact Admin")
                        userId.text.clear()
                        password.text.clear()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    //    println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //  println("Network error: ${t.message}")
                Toast.makeText(this@LoginActivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun nextScreen() {
        Toast.makeText(this,R.string.pro_suc,Toast.LENGTH_LONG).show()
        val intent2 = Intent(this, StaticSoundBoxService::class.java)
        startService(intent2)
        val intent = Intent(this, WelcomeScreen::class.java)
        startActivity(intent)

    }
    private fun moveToAuthenticationScreen(merchantUserId: String?, userNAme: String?, merchantPassword: String?, mobileNo: String?, email: String?) {
        Toast.makeText(this,"Proceeded Successfully to Two factor authentication",Toast.LENGTH_LONG).show()
        val intent = Intent(this, TwoFactorAuthentication::class.java)
        intent.putExtra("userId",merchantUserId)
        intent.putExtra("userName", userNAme)
        intent.putExtra("password",merchantPassword)
        intent.putExtra("mobNo",mobileNo)
        intent.putExtra("email",email)
        startActivity(intent)

    }
    private fun validation() {
        if (userId.length() == 0) {
            //    println("Please enter the user id")
            userId.error = "Enter user Id"
            return
        }
        if (password.length() == 0) {
            //  println("Enter the password")
            password.error = "Enter password"
            return
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            NetworkUtils.NoInternetAlert(this@LoginActivity)
            return
        }
        checkDeviceId()
    }

    override fun onResume() {
        super.onResume()
        userId.text.clear()
        password.text.clear()
    }
    private fun otpAndroid(merchantId: String) {
        NetworkUtils.showProgress(this)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val call = ApiClient.apiService.getOtpForAndroid(merchantId,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    NetworkUtils.hideProgress(this@LoginActivity)
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                        //    println("Decrypted response: $decryptedResponse")
                        val objectMapper = jacksonObjectMapper()
                        val loginResponse = objectMapper.readValue(
                            decryptedResponse,
                            LoginforTabResponse::class.java
                        )
                        val loginEntityJsonString = loginResponse?.message
                        val keyValuePairs =
                            loginEntityJsonString?.substringAfter("{")?.substringBefore("}")
                        val valuePairs = keyValuePairs?.split(",")
                        var mobileNumber: Long = 0
                        var otp = 0
                        for (pair in valuePairs!!) {
                            val (key, value) = pair.split("=")
                            when (key.trim()) {
                                "Mobile" -> mobileNumber = value.trim().toLong()
                                "OTP" -> otp = value.trim().toInt()
                            }
                        }
                        nextScreen(mobileNumber.toString(), otp.toString(), merchantId)
                    }
                }
                else {
                    NetworkUtils.hideProgress(this@LoginActivity)
                    //    println("Error: ${response.code()}")
                    Toast.makeText(this@LoginActivity,"Invalid User",Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // t.printStackTrace()
                Toast.makeText(this@LoginActivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()


            }
        })
    }
    private fun nextScreen(mob : String, otp : String,merchantId:String) {
        val intent = Intent(this,forgetotpactivity::class.java)
        intent.putExtra("Mobile",mob)
        intent.putExtra("OTP", otp)
        intent.putExtra("UserId",merchantId)
        startActivity(intent)
    }
    private fun showResponseDialog(response:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage(response)
        builder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }
    private fun showYesNoDialog(empId:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Password Expired")
        builder.setMessage("Please change the password")

        builder.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
            otpAndroid(empId)
        }
        builder.setNegativeButton("OK") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }
    private fun getAppVersion(): Pair<String?, Int?> {
        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            Pair(versionName, versionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            //  e.printStackTrace()
            Pair(null, null)
        }
    }
    private fun startAppMonitorService() {
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startForegroundService(serviceIntent)
    }

}