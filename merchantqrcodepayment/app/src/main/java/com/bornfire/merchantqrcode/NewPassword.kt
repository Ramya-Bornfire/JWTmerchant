package com.bornfire.merchantqrcode

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.*
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.LogoutClass
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewPassword : BaseActivity() {
    lateinit var newpswd: EditText
    lateinit var confrompswd: EditText
    lateinit var submitBtn: Button
    lateinit var userid: String
    lateinit var goback: String
    var visbility: Boolean = true
    var visbility1: Boolean = true
    var visbility3:Boolean=true
    lateinit var visibleImg1: ImageView
    lateinit var visibleImg2: ImageView
    lateinit var visibleImg3: ImageView
    val objectMapper = jacksonObjectMapper()
    val merchantdata = SharedMerchantDataObj.merchantData
    val userData = SharedUserDataObj.userData
    val usercategory = SharedusercatDataObj.UserCategory
    lateinit var passwordStrengthView: TextView
    lateinit var matchpswd: TextView
    lateinit var oldpswd: EditText
    lateinit var oldpswdtext:TextView
    lateinit var reqText:TextView

    lateinit var eyeimg1: ImageView
    lateinit var eyeimg2: ImageView
    lateinit var eyrimg3: ImageView
    val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!])(?=\\S+\$).{8,}\$")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)
        newpswd = findViewById(R.id.newpswd)
        confrompswd = findViewById(R.id.conpswd)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)

        val actionBar = supportActionBar
        if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            // Set the title
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.chg_password)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener(){
                HelpInfo.getInfo(this,"10")
            } }
        eyeimg1 = findViewById(R.id.oldpswdvisble)
        eyeimg2 = findViewById(R.id.pswdvisble)
        eyrimg3 = findViewById(R.id.pswdvisble1)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        submitBtn = findViewById(R.id.submitBtn)
        visibleImg1 = findViewById<ImageView>(R.id.pswdvisble)
        visibleImg2 = findViewById<ImageView>(R.id.pswdvisble1)
        visibleImg3=findViewById(R.id.oldpswdvisble)
        passwordStrengthView = findViewById(R.id.passwordStrength)
        oldpswd = findViewById(R.id.oldpswd)
        oldpswdtext = findViewById(R.id.oldpswdtext)
        matchpswd = findViewById(R.id.matchpswd)
        reqText = findViewById(R.id.reqText)
        val isTablet =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        userid = intent.getStringExtra("userId").toString()
        goback = intent.getStringExtra("GoBack").toString()
        if (changeOrNew()) {
            oldpswd.visibility = View.GONE
            oldpswdtext.visibility = View.GONE
            visibleImg3.visibility = View.GONE
            reqText.visibility = View.GONE
        }
        newpswd.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                val strength = getPasswordStrength(password)
                updatePasswordStrengthView(strength, passwordStrengthView)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        confrompswd.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                matchpswd.text =
                    if (newpswd.text.toString() == password) "" else "Password Mismatch"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        submitBtn.setOnClickListener {
            submitBtn.isEnabled=false
            val newPassword = newpswd.text.toString()
            if(validate()) {
                if (NetworkUtils.isNetworkAvailable(this)) {
                    if (usercategory?.user_category == "Representative") {
                        if(changeOrNew()){
                            setMerNewPswd(newPassword,userid)
                        }
                        else{
                            resetPswdMerchant(newPassword, userid)
                        } }
                    else{
                        if(changeOrNew()){
                            setUserNewPswd(newPassword,userid)
                        }
                        else{
                            resetPswdUser(newPassword, userid)
                        }
                    }
                }
                else {
                    NetworkUtils.NoInternetAlert(this)
                }
            }
        }
        eyeimg1.setOnClickListener {
            if (visbility3 == true) {
                oldpswd.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                visbility3 = false
                visibleImg3.setImageResource(R.drawable.ic_visible)
            } else {
                oldpswd.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                visbility3 = true
                visibleImg3.setImageResource(R.drawable.ic_visibility_off)
            }
        }
        eyeimg2.setOnClickListener {
            if (visbility == true) {
                newpswd.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                visbility = false
                visibleImg1.setImageResource(R.drawable.ic_visible)
            } else {
                newpswd.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                visbility = true
                visibleImg1.setImageResource(R.drawable.ic_visibility_off)
            }
        }
        eyrimg3.setOnClickListener {
            if (visbility == true) {
                newpswd.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                visbility = false
                visibleImg1.setImageResource(R.drawable.ic_visible)
            } else {
                newpswd.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                visbility = true
                visibleImg1.setImageResource(R.drawable.ic_visibility_off)
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(changeOrNew()){
                    logoutId()
                    val intent = Intent(this@NewPassword, LoginActivity::class.java)
                    startActivity(intent)
                }
                else{
                    val intent = Intent(this@NewPassword, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
    private fun validate():Boolean{
        val password = newpswd.text.toString()
        val conpswd = confrompswd.text.toString()
        if(!changeOrNew()){
            if(oldpswd.text.toString().length==0){
                AlertDialogBox().showDialog(this@NewPassword, "Please enter your old password.")
                return false
            }
            if(password.isNotEmpty()){
                if(password==oldpswd.text.toString()){
                    AlertDialogBox().showDialog(this@NewPassword, "New password cannot be same as old password.")
                    return false
                }
            }
        }
        if (password.length == 0){
            AlertDialogBox().showDialog(this@NewPassword, "Please enter the password")
            return false
        }
        if (password.length < 8){
            AlertDialogBox().showDialog(this@NewPassword, "Password must be minimum eight characters")
            return false
        }
        if (password.matches(passwordPattern)){
            if(conpswd.length==0){
                AlertDialogBox().showDialog(this@NewPassword, "Please enter the confirm Password")
                return false }
        }
        else {
            AlertDialogBox().showDialog(this@NewPassword, "Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character, and be at least 8 characters long.")
            return false
        }
        if (password != conpswd) {
            AlertDialogBox().showDialog(this@NewPassword, "Confirm password does not match with new password. Please make sure both passwords are the same.")
            return false
        }

      return true
    }
    private fun changeOrNew():Boolean{
        if(goback?.takeIf { it != "null" } != null){
          return true
        }
        return false
    }
    private fun updatePassword(
        pswd: String,
        merchantId: String,
        updatePasswordFunction: (EncryptedRequest, String) -> Call<ResponseBody>,
        successMessage: String,
        failureMessage: String
    ) {
        val pswdData = PasswordData(merchantId, "", pswd)
        val jsonString = objectMapper.writeValueAsString(pswdData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = updatePasswordFunction(encryptedRequest, psuDeviceID)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
              //      print(loginResponse)
                    //      println("Password updated successfully")
                    showImageAlertDialog()

                } else {
                    Toast.makeText(this@NewPassword, "$failureMessage: ${response.code()}", Toast.LENGTH_LONG).show()
                  //  println("Failed to update Password: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  println("Failed to update user data: ${t.message}")
                Toast.makeText(this@NewPassword, "Something went wrong at the server end", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun setMerNewPswd(pswd: String, merchantId: String) {
        updatePassword(pswd, merchantId, ApiClient.apiService::updateRepNewPassword,
            "Password updated successfully for merchant.",
            "Failed to update password for merchant"
        )
    }
    private fun setUserNewPswd(pswd: String, merchantId: String) {
        updatePassword(pswd,merchantId, ApiClient.apiService::updateUserNewPassword, "Password updated successfully for user.",
            "Failed to update password for user"
        )
    }
    override fun onSupportNavigateUp(): Boolean {
        if(changeOrNew()){
            logoutId()
            val intent = Intent(this@NewPassword, LoginActivity::class.java)
            startActivity(intent)
        }
        else{
            val intent = Intent(this@NewPassword, MainActivity::class.java)
            startActivity(intent)
        }
        return true
    }
    private fun resetPassword(type: String, id: String, pswd: String) {
        val pswdData = PasswordData(id, oldpswd.text.toString(), pswd)
        val jsonString = objectMapper.writeValueAsString(pswdData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)

        val call = when (type) {
            "user" -> ApiClient.apiService.resetPasswordUser(encryptedRequest, psuDeviceID)
            "merchant" -> ApiClient.apiService.resetPasswordMerchant(encryptedRequest, psuDeviceID)
            else -> throw IllegalArgumentException("Invalid type")
        }

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val decryptedResponse = Encryption.decrypt(responseString, psuDeviceID)
                        val loginResponse = objectMapper.readValue(decryptedResponse, LoginforTabResponse::class.java)

                        if (loginResponse.status == "Success") {
                            showImageAlertDialog()
                        } else {
                            AlertDialogBox().showDialog(this@NewPassword, "Your old password is incorrect")
                        }
                    } else {
                        showImageAlertDialog()
                    }
                    //println("User data updated successfully")
                } else {
                    Toast.makeText(this@NewPassword, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Failed to update user data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  println("Failed to update user data: ${t.message}")
                Toast.makeText(this@NewPassword, "Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
            }
        })
    }
    // Usage for user
    private fun resetPswdUser(pswd: String, userid: String) {
        resetPassword("user", userid, pswd) }
    // Usage for merchant
    private fun resetPswdMerchant(pswd: String, merchantid: String) {
        resetPassword("merchant", merchantid, pswd) }
    private fun showImageAlertDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.activity_success_dialog, null)
        val messageTextView = view.findViewById<TextView>(R.id.textview)
        messageTextView.text = "Password Changed Successfully" // Set the message text
        builder.setCancelable(false)
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
            logoutId()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }


    private fun updatePasswordStrengthView(strength: PasswordStrength, textView: TextView) {
        when (strength) {
            PasswordStrength.WEAK -> {
                textView.text = "Weak"
                textView.setTextColor(ContextCompat.getColor(this, R.color.red))
            }

            PasswordStrength.MEDIUM -> {
                textView.text = "Medium"
                textView.setTextColor(ContextCompat.getColor(this, R.color.bk1))
            }

            PasswordStrength.STRONG -> {
                textView.text = "Strong"
                textView.setTextColor(ContextCompat.getColor(this, R.color.green))
            }
        }
    }
    private fun getPasswordStrength(password: String): PasswordStrength {
        var strength = PasswordStrength.WEAK
        if (password.length >= 8) {
            strength = PasswordStrength.MEDIUM
            if (password.matches(".*[0-9].*".toRegex()) &&
                password.matches(".*[a-z].*".toRegex()) &&
                password.matches(".*[A-Z].*".toRegex()) &&
                password.matches(".*[!@#\$%^&*(),.?\":{}|<>].*".toRegex())
            ) {
                strength = PasswordStrength.STRONG
            }
        }
        return strength
    }
    private fun logoutId() {
            if (usercategory?.user_category == "Representative") {
                merchantdata?.merchant_rep_id?.let{userId->
                    LogoutClass.LogoutforTab(userId)
                }
            }
            else{
                userData?.user_id?.let { userId ->
                    LogoutClass.LogoutforMobile(userId)
                }
            }
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
enum class PasswordStrength { WEAK, MEDIUM, STRONG }
