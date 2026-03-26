package com.bornfire.merchantqrcode.DetailScreen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.DataModel.UserData
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.AdminScreens.Usermanagement
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.DeviceData
import com.bornfire.merchantqrcode.DataModel.EncryptedRequest
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.UnitData
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.Utils.EditNonEdit
import com.bornfire.merchantqrcode.Utils.EditNonEdit.hideKeyboard
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.NullCheck
import com.bornfire.merchantqrcode.Utils.PhNumValidation
import com.bornfire.merchantqrcode.Utils.ReferenceCode
import com.bornfire.merchantqrcode.Utils.ReferenceCodeCallback
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.hbb20.CountryCodePicker
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class UserDetailsActivity : BaseActivity(), ReferenceCodeCallback {
    lateinit var editUserId: EditText
    lateinit var editUserName: EditText
    lateinit var editDefaultDeviceId: EditText
    lateinit var editDeviceId1: EditText
    lateinit var editDeviceId2: EditText
    lateinit var editmobileNum: EditText
    lateinit var editemail: EditText
    lateinit var edituserdisabledFromDate: EditText
    lateinit var editUserDisableToDate: EditText
    lateinit var editUserDesign: EditText
    lateinit var editUserRole: EditText
    lateinit var editUserMakerChecker: EditText
    lateinit var editUserAltMobNum: EditText
    lateinit var editUserAltEmail: EditText
    lateinit var editUnitId: EditText
    lateinit var editUnitName: EditText
    lateinit var editUnitType: EditText
    lateinit var verifybtn: Button
    lateinit var submitButton: Button
    lateinit var modFlag: String
    lateinit var entryFlag: String
    lateinit var modifyuser: String
    private lateinit var countryCodePicker: CountryCodePicker
    private lateinit var countryCodePicker1: CountryCodePicker
    val merchantdata = SharedMerchantDataObj.merchantData
    var alterMobNum: Long? = null
    lateinit var deviceList: List<DeviceData>
    lateinit var unitList: List<UnitData>
    private var fromDate: Calendar = Calendar.getInstance()
    private var toDate: Calendar = Calendar.getInstance()
    lateinit var formattedDate1: String
    lateinit var formattedDate2: String
    lateinit var merchantName: String
    lateinit var merchantid: String
    lateinit var headertext: TextView
    lateinit var countryCodeWithPlus: String
    lateinit var countryCodeWithPlus1: String
    private var mediaPlayer: MediaPlayer? = null
    var disFrom = ""
    var disTo = ""
    lateinit var back_img: ImageView
    lateinit var editBtn: ImageView
    private var isUserDataFetched = false
    private var lastUnitId: String = ""
    private lateinit var deleBtn: ImageView
    private lateinit var delUserId: String
    private lateinit var textunitid: TextView
    private lateinit var textemail: TextView
    private lateinit var textmobile: TextView
    private lateinit var userrole: TextView
    private lateinit var userdesign: TextView
    private lateinit var username: TextView
    val unitid = "UNIT ID"
    val emailid = "EMAIL"
    val mobilenumber = "MOBILE NUMBER"
    val role = "USER ROLE"
    val designation = "USER DESIGNATION"
    val nametext = "USER NAME"
    lateinit var ud_help_image:ImageView
    var screenid:String = "14"
    private lateinit var popupWindow: PopupWindow
    private lateinit var adapter: ArrayAdapter<String>
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        supportActionBar?.hide()
        val userId = intent.getStringExtra("userId")
        val entryUser = intent.getStringExtra("entryUser")
        entryFlag = intent.getStringExtra("entryFlag").toString()
        modFlag = intent.getStringExtra("mofFlag").toString()
        modifyuser = intent.getStringExtra("modifyUser").toString()
        ud_help_image = findViewById(R.id.ud_help_image)
        ud_help_image.setOnClickListener(){
            HelpInfo.getInfo(this,screenid)
        }
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (isTablet) { requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        else { requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        // Initialize TextView variables
        textunitid = findViewById(R.id.textunitid)
        textemail = findViewById(R.id.textemail)
        textmobile = findViewById(R.id.textmobilenumber)
        userrole = findViewById(R.id.textuserrole)
        userdesign = findViewById(R.id.textdesignation)
        username = findViewById(R.id.textusername)

        editUserId = findViewById(R.id.editUserId)
        editUserName = findViewById(R.id.editUserName)
        editDefaultDeviceId = findViewById(R.id.editDefaultDeviceId)
        editDeviceId1 = findViewById(R.id.editDeviceId1)
        editDeviceId2 = findViewById(R.id.editDeviceId2)
        editmobileNum = findViewById(R.id.mobnum)
        countryCodePicker = findViewById(R.id.country_code)
        countryCodePicker1 = findViewById(R.id.country_code1)
        editemail = findViewById(R.id.email1)
        edituserdisabledFromDate = findViewById(R.id.userdiableddatefrom)
        editUserDisableToDate = findViewById(R.id.userdisableddateto)
        editUserDesign = findViewById(R.id.editUserDesign)
        editUserRole = findViewById(R.id.editUserRole)
        editUserMakerChecker = findViewById(R.id.editUserMakerChecker)
        editUserAltMobNum = findViewById(R.id.editUserAltMobNum)
        editUserAltEmail = findViewById(R.id.editUserAltEmail)
        editUnitId = findViewById(R.id.editUnitId)
        editUnitName = findViewById(R.id.editUnitName)
        editUnitType = findViewById(R.id.editUnitType)
        back_img = findViewById(R.id.back_img)
        editBtn = findViewById(R.id.editableBtn)
        deleBtn = findViewById(R.id.deleBtn)
        editDefaultDeviceId.inputType = InputType.TYPE_NULL
        editDeviceId1.inputType = InputType.TYPE_NULL
        editDeviceId2.inputType = InputType.TYPE_NULL
        editUnitId.inputType = InputType.TYPE_NULL
        editUserDesign.setOnClickListener {
            hideKeyboard(it)
            fetchReferenceCode("UD", editUserDesign)
        }
        editUserRole.setOnClickListener {
            hideKeyboard(it)
            fetchReferenceCode("UR", editUserRole)
        }
        if (merchantdata?.pwlog_flg == "MERCHANT") {
            editDefaultDeviceId.setOnClickListener() {
                hideKeyboard(it)
                val editTextView = findViewById<EditText>(R.id.editDefaultDeviceId)
                // Check if Unit ID is provided
                if (editUnitId.text.toString().isEmpty()) {
                    AlertDialogBox().showDialog(this, "Please Choose Unit")
                } else {
                    // Fetch data if not already fetched or if the unit ID has changed
                    if (!isUserDataFetched) {
                        fetchUnitDeviceData(R.id.editDefaultDeviceId)
                    } else {
                        // Show the existing data or handle it appropriately
                        val userListData =
                            deviceList?.map { "${it.device_id} (${it.device_name})" }
                        if (!userListData.isNullOrEmpty()) {
                            showDeviceSelectionPopup(editTextView, userListData)
                        } else {
                            AlertDialogBox().showDialog(this, "No data available")
                        }
                    }
                }
            }
            editDeviceId1.setOnClickListener() {
                hideKeyboard(it)
                val editTextView = findViewById<EditText>(R.id.editDeviceId1)
                // Check if Unit ID is provided
                if (editUnitId.text.toString().isEmpty()) {
                    AlertDialogBox().showDialog(this, "Please Choose Unit")
                } else {
                    // Fetch data if not already fetched or if the unit ID has changed
                    if (!isUserDataFetched) {
                        fetchUnitDeviceData(R.id.editDeviceId1)
                    } else {
                        // Show the existing data or handle it appropriately
                        val userListData =
                            deviceList?.map { "${it.device_id} (${it.device_name})" }
                        if (!userListData.isNullOrEmpty()) {
                            showDeviceSelectionPopup(editTextView, userListData)
                        } else {
                            AlertDialogBox().showDialog(this, "No data available")
                        }
                    }
                }
            }
            editDeviceId2.setOnClickListener() {
                hideKeyboard(it)
                val editTextView = findViewById<EditText>(R.id.editDeviceId2)
                // Check if Unit ID is provided
                if (editUnitId.text.toString().isEmpty()) {
                    AlertDialogBox().showDialog(this, "Please Choose Unit")
                } else {
                    // Fetch data if not already fetched or if the unit ID has changed
                    if (!isUserDataFetched) {
                        fetchUnitDeviceData(R.id.editDeviceId2)
                    } else {
                        // Show the existing data or handle it appropriately
                        val userListData =
                            deviceList?.map { "${it.device_id} (${it.device_name})" }
                        if (!userListData.isNullOrEmpty()) {
                            showDeviceSelectionPopup(editTextView, userListData)
                        } else {
                            AlertDialogBox().showDialog(this, "No data available")
                        }
                    }
                }
            }
            editUnitId.setOnClickListener() {
                fetchUnitData(R.id.editUnitId) }
        }
        else{
            editUnitId.isEnabled = false
            editUnitId.setText(merchantdata?.unit_id)
            editUnitName.setText(merchantdata?.unit_name)
            editUnitType.setText(merchantdata?.unit_type)
        }
        EditNonEdit.SetCaps(editUserName)

        back_img.setOnClickListener {
            val intent = Intent(this,Usermanagement::class.java)
            startActivity(intent)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backToUM()
            }
        })
        deleBtn.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Delete")
                .setMessage("Are you sure you want to Delete the User?")
                .setPositiveButton("Ok") { dialog, which ->
                    calldeldialog()
                }.setNegativeButton("Cancel", null).show()
                .setCancelable(false)
            true
        }
        setEditTextsColor(
            editUserId, editUnitType, editUnitName)
        setEditTextsNonEditable(
            editUserId, editUnitType, editUnitName,editDefaultDeviceId,editDeviceId1,editDeviceId2)
        editBtn.setOnClickListener() {
            headertext.text = getString(R.string.upd_user_det)
            screenid = "14"
            setEditMode()
            setHint(
                editUserDesign,
                editUserRole,
                editDefaultDeviceId,
                editDeviceId1,
                editDeviceId2,
                editUnitId
            )
            countryCodePicker.setOnCountryChangeListener(){
                editmobileNum.text.clear() }
            countryCodePicker1.setOnCountryChangeListener(){
                editUserAltMobNum.text.clear() }
            submitButton.visibility = View.VISIBLE
            countryCodePicker.setOnCountryChangeListener {
                PhNumValidation.updateExpectedLength(
                    countryCodePicker.selectedCountryNameCode, editmobileNum
                )
            }
            countryCodePicker.isEnabled = true
            countryCodePicker1.isEnabled = true
            editmobileNum.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    PhNumValidation.updateExpectedLength(
                        countryCodePicker.selectedCountryNameCode, editmobileNum
                    )
                }
            })
            countryCodePicker1.setOnCountryChangeListener {
                PhNumValidation.updateExpectedLength(
                    countryCodePicker1.selectedCountryNameCode, editUserAltMobNum
                )
            }
            editUserAltMobNum.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    PhNumValidation.updateExpectedLength(
                        countryCodePicker1.selectedCountryNameCode, editUserAltMobNum)
                    if (s.isNullOrEmpty()) {
                        editUserAltMobNum.error = null
                        editUserAltMobNum.clearFocus()
                    }
                }
            })
            setEditTextsEditable(
                editUserName,
                editDefaultDeviceId,
                editDeviceId1,
                editDeviceId2,
                editmobileNum,
                editemail,
                editUserDesign,
                editUserRole,
                editUnitId,
                editUserAltMobNum,
                editUserAltEmail,
                editUserDisableToDate,
                edituserdisabledFromDate
            )
        }
        countryCodeWithPlus = countryCodePicker.selectedCountryCode
        countryCodeWithPlus1 = countryCodePicker1.selectedCountryCode
        if (merchantdata?.pwlog_flg.toString() == "MERCHANT") {
        } else {
            editUnitId.isEnabled = false
            editUnitId.setText(merchantdata?.unit_id.toString())
            editUnitName.setText(merchantdata?.unit_name.toString())
            editUnitType.setText(merchantdata?.unit_type.toString())
        }
        verifybtn = findViewById(R.id.verifyBtn)
        verifybtn.setOnClickListener() {
                if (entryFlag == "N") {
                    if (entryUser.toString() == merchantdata?.merchant_rep_id.toString()||modifyuser == merchantdata?.merchant_rep_id.toString()) {
                        Toast.makeText(this, "Same users cannot be verify", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        verifyapicall()
                    }
                } else {
                    if (modifyuser == merchantdata?.merchant_rep_id.toString()) {
                        Toast.makeText(this, "Same users cannot be verify", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        verifyapicall()
                    }
                }

        }
        edituserdisabledFromDate.setOnClickListener() {
            showDatePickerDialog(true)
        }
        editUserDisableToDate.setOnClickListener() {
            showDatePickerDialog(false)
        }
        val textWatcher: (EditText) -> Unit = { editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        textWatcher(editmobileNum)
        textWatcher(editUserAltMobNum)
        submitButton = findViewById(R.id.submitbtn)
        mediaPlayer = MediaPlayer.create(this, R.raw.iphone)
        supportActionBar?.title = "User Details"
        //check add or update
        val buttonName = intent.getStringExtra("Button")
        //Add Screen
        if (buttonName != null) {
            getUserId()
            screenid = "13"
            setHint(
                editUserDesign,
                editUserRole,
                editDefaultDeviceId,
                editDeviceId1,
                editDeviceId2,
                editUnitId
            )
            setEditTextsEditable(
                editUserDesign,editUserRole
            )
            headertext = findViewById(R.id.headertext)
            headertext.text = getString(R.string.add_user_det)
            setEditMode()
            countryCodePicker.setOnCountryChangeListener {
                PhNumValidation.updateExpectedLength(
                    countryCodePicker.selectedCountryNameCode, editmobileNum
                )
            }
            editmobileNum.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    PhNumValidation.updateExpectedLength(
                        countryCodePicker.selectedCountryNameCode, editmobileNum
                    )
                }
            })
            countryCodePicker1.setOnCountryChangeListener {
                PhNumValidation.updateExpectedLength(
                    countryCodePicker1.selectedCountryNameCode, editUserAltMobNum
                )
            }
            editUserAltMobNum.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    PhNumValidation.updateExpectedLength(
                        countryCodePicker1.selectedCountryNameCode, editUserAltMobNum
                    )
                }
            })
            editUserMakerChecker.setOnClickListener() {
                //showMakerChecker(editUserMakerChecker)
            }
            submitButton.setText(buttonName)
            submitButton.setOnClickListener() {
                if (validateFields()) {
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        postapicall()
                    } else {
                        NetworkUtils.NoInternetAlert(this)
                    }
                } else {

                }
            }
        }
        //Update or verify Screen
        else {
            Updatemethod(submitButton)
        }
        editUnitId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // When unit ID changes, reset the flag and device list
                if (s.toString() != lastUnitId) {
                    isUserDataFetched = false
                    deviceList = emptyList()
                    lastUnitId = s.toString()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun getUserId() {
        val call = ApiClient.apiService.getUserId(merchantdata?.merchant_user_id.toString())
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val response = response.body()
                    editUserId.setText(response.toString())
                }
                else {
                    Toast.makeText(this@UserDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                //    println("Failed to get user id: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
          //      Log.d("Get User Id", "Network error: ${t.message}")
            }
        })
    }
    private fun updateFromDate(date: Calendar) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        edituserdisabledFromDate.setText(dateFormat.format(date.time))
        val dateFormat1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate1 = dateFormat1.format(date.time)
        disFrom = parsedDate1
       // println(parsedDate1)

    }
    fun getSpannableStringWithRedStar(text: String): SpannableString {
        val spannable = SpannableString("$text *")
        spannable.setSpan(ForegroundColorSpan(Color.RED), spannable.length - 1, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }
    fun setTextViewMode() {
        textunitid.text = unitid
        textemail.text = emailid
        textmobile.text = mobilenumber
        userrole.text = role
        userdesign.text = designation
        username.text = nametext
    }
    fun setEditMode() {
        textunitid.text = getSpannableStringWithRedStar(unitid)
        textemail.text = getSpannableStringWithRedStar(emailid)
        textmobile.text = getSpannableStringWithRedStar(mobilenumber)
        userrole.text = getSpannableStringWithRedStar(role)
        userdesign.text = getSpannableStringWithRedStar(designation)
        username.text = getSpannableStringWithRedStar(nametext)
        editDefaultDeviceId.isEnabled = true
        editDeviceId1.isEnabled = true
        editDeviceId2.isEnabled = true
    }
    private fun updateToDate(date: Calendar) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        editUserDisableToDate.setText(dateFormat.format(date.time))
        val dateFormat1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate1 = dateFormat1.format(date.time)
        disTo = parsedDate1
        //println(parsedDate1)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun Updatemethod(btn: Button) {
        countryCodePicker.isEnabled = false
        countryCodePicker1.isEnabled = false
        setEditTextsNonEditable(
            editUserId,
            editUserName,
            editDefaultDeviceId,
            editDeviceId1,
            editDeviceId2,
            editmobileNum,
            editemail,
            editUserDesign,
            editUserRole,
            editUserMakerChecker,
            editUserAltMobNum,
            editUserAltEmail,
            editUnitId,
            editUnitName,
            editUnitType,
            editUserDisableToDate,
            edituserdisabledFromDate
        )
        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")
        val defaultDeviceId = intent.getStringExtra("defaultDeviceId")
        val deviceId1 = intent.getStringExtra("deviceId1")
        val deviceId2 = intent.getStringExtra("deviceId2")
        val pswd = intent.getStringExtra("password")
        val userDisFromDate = intent.getStringExtra("userDisabledFromDate")
        val userDisToDate = intent.getStringExtra("userDisabledToDate")
        val mobileNum = intent.getStringExtra("mobileNum")
        val email = intent.getStringExtra("email")
        val userDesign = intent.getStringExtra("designation")
        val userRole = intent.getStringExtra("role")
        val userAltMob = intent.getStringExtra("alterMobNo")
        val userAltEmail = intent.getStringExtra("alterEmail")
        val userMakerCheckr = intent.getStringExtra("makerChecker")
        val userUnitId = intent.getStringExtra("unitId")
        val userUnitName = intent.getStringExtra("unitName")
        val userUnitType = intent.getStringExtra("unitType")
        val entryflg = intent.getStringExtra("entry_flag")
        val modflg = intent.getStringExtra("modify_flag")
        val countrycode1 = intent.getStringExtra("countryCode")
        val countrycode2 = intent.getStringExtra("altcountryCode")
        delUserId = userId.toString()
        if (userDisFromDate != null) {
            if (userDisFromDate.isNotEmpty()) {
                val timestamp = userDisFromDate
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                val dateTime = LocalDateTime.parse(timestamp, formatter)
                val dateOnly = dateTime.toLocalDate()
                val formattedDate = dateOnly.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                edituserdisabledFromDate.setText(formattedDate.toString())
            }
        }
        if (userDisToDate != null) {
            if (userDisToDate.isNotEmpty()) {
                val timestamp1 = userDisToDate
                val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                val dateTime1 = LocalDateTime.parse(timestamp1, formatter1)
                val dateOnly1 = dateTime1.toLocalDate()
                val formattedDate1 = dateOnly1.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                editUserDisableToDate.setText(formattedDate1.toString())
            }
        }
        editUserId.setText(NullCheck.getValidText(userId))
        editUserName.setText(NullCheck.getValidText(userName))
        editDefaultDeviceId.setText(NullCheck.getValidText(defaultDeviceId))
        editDeviceId1.setText(NullCheck.getValidText(deviceId1))
        editDeviceId2.setText(NullCheck.getValidText(deviceId2))
        editmobileNum.setText(NullCheck.getValidText(mobileNum))
        editemail.setText(NullCheck.getValidText(email))
        editUserDesign.setText(NullCheck.getValidText(userDesign))
        editUserRole.setText(NullCheck.getValidText(userRole))
        editUserMakerChecker.setText(NullCheck.getValidText(userMakerCheckr))
        editUserAltMobNum.setText(NullCheck.getValidText(userAltMob))
        editUserAltEmail.setText(NullCheck.getValidText(userAltEmail))
        editUnitId.setText(NullCheck.getValidText(userUnitId))
        editUnitName.setText(NullCheck.getValidText(userUnitName))
        editUnitType.setText(NullCheck.getValidText(userUnitType))
        if (countrycode1 != null) {
            countryCodePicker.setCountryForPhoneCode(countrycode1.toInt())
        } else {
            //Toast.makeText(this,"NO COUNTRY",Toast.LENGTH_LONG).show()
        }
        if (countrycode2 != null) {
            countryCodePicker1.setCountryForPhoneCode(countrycode2.toInt())
        }
        deleBtn.visibility = View.VISIBLE
        editBtn.visibility = View.VISIBLE

        btn.setOnClickListener() {

            if (validateFields()) {
                if (NetworkUtils.isNetworkAvailable(this)) {
                    if (NullCheck.getValidText(userName) != editUserName.text.toString() || NullCheck.getValidText(
                            userDesign
                        ) != editUserDesign.text.toString() || NullCheck.getValidText(userRole) != editUserRole.text.toString() || NullCheck.getValidText(
                            userMakerCheckr
                        ) != editUserMakerChecker.text.toString() || NullCheck.getValidText(
                            defaultDeviceId
                        ) != editDefaultDeviceId.text.toString() || NullCheck.getValidText(
                            deviceId1
                        ) != editDeviceId1.text.toString() || NullCheck.getValidText(deviceId2) != editDeviceId2.text.toString() || NullCheck.getValidText(
                            mobileNum
                        ) != editmobileNum.text.toString() || NullCheck.getValidText(userAltMob) != editUserAltMobNum.text.toString() || NullCheck.getValidText(
                            email
                        ) != editemail.text.toString() || NullCheck.getValidText(userAltEmail) != editUserAltEmail.text.toString() || NullCheck.getValidText(
                            userDisFromDate
                        ) != edituserdisabledFromDate.text.toString() || NullCheck.getValidText(
                            userDisToDate
                        ) != editUserDisableToDate.text.toString() || NullCheck.getValidText(
                            userUnitId
                        ) != editUnitId.text.toString() || NullCheck.getValidText(userUnitName) != editUnitName.text.toString() || NullCheck.getValidText(
                            userUnitType
                        ) != editUnitType.text.toString()
                    ) {
                        updateapicall(userId)
                    } else {
                        AlertDialogBox().showDialog(this, "Cannot update beacause of no changes")
                    }
                } else {
                    NetworkUtils.NoInternetAlert(this)
                }
            } else {
                AlertDialogBox().showDialog(this, "Please fill the required field")
            }
        }
        if (entryFlag.equals("N") || modFlag.equals("Y")) {
            headertext = findViewById(R.id.headertext)
            headertext.text = getString(R.string.ver_user_det)
            setTextViewMode()
            screenid = "15"
            verifybtn.visibility = View.VISIBLE
            submitButton.visibility = View.GONE
            setEditTextsNonEditable(
                editUserId,
                editUserName,
                editDefaultDeviceId,
                editDeviceId1,
                editDeviceId2,
                editmobileNum,
                editemail,
                editUserDesign,
                editUserRole,
                editUserMakerChecker,
                editUserAltMobNum,
                editUserAltEmail,
                editUnitId,
                editUnitName,
                editUnitType,
                editUserDisableToDate,
                edituserdisabledFromDate
            )

        }
        else {
            screenid = "14"
            headertext = findViewById(R.id.headertext)
            headertext.text =getString(R.string.view_user_det)
            setTextViewMode()
            verifybtn.visibility = View.GONE
            submitButton.visibility = View.GONE

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun postapicall() {
        if (editUserAltMobNum.text.toString() == "") {
            alterMobNum = null
        } else {
            alterMobNum = editUserAltMobNum.text.toString().toLong()
        }
        if (edituserdisabledFromDate.text.isNotEmpty()) {
            val inputDate1 = edituserdisabledFromDate.text.toString()
            val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date1: LocalDate =
                LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
            formattedDate1 = date1.format(outputFormat1)
            edituserdisabledFromDate.setText(formattedDate1)
        }
        if (editUserDisableToDate.text.isNotEmpty()) {
            val inputDate1 = editUserDisableToDate.text.toString()
            val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date1: LocalDate =
                LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
            formattedDate2 = date1.format(outputFormat1)
            editUserDisableToDate.setText(formattedDate2)
        }
        merchantName = merchantdata!!.merchant_name.toString()
        merchantid = merchantdata!!.merchant_user_id.toString()
        val userData = UserData(
            merchant_user_id = merchantid,
            merchant_name = merchantName,
            merchant_legal_user_id = "",
            merchant_corporate_name = "",
            user_id = editUserId.text.toString(),
            user_name = editUserName.text.toString(),
            user_designation = editUserDesign.text.toString(),
            user_role = editUserRole.text.toString(),
            password1 = "",
            password_expiry_date1 = "",
            password_life1 = "",
            account_expiry_date1 = "",
            make_or_checker = editUserMakerChecker.text.toString(),
            supervisor_flag = "",
            user_disable_flag1 = "",
            user_disable_from_date1 = edituserdisabledFromDate.text.toString(),
            user_disable_to_date1 = editUserDisableToDate.text.toString(),
            del_flag1 = "",
            user_status1 = "",
            login_status1 = "",
            login_channel1 = "TAB",
            mobile_no1 = (editmobileNum.text.toString()),
            alternate_mobile_no1 = (editUserAltMobNum.text.toString()),
            email_address1 = editemail.text.toString(),
            alternate_email_id1 = editUserAltEmail.text.toString(),
            default_device_id = editDefaultDeviceId.text.toString(),
            alternative_device_id1 = editDeviceId1.text.toString(),
            entry_user = merchantdata.merchant_rep_id.toString(),
            modify_user = null,
            verify_user = null,
            entry_time = "",
            modify_time = "",
            verify_time = "",
            alternative_device_id2 = editDeviceId2.text.toString(),
            editUnitId.text.toString(),
            editUnitName.text.toString(),
            editUnitType.text.toString(),
            "N",
            "N",
            countryCodePicker.selectedCountryCode.toString(),
            "",
            countryCodePicker1.selectedCountryCode.toString(),
            "",
            "",
            ""
        )
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(userData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val de = Encryption.decrypt(encryptedText, psuDeviceID)
        val call = ApiClient.apiService.addUserData(encryptedRequest, psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val encryptedResponseBody = response.body()
                    if (encryptedResponseBody != null) {
                        try {
                         //   println(encryptedResponseBody)
                            callDialog()
                        } catch (e: Exception) {
                         //   e.printStackTrace()
                            Toast.makeText(
                                this@UserDetailsActivity,
                                "Decryption failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        //println("Error: Empty response body")
                    }
                } else {
                    Toast.makeText(this@UserDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    //println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //println("Network error: ${t.message}")
                Toast.makeText(
                    this@UserDetailsActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateapicall(userId: String?) {
        if (edituserdisabledFromDate.text.isNotEmpty()) {
            val inputDate1 = edituserdisabledFromDate.text.toString()
            val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date1: LocalDate =
                LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
            formattedDate1 = date1.format(outputFormat1)
            edituserdisabledFromDate.setText(formattedDate1)
        }
        if (editUserDisableToDate.text.isNotEmpty()) {
            val inputDate1 = editUserDisableToDate.text.toString()
            val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date1: LocalDate =
                LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
            formattedDate2 = date1.format(outputFormat1)
            editUserDisableToDate.setText(formattedDate2)
        }
        if (editUserAltMobNum.text.toString() == "") {
            alterMobNum = 0
        } else {
            alterMobNum = editUserAltMobNum.text.toString().toLong()
        }
        val id = userId
        val userData = UserData(
            "",
            merchant_name = "",
            merchant_legal_user_id = "",
            merchant_corporate_name = "",
            user_id = editUserId.text.toString(),
            user_name = editUserName.text.toString(),
            user_designation = editUserDesign.text.toString(),
            user_role = editUserRole.text.toString(),
            password1 = "",
            password_expiry_date1 = "",
            password_life1 = "",
            account_expiry_date1 = "",
            make_or_checker = editUserMakerChecker.text.toString(),
            supervisor_flag = "",
            user_disable_flag1 = "",
            user_disable_from_date1 = disFrom,
            user_disable_to_date1 = disTo,
            del_flag1 = "",
            user_status1 = "",
            login_status1 = "",
            login_channel1 = "",
            mobile_no1 = (editmobileNum.text.toString()),
            alternate_mobile_no1 = (editUserAltMobNum.text.toString()),
            email_address1 = editemail.text.toString(),
            alternate_email_id1 = editUserAltEmail.text.toString(),
            default_device_id = editDefaultDeviceId.text.toString(),
            alternative_device_id1 = editDeviceId1.text.toString(),
            entry_user = "",
            modify_user = merchantdata!!.merchant_rep_id.toString(),
            verify_user = null,
            entry_time = "",
            modify_time = "",
            verify_time = "",
            alternative_device_id2 = editDeviceId2.text.toString(),
            editUnitId.text.toString(),
            editUnitName.text.toString(),
            editUnitType.text.toString(),
            "N",
            "Y",
            countryCodePicker.selectedCountryCodeWithPlus,
            "",
            countryCodePicker1.selectedCountryCodeWithPlus,
            "",
            "",
            ""
        )

        val url = "api/UpdateUser?userId=${id}" // Replace "your_user_id" with the actual user ID
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(userData)

        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.updateUserData(url, encryptedRequest, psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                 //   println("User data updated successfully")
                    callDialog()
                } else {
                    Toast.makeText(this@UserDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                   // println("Failed to update user data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //println("Failed to update user data: ${t.message}")
                Toast.makeText(
                    this@UserDetailsActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun verifyapicall() {
        if (edituserdisabledFromDate.text.isNotEmpty()) {
            val inputDate1 = edituserdisabledFromDate.text.toString()
            val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date1: LocalDate =
                LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
            formattedDate1 = date1.format(outputFormat1)
            edituserdisabledFromDate.setText(formattedDate1)
        }
        if (editUserDisableToDate.text.isNotEmpty()) {
            val inputDate1 = editUserDisableToDate.text.toString()
            val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date1: LocalDate =
                LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
            formattedDate2 = date1.format(outputFormat1)
            editUserDisableToDate.setText(formattedDate2)
        }
        if (editUserAltMobNum.text.toString() == "") {
            alterMobNum = 0
        } else {
            alterMobNum = editUserAltMobNum.text.toString().toLong()
        }
        val userData = UserData(
            merchantdata?.merchant_user_id!!,
            merchant_name = "",
            merchant_legal_user_id = "",
            merchant_corporate_name = "",
            user_id = editUserId.text.toString(),
            user_name = editUserName.text.toString(),
            user_designation = editUserDesign.text.toString(),
            user_role = editUserRole.text.toString(),
            password1 = "",
            password_expiry_date1 = "",
            password_life1 = "",
            account_expiry_date1 = "",
            make_or_checker = "",
            supervisor_flag = "",
            user_disable_flag1 = "",
            user_disable_from_date1 = disFrom,
            user_disable_to_date1 = disTo,
            del_flag1 = "",
            user_status1 = "",
            login_status1 = "",
            login_channel1 = "",
            mobile_no1 = (editmobileNum.text.toString()),
            alternate_mobile_no1 = (editUserAltMobNum.text.toString()),
            email_address1 = editemail.text.toString(),
            alternate_email_id1 = editUserAltEmail.text.toString(),
            default_device_id = editDefaultDeviceId.text.toString(),
            alternative_device_id1 = editDeviceId1.text.toString(),
            entry_user = merchantdata?.merchant_rep_id.toString(),
            modify_user = "",
            verify_user = merchantdata?.merchant_rep_id.toString(),
            entry_time = "",
            modify_time = "",
            verify_time = null,
            alternative_device_id2 = editDeviceId2.text.toString(),
            editUnitId.text.toString(),
            editUnitName.text.toString(),
            editUnitType.text.toString(),
            "Y",
            "N",
            countryCodePicker.selectedCountryCode,
            "",
            countryCodePicker1.selectedCountryCode,
            "",
            "",
            ""
        )
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(userData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val de = Encryption.decrypt(encryptedText, psuDeviceID)
        val call = ApiClient.apiService.verifyUser(psuDeviceID, encryptedRequest)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val response = response.body()?.string()

                    val de = "93lYBOMhMHFutLZ14QKw1Q3fLx/YISI0cncLF16lxMFyMrO2yb5BvQ=="
                    val dev = "bb534eba-50c6-4521-91cc-fb5a96572ef8"
                    val decrypt = Encryption.decrypt(de, dev)
                  //  println(" USERRRRRRRRRR    User data Verified successfully")
                    //println("DECRYPTTTT  " + decrypt.toString())
                    callDialog()
                } else {
                    Toast.makeText(this@UserDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    //println("Failed to update user data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //println("Failed to update user data: ${t.message}")
                Toast.makeText(
                    this@UserDetailsActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    private fun deleteapicall(reason: String) {
        val call = ApiClient.apiService.deleteUser(
            delUserId, reason, merchantdata?.merchant_rep_id.toString()
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val response = response.body()?.string()
                    callDialog()
                } else {
                    Toast.makeText(this@UserDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Failed to delete user data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //println("Failed to delete user data: ${t.message}")
                Toast.makeText(
                    this@UserDetailsActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    private fun showDatePickerDialog(isFromDate: Boolean) {
        val currentDate = if (isFromDate) fromDate else toDate
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                currentDate.set(year, monthOfYear, dayOfMonth)
                if (isFromDate) {
                    updateFromDate(currentDate)
                } else {
                    updateToDate(currentDate)
                }
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )
        if (!isFromDate) {
            // Set min date for toDate as one day after fromDate
            datePickerDialog.datePicker.minDate =
                fromDate.timeInMillis + 86400000 // 86400000 milliseconds = 1 day
        }
        datePickerDialog.show()
    }
    fun validateFields(): Boolean {
        // Retrieve and trim user input
        val userId = editUserId.text.toString().trim()
        val userName = editUserName.text.toString().trim()
        val mobileNum = editmobileNum.text.toString().trim()
        val email = editemail.text.toString().trim()
        val designation = editUserDesign.text.toString().trim()
        val role = editUserRole.text.toString().trim()
        val unitid = editUnitId.text.toString().trim()
        val altMobileNum = editUserAltMobNum.text.toString().trim()
        val altEmail = editUserAltEmail.text.toString().trim()
        val nameRegex = Regex("^[a-zA-Z ]+$")
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$")
        clearAllErrors()
        val isValid = when {
            userId.isEmpty() -> {
                editUserId.error = "User ID cannot be empty"
                false
            }
            userName.isEmpty() -> {
                editUserName.error = "User Name cannot be empty"
                false
            }
            !userName.matches(nameRegex) -> {
                editUserName.error = "Invalid name format"
                false
            }
            designation.isEmpty() -> {
                editUserDesign.error = "Please select Designation"
                false
            }
            role.isEmpty() -> {
                editUserRole.error = "Please select Role"
                false
            }
            mobileNum.isEmpty() -> {
                editmobileNum.error = "Mobile number cannot be empty"
                false
            }
            email.isEmpty() -> {
                editemail.error = "Email cannot be empty"
                false
            }
            !email.matches(emailRegex) -> {
                editemail.error = "Invalid email format"
                false
            }
            unitid.isEmpty() -> {
                editUnitId.error = "Please Choose Unit"
                false
            }
            altMobileNum.isNotEmpty() && mobileNum == altMobileNum -> {
                editUserAltMobNum.error = "Alternate Number cannot be the same as Phone Number"
                false
            }
            altEmail.isNotEmpty() && (!altEmail.matches(emailRegex) || altEmail == email) -> {
                editUserAltEmail.error = if (!altEmail.matches(emailRegex)) {
                    "Invalid email format"
                } else {
                    "Email and alternative email cannot be the same. Please enter a different email ID."
                }
                false
            }
            else -> true
        }
        // Show dialog if validation fails
        if (!isValid) {
            AlertDialogBox().showDialog(this, "Please fill the required fields")
        }
        return isValid
    }
    private fun clearAllErrors() {
        editUserId.error = null
        editUserName.error = null
        editUserDesign.error = null
        editUserRole.error = null
        editmobileNum.error = null
        editemail.error = null
        editUnitId.error = null
        editUserAltMobNum.error = null
        editUserAltEmail.error = null
    }
    private fun callDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.sucessdialog, null)
        builder.setCancelable(false)
        // Set the custom view to the builder
        builder.setView(view)
        playSound()
        // Set other properties of the AlertDialog (e.g., title, buttons, etc.)
        builder.setPositiveButton("OK") { dialog, which ->
            // Handle positive button click
            dialog.dismiss()
            backToUM()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun playSound() {
        mediaPlayer?.start()
    }
    override fun onSupportNavigateUp(): Boolean {
        backToUM()
        return true
    }
    override fun onResume() {
        super.onResume()
    }
    override fun onPause() {
        super.onPause()
    }
    private fun fetchUnitDeviceData(editTextId: Int) {
        val call: Call<List<DeviceData>> = ApiClient.apiService.getDeviceList(
            merchantdata?.merchant_user_id!!.toString(), editUnitId.text.toString()
        )
        call.enqueue(object : Callback<List<DeviceData>> {
            override fun onResponse(
                call: Call<List<DeviceData>>, response: Response<List<DeviceData>>
            ) {
                if (response.isSuccessful) {
                    deviceList = response.body() ?: return // Ensure userList is not null

                    if (deviceList.isNotEmpty()) {
                        // Convert userList to display-friendly strings
                        val userListData = deviceList.map { "${it.device_id} (${it.device_name})" }

                        val editTextView = findViewById<EditText>(editTextId)
                        // Make EditText non-editable, but clickable
                        editTextView.inputType = InputType.TYPE_NULL
                        editTextView.isFocusable = false
                        editTextView.isClickable = true
                        // Show user selection popup
                        showDeviceSelectionPopup(editTextView, userListData)
                        // Set flag indicating data has been fetched
                        isUserDataFetched = true
                    } else {
                        // Make EditText editable if no users are found
                        val editTextView = findViewById<EditText>(editTextId)
                        editTextView.inputType = InputType.TYPE_CLASS_TEXT
                        editTextView.isFocusable = true
                        editTextView.isClickable = true
                        editTextView.isFocusableInTouchMode = true
                        AlertDialogBox().showDialog(
                            this@UserDetailsActivity, "No device found for this unit"
                        )
                    }
                }
                else{
                    Toast.makeText(this@UserDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<DeviceData>>, t: Throwable) {
                //t.printStackTrace()
                NetworkUtils.hideProgress(this@UserDetailsActivity)
                Toast.makeText(
                    this@UserDetailsActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    private fun showDeviceSelectionPopup(anchorView: EditText, deviceListData: List<String>) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_device_list, null)
        val editTextWidth = anchorView.width
        val popupWindow =
            PopupWindow(popupView, editTextWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        val listView = popupView.findViewById<ListView>(R.id.deviceListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceListData)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedUserData = deviceList[position]
            anchorView.setText(selectedUserData.device_id)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.NO_GRAVITY)
    }
    private fun fetchUnitData(id: Int) {
        val call: Call<List<UnitData>> =
            ApiClient.apiService.getUnitList(merchantdata?.merchant_user_id!!.toString())
        call.enqueue(object : Callback<List<UnitData>> {
            override fun onResponse(
                call: Call<List<UnitData>>, response: Response<List<UnitData>>
            ) {
                if (response.isSuccessful) {
                    unitList = response.body() ?: return // Ensure unitList is not null
                    if (unitList.isNotEmpty()) {
                        val unitListData = unitList.map { "${it.unit_id} (${it.unit_name})" }
                        val editText = findViewById<EditText>(id)
                        editText.inputType = InputType.TYPE_NULL
                        editText.isFocusable = false
                        editText.isClickable = true
                        editText.setOnClickListener {
                            hideKeyboard(it)
                            showUnitSelectionPopup(editText, unitListData)
                        }
                    } else {
                        val editTextView = findViewById<EditText>(id)
                        editTextView.isFocusable = true
                        editUnitName.isEnabled = true
                        editUnitName.isFocusable = true
                        editUnitName.editableText
                        editUnitName.inputType = InputType.TYPE_CLASS_TEXT
                        editUnitId.isEnabled = true
                        editUnitId.isFocusable = true
                        editUnitId.editableText
                        editUnitId.inputType = InputType.TYPE_CLASS_NUMBER
                        editUnitType.isEnabled = true
                        editUnitType.isFocusable = true
                        editUnitType.editableText
                        editUnitType.inputType = InputType.TYPE_CLASS_TEXT
                    }
                }
                else{
                    Toast.makeText(this@UserDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<UnitData>>, t: Throwable) {
                //t.printStackTrace()
                NetworkUtils.hideProgress(this@UserDetailsActivity)
                Toast.makeText(
                    this@UserDetailsActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    private fun showUnitSelectionPopup(anchorView: EditText, unitListData: List<String>) {
        // Inflate the layout for the popup window
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_device_list, null)
        val editTextWidth = anchorView.width
        val popupWindow = PopupWindow(
            popupView, editTextWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true
        )
        val listView = popupView.findViewById<ListView>(R.id.deviceListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, unitListData)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedUnitData = unitList[position]
            anchorView.setText(selectedUnitData.unit_id)
            editUnitName.setText(selectedUnitData.unit_name)
            editUnitType.setText(selectedUnitData.unit_type)
            editDefaultDeviceId.text.clear()
            editDeviceId1.text.clear()
            editDeviceId2.text.clear()
            editUnitId.error = null
            editUnitId.clearFocus()
            editUnitName.error = null
            editUnitName.clearFocus()
            editUnitType.error = null
            editUnitType.clearFocus()
            popupWindow.dismiss() }
        // Show the popup window below the EditText
        popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.NO_GRAVITY)
    }
    fun setEditTextsNonEditable(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.isEnabled = false
        }
    }
    fun setEditTextsEditable(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.isEnabled = true
        }
    }
    fun setEditTextsColor(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.setTextColor(getResources().getColor(R.color.lightgrey))
        }
    }
    private fun setHint(vararg editTexts: EditText) {
        editUserName.hint = "Enter User Name"
        editUserDisableToDate.hint = "To Date"
        edituserdisabledFromDate.hint = "From Date"
        editemail.hint = "Enter Email"
        editUserAltEmail.hint = "Enter Alter Email"
        editmobileNum.hint = "Enter Mobile Number"
        editUserAltMobNum.hint = "Alter Mobile Number"
        for (editText in editTexts) {
            editText.hint = "Select"
        }
    }
    private fun calldeldialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.delete_popup, null)
        builder.setView(view)
        val title:TextView=view.findViewById(R.id.titlefordelete)
        title.text="Enter the reason for deleting the user"
        val sumbtn: Button = view.findViewById(R.id.del_user)
        val delReason: EditText = view.findViewById(R.id.del_reason)
        val alertDialog = builder.create()
        alertDialog.show()
        val window = alertDialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams
        val decorView = window?.decorView
        decorView?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_background))
        sumbtn.setOnClickListener {
            val reasonText = delReason.text.toString().trim()
            if (reasonText.isEmpty()) {
                delReason.error = "Please provide a reason for deletion"
            } else {
                deleteapicall(reasonText)
                alertDialog.dismiss()
            }
        }
    }
    private fun backToUM(){
        val intent = Intent(this,Usermanagement::class.java)
        startActivity(intent)
        finish()
    }
    override fun onSuccess(data: List<String>) {
        adapter.clear()
        adapter.addAll(data)
        adapter.notifyDataSetChanged()
        popupWindow.showAsDropDown(editUserDesign)
    }
    override fun onError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
    private fun fetchReferenceCode(refId: String, view: EditText) {
        if (refId.isNotEmpty()) {
            ReferenceCode.getInfo(this, refId, object : ReferenceCodeCallback {
                override fun onSuccess(data: List<String>) {
                    if (data.isNotEmpty()) {
                        ReferenceCode.showReferenceCodePopup(this@UserDetailsActivity, view, data)
                    } else {
                        Toast.makeText(this@UserDetailsActivity, "No data found", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(errorMessage: String) {
                    Toast.makeText(this@UserDetailsActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}