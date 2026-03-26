package com.bornfire.merchantqrcode.DetailScreen

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.AdminScreens.DeviceManagement
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.DeviceData
import com.bornfire.merchantqrcode.DataModel.EncryptedRequest
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.UnitData
import com.bornfire.merchantqrcode.DataModel.UserData
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Dialog.YesNoDialog.showYesOrNO
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.EditNonEdit.hideKeyboard
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.NullCheck
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeviceDetailsActivity : BaseActivity() {
    private lateinit var editDeviceId: EditText
    private lateinit var editDefinedUser: EditText
    private lateinit var editApproveduser: EditText
    private lateinit var editAlteruser1: EditText
    private lateinit var editAlterUser2: EditText
    private lateinit var editDeviceName:EditText
    private lateinit var editDeviceIdNo:EditText
    private lateinit var editDeviceModel:EditText
    private lateinit var editDeviceMake:EditText
    private lateinit var editLocation:EditText
    private lateinit var editStoreId:EditText
    private lateinit var editFingerPrint:EditText
    private lateinit var editFaceReg:EditText
    private lateinit var editDeviceStatus:EditText
    private var mediaPlayer: MediaPlayer? = null
    lateinit var merchantName : String
    lateinit var merchantid : String
    private lateinit var headertext: TextView
    private lateinit var editUnitId:EditText
    private lateinit var editUnitName:EditText
    private lateinit var editUnitType:EditText
    lateinit var verifybtn:Button
    private lateinit var editTermId:EditText
    private lateinit var entryuser:String
    private lateinit var entryFlag:String
    private lateinit var modifyuser:String
    private lateinit var modifyFalg:String
    private lateinit var verifyUser:String
    lateinit var deviceId:String
    lateinit var userList: List<UserData>
    private lateinit var backimg: ImageView
    private lateinit var editBtn: ImageView
    lateinit var unitList:List<UnitData>
    private var isUserDataFetched = false
    private var lastUnitId: String = ""
    val merchantdata = SharedMerchantDataObj.merchantData
    private lateinit var deledevBtn:ImageView
    private lateinit var delDevId:String
    private lateinit var textunitid: TextView
    private lateinit var textterminalid: TextView
    private lateinit var textmodel: TextView
    private lateinit var textdevicename: TextView
    private lateinit var textidentification: TextView
    val unitid = "UNIT ID"
    val terminalid = "TERMINAL ID"
    val model = "DEVICE MODEL"
    val identificationnumb = "DEVICE IDENTIFICATION NUMBER"
    val devicename = "DEVICE NAME"
    lateinit var dd_help_image:ImageView
    var screenid:String = "18"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_details)
        supportActionBar?.hide()
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        verifyUser = intent.getStringExtra("verify").toString()
        deviceId = intent.getStringExtra("deviceId").toString()
        entryuser = intent.getStringExtra("entryUser").toString()
        entryFlag = intent.getStringExtra("entryFlag").toString()
        modifyFalg = intent.getStringExtra("modifyFlag").toString()
        modifyuser  = intent.getStringExtra("modifyuser").toString()
        // Initialize TextView variables
        textunitid = findViewById(R.id.textunitid)
        textterminalid= findViewById(R.id.textterminalid)
        textmodel = findViewById(R.id.textmodel)
        textidentification= findViewById(R.id.textidentification)
        textdevicename= findViewById(R.id.textdevicename)
        dd_help_image = findViewById(R.id.dd_help_image)

        verifybtn = findViewById(R.id.verifybtn)
        verifybtn.setOnClickListener{
                    if(entryFlag=="N"){
                        if(entryuser==merchantdata?.merchant_rep_id.toString()||modifyuser==merchantdata?.merchant_rep_id.toString()){
                            Toast.makeText(this,"Same users cannot be verify",Toast.LENGTH_LONG).show()
                        }
                        else{
                            verifyapicall()
                        }
                    }
                    else{
                        if(modifyuser==merchantdata?.merchant_rep_id.toString()){
                            Toast.makeText(this,"Same users cannot be verify",Toast.LENGTH_LONG).show()
                        }
                        else{
                            verifyapicall()
                        }
                    }

        }
        dd_help_image.setOnClickListener(){
            HelpInfo.getInfo(this,screenid)
        }
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        editDeviceId = findViewById(R.id.editDeviceId)
        editDefinedUser = findViewById(R.id.editDefinedUser)
        editApproveduser = findViewById(R.id.editApprovedUser)
        editAlteruser1 = findViewById(R.id.alterUser1)
        editAlterUser2 = findViewById(R.id.alterUser2)
        editDeviceName = findViewById(R.id.editDeviceName)
        editDeviceIdNo = findViewById(R.id.editDeviceIdNo)
        editDeviceModel = findViewById(R.id.editDeviceModel)
        editDeviceMake = findViewById(R.id.editDeviceMake)
        editLocation = findViewById(R.id.editLocation)
        editStoreId = findViewById(R.id.editStoreId)
        editFingerPrint = findViewById(R.id.editFingerPrint)
        editFaceReg = findViewById(R.id.editFaceReg)
        editDeviceStatus = findViewById(R.id.editDeviceStatus)
        editUnitId = findViewById(R.id.editUnitId)
        editUnitName = findViewById(R.id.editUnitName)
        editUnitType = findViewById(R.id.editUnitType)
        editTermId = findViewById(R.id.editTermId)
        deledevBtn= findViewById(R.id.deledevBtn)
        val btn: Button = findViewById(R.id.submitbtn)
        mediaPlayer = MediaPlayer.create(this, R.raw.iphone)
        supportActionBar?.title = "DEVICE DETAILS"

        editDefinedUser.inputType = InputType.TYPE_NULL
        editApproveduser.inputType = InputType.TYPE_NULL
        editAlteruser1.inputType = InputType.TYPE_NULL
        editAlterUser2.inputType = InputType.TYPE_NULL
        editUnitId.inputType = InputType.TYPE_NULL
        val buttonName = intent.getStringExtra("Button")
        editFaceReg.setOnClickListener{
            showYesOrNO(this,editFaceReg)
        }
        editFingerPrint.setOnClickListener{
            hideKeyboard(it)
            showYesOrNO(this,editFingerPrint)
        }
        editDeviceStatus.setOnClickListener{
            hideKeyboard(it)
            showDevStatus(editDeviceStatus)
        }
        backimg = findViewById(R.id.back_img)
        editBtn = findViewById(R.id.editableBtn)
        backimg.setOnClickListener{
            backToDM() }
        editDefinedUser.setOnClickListener {
            // Check if Unit ID is provided
            if (editUnitId.text.toString().isEmpty()) {
                AlertDialogBox().showDialog(this, "Please Choose Unit")
            } else {
                // Fetch data if not already fetched or if the unit ID has changed
                if (!isUserDataFetched) {
                    fetchUnitUserData(R.id.editDefinedUser)
                } else {
                    // Show the existing data or handle it appropriately
                    val userListData = userList.map { "${it.user_id} (${it.user_name})" }
                    if (userListData.isNotEmpty()) {
                        showUserSelectionPopup(editDefinedUser, userListData)
                    } else {
                        AlertDialogBox().showDialog(this, "No data available")
                    }
                }
            }
        }
        editApproveduser.setOnClickListener{
            if (editUnitId.text.toString().isEmpty()) {
                AlertDialogBox().showDialog(this, "Please Choose Unit")
            } else {
                // Fetch data if not already fetched or if the unit ID has changed
                if (!isUserDataFetched) {
                    fetchUnitUserData(R.id.editApprovedUser)
                } else {
                    val userListData = userList.map { "${it.user_id} (${it.user_name})" }
                    if (userListData.isNotEmpty()) {
                        showUserSelectionPopup(editApproveduser, userListData)
                    } else {
                        AlertDialogBox().showDialog(this, "No data available")
                    }
                }
            }
        }
        editAlteruser1.setOnClickListener{
            if (editUnitId.text.toString().isEmpty()) {
                AlertDialogBox().showDialog(this, "Please Choose Unit")
            } else {
                // Fetch data if not already fetched or if the unit ID has changed
                if (!isUserDataFetched) {
                    fetchUnitUserData(R.id.alterUser1)
                } else {
                    val userListData = userList.map { "${it.user_id} (${it.user_name})" }
                    if (userListData.isNotEmpty()) {
                        showUserSelectionPopup(editAlteruser1, userListData)
                    } else {
                        AlertDialogBox().showDialog(this, "No data available")
                    }
                }
            }
        }
        editAlterUser2.setOnClickListener{
            if (editUnitId.text.toString().isEmpty()) {
                AlertDialogBox().showDialog(this, "Please Choose Unit")
            } else {
                if (!isUserDataFetched) {
                    fetchUnitUserData(R.id.alterUser2)
                } else {
                    val userListData = userList.map { "${it.user_id} (${it.user_name})" }
                    if (userListData.isNotEmpty()) {
                        showUserSelectionPopup(editAlterUser2, userListData)
                    } else {
                        AlertDialogBox().showDialog(this, "No data available")
                    }
                }
            }
        }
        editUnitId.setOnClickListener{
            if(merchantdata?.pwlog_flg=="MERCHANT"){
                fetchUnitData(R.id.editUnitId)
            }
            else{
                editUnitId.setText(merchantdata?.unit_id)
                editUnitName.setText(merchantdata?.unit_id)
                editUnitType.setText(merchantdata?.unit_type)
            }
        }
        deledevBtn.setOnClickListener{
            AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to Delete the Device?")
                .setPositiveButton("Ok") { dialog, which ->
                    calldeldialog()
                }
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show()
            true
        }
        editBtn.setOnClickListener{
            screenid = "18"
            headertext=findViewById(R.id.headertext)
            headertext.text=getString(R.string.upd_dev_det)
            setUpdateEditMode()
            btn.visibility=View.VISIBLE
            setEditTextsEditable( editDefinedUser, editApproveduser, editAlteruser1,
                editAlterUser2,
                editDeviceMake, editLocation, editStoreId, editFingerPrint,
                editFaceReg, editDeviceStatus, editUnitId,editTermId) }
        //Add Screen
        if(buttonName!=null) {
            getDevUniqueId()
            screenid = "17"
            headertext=findViewById(R.id.headertext)
            headertext.text=getString(R.string.add_dev_det)
            setEditMode()
            btn.text = buttonName
            btn.visibility=View.VISIBLE
            if(merchantdata?.pwlog_flg=="MERCHANT"){
                fetchUnitData(R.id.editUnitId)
            }
            else{
                setEditTextsColor(editUnitId,editUnitName,editUnitType)
                editUnitId.isEnabled = false
                editUnitId.isFocusable = false
                editUnitId.setText(merchantdata?.unit_id.toString())
                editUnitName.setText(merchantdata?.unit_name.toString())
                editUnitType.setText(merchantdata?.unit_type.toString())
            }
            btn.setOnClickListener{
                if (validateFields()) {
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        postapicall()
                    } else {
                        NetworkUtils.NoInternetAlert(this)
                    }
                } else {
                    AlertDialogBox().showDialog(this,"Please fill the required field")
                }
            }
        }
        //Update or Verify Screen
        else {
            val deviceId = intent.getStringExtra("deviceId")
            val defaultUser = intent.getStringExtra("defineduser")
            val approvedUser = intent.getStringExtra("approvedUser")
            val user1 = intent.getStringExtra("user1")
            val user2 = intent.getStringExtra("user2")
            val deviceName = intent.getStringExtra("DeviceName")
            val deviceIdNo = intent.getStringExtra("DeviceIdNo")
            val deviceModel = intent.getStringExtra("DeviceModel")
            val deviceMake = intent.getStringExtra("DeviceMake")
            val location = intent.getStringExtra("Location")
            val storeId = intent.getStringExtra("StoreId")
            val fingerPrint = intent.getStringExtra("FingerEnabled")
            val faceEnabled = intent.getStringExtra("FaceEnabled")
            val devStatus = intent.getStringExtra("DeviceStatus")
            val deviceUnitId = intent.getStringExtra("unitId")
            val deviceUnitName = intent.getStringExtra("unitName")
            val deviceUnitType = intent.getStringExtra("unitType")
            val terminalId = intent.getStringExtra("terminalId")
            delDevId = deviceIdNo.toString()

            editDeviceId.setText(getValidText(deviceId))
            editDefinedUser.setText(getValidText(defaultUser))
            editApproveduser.setText(getValidText(approvedUser))
            editAlteruser1.setText(getValidText(user1))
            editAlterUser2.setText(getValidText(user2))
            editDeviceName.setText(getValidText(deviceName))
            editDeviceIdNo.setText(getValidText(deviceIdNo))
            editDeviceModel.setText(getValidText(deviceModel))
            editDeviceMake.setText(getValidText(deviceMake))
            editLocation.setText(getValidText(location))
            editStoreId.setText(getValidText(storeId))
            editFingerPrint.setText(getValidText(fingerPrint))
            editFaceReg.setText(getValidText(faceEnabled))
            editDeviceStatus.setText(getValidText(devStatus))
            editUnitId.setText(getValidText(deviceUnitId))
            editUnitName.setText(getValidText(deviceUnitName))
            editUnitType.setText(getValidText(deviceUnitType))
            editTermId.setText(getValidText(terminalId))
            deledevBtn.visibility = View.VISIBLE
            editBtn.visibility = View.VISIBLE
            btn.setOnClickListener{
                if (validateFields()) {
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        if (NullCheck.getValidText(deviceId) != editDeviceId.text.toString() ||
                            NullCheck.getValidText(defaultUser) != editDefinedUser.text.toString() ||
                            NullCheck.getValidText(approvedUser) != editApproveduser.text.toString() ||
                            NullCheck.getValidText(user1) != editAlteruser1.text.toString() ||
                            NullCheck.getValidText(user2) != editAlterUser2.text.toString() ||
                            NullCheck.getValidText(deviceName) != editDeviceName.text.toString() ||
                            NullCheck.getValidText(deviceIdNo) != editDeviceIdNo.text.toString() ||
                            NullCheck.getValidText(deviceModel) != editDeviceModel.text.toString() ||
                            NullCheck.getValidText(deviceMake) != editDeviceMake.text.toString() ||
                            NullCheck.getValidText(location) != editLocation.text.toString() ||
                            NullCheck.getValidText(storeId) != editStoreId.text.toString() ||
                            NullCheck.getValidText(fingerPrint) != editFingerPrint.text.toString() ||
                            NullCheck.getValidText(faceEnabled) != editFaceReg.text.toString() ||
                            NullCheck.getValidText(devStatus) != editDeviceStatus.text.toString() ||
                            NullCheck.getValidText(deviceUnitId) != editUnitId.text.toString() ||
                            NullCheck.getValidText(deviceUnitName) != editUnitName.text.toString() ||
                            NullCheck.getValidText(deviceUnitType) != editUnitType.text.toString() ||
                            NullCheck.getValidText(terminalId) != editTermId.text.toString()
                        ) {
                            updateapicall()
                        }
                    } else {
                        NetworkUtils.NoInternetAlert(this)
                    }
                } else {
                    Toast.makeText(this, "Validation Failed", Toast.LENGTH_LONG).show()
                }
            }
            if(entryFlag == "N" || modifyFalg == "Y"){
                screenid = "19"
                headertext=findViewById(R.id.headertext)
                headertext.text=getString(R.string.ver_dev_det)
                setTextViewMode()
                hideHint( editDeviceId, editDefinedUser, editApproveduser, editAlteruser1, editAlterUser2, editDeviceName,
                    editDeviceIdNo, editDeviceModel, editDeviceMake, editLocation, editStoreId, editFingerPrint, editFaceReg, editDeviceStatus,
                    editUnitId, editUnitName, editUnitType, editTermId)
                verifybtn.visibility = View.VISIBLE
                btn.visibility = View.GONE
                setEditTextsNonEditable(
                    editDeviceId,
                    editDefinedUser,
                    editApproveduser,
                    editAlteruser1,
                    editAlterUser2,
                    editDeviceName,
                    editDeviceIdNo,
                    editDeviceModel,
                    editDeviceMake,
                    editLocation,
                    editStoreId,
                    editFingerPrint,
                    editFaceReg,
                    editDeviceStatus,
                    editUnitId,
                    editUnitName,
                    editUnitType,
                    editTermId
                )
            }
            else{
                screenid = "18"
                setEditTextsNonEditable(
                    editDeviceId, editDefinedUser, editApproveduser, editAlteruser1, editAlterUser2, editDeviceName, editDeviceIdNo,
                    editDeviceModel, editDeviceMake, editLocation, editStoreId, editFingerPrint, editFaceReg, editDeviceStatus, editUnitId,
                    editUnitName, editUnitType, editTermId
                )
                headertext=findViewById(R.id.headertext)
                headertext.text=getString(R.string.view_dev_det)
                hideHint( editDeviceId, editDefinedUser, editApproveduser, editAlteruser1, editAlterUser2, editDeviceName,
                    editDeviceIdNo, editDeviceModel, editDeviceMake, editLocation, editStoreId, editFingerPrint, editFaceReg, editDeviceStatus,
                    editUnitId, editUnitName, editUnitType, editTermId)
                setTextViewMode()
                verifybtn.visibility = View.GONE


        }
        }
        editUnitId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // When unit ID changes, reset the flag and user list
                if (s.toString() != lastUnitId) {
                    isUserDataFetched = false
                    userList = emptyList()
                    lastUnitId = s.toString()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { backToDM() }
        })
    }
    fun getSpannableStringWithRedStar(text: String): SpannableString {
        val spannable = SpannableString("$text *")
        spannable.setSpan(ForegroundColorSpan(Color.RED), spannable.length - 1, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }
    fun setTextViewMode() {
        textunitid.text = unitid
        textterminalid.text = terminalid
        textmodel.text = model
        textidentification.text = identificationnumb
        textdevicename.text = devicename
    }
    fun setUpdateEditMode() {
        textunitid.text = getSpannableStringWithRedStar(unitid)
        textterminalid.text = getSpannableStringWithRedStar(terminalid)
    }
    fun setEditMode() {
        textunitid.text = getSpannableStringWithRedStar(unitid)
        textterminalid.text = getSpannableStringWithRedStar(terminalid)
        textmodel.text = getSpannableStringWithRedStar(model)
        textidentification.text = getSpannableStringWithRedStar(identificationnumb)
        textdevicename.text = getSpannableStringWithRedStar(devicename)
    }
    private fun getValidText(value: String?, default: String = ""): String {
        return if (value == null || value.equals("null", ignoreCase = true)) {
            default
        } else {
            value
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun postapicall() {
        merchantName = merchantdata!!.merchant_name.toString()
        merchantid = merchantdata!!.merchant_user_id.toString()
        val deviceData = DeviceData(merchantid,merchantName,"","", editDeviceId.text.toString(),editDeviceName.text.toString(),editDeviceIdNo.text.toString(),
            "",editDeviceModel.text.toString(),editDeviceMake.text.toString(),"",editDeviceStatus.text.toString(),editLocation.text.toString(),editStoreId.text.toString(),editFingerPrint.text.toString(),editFaceReg.text.toString(),"","",
            merchantdata.merchant_rep_id.toString(),null,null,"","","",editApproveduser.text.toString(),editDefinedUser.text.toString(),
            editAlteruser1.text.toString(),editAlterUser2.text.toString(),editUnitId.text.toString(),editUnitName.text.toString(),
            editUnitType.text.toString(),"N","N",editTermId.text.toString(),"","","","")
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(deviceData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.addDeviceData(encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    callDialog()
                    val responseBody = response.body()?.string()
                    val decry = Encryption.decrypt(responseBody!!,psuDeviceID)
                 //   println("Response-----------> $decry")

                } else {
                    Toast.makeText(this@DeviceDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                   // println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               // println("Network error: ${t.message}")
                Toast.makeText(this@DeviceDetailsActivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun verifyapicall() {
            val deviceData = DeviceData("","","","", editDeviceId.text.toString(),editDeviceName.text.toString(),editDeviceIdNo.text.toString(),
                "",editDeviceModel.text.toString(),editDeviceMake.text.toString(),"",editDeviceStatus.text.toString(),editLocation.text.toString(),editStoreId.text.toString(),editFingerPrint.text.toString(),editFingerPrint.text.toString(),"","",
                "","",merchantdata?.merchant_rep_id.toString(),"","","",editApproveduser.text.toString(),editDefinedUser.text.toString(),editAlteruser1.text.toString(),editAlterUser2.text.toString(),editUnitId.text.toString(),editUnitName.text.toString(),editUnitType.text.toString(),"Y","N",editTermId.text.toString(),"","","","")
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(deviceData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
            val call = ApiClient.apiService.verifyDevice(psuDeviceID, encryptedRequest)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        callDialog()
                      //  println("User data updated successfully")
                    } else {
                        Toast.makeText(this@DeviceDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                       // println("Failed to update user data: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                 //   println("Failed to update user data: ${t.message}")
                    Toast.makeText(this@DeviceDetailsActivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
                }
            })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateapicall() {
        val deviceData = DeviceData("","","","", editDeviceId.text.toString(),editDeviceName.text.toString(),editDeviceIdNo.text.toString(),
            "",editDeviceModel.text.toString(),editDeviceMake.text.toString(),"",editDeviceStatus.text.toString(),editLocation.text.toString(),editStoreId.text.toString(),editFingerPrint.text.toString(),editFingerPrint.text.toString(),"","",
            "",merchantdata!!.merchant_rep_id.toString(),null,"","","",editApproveduser.text.toString(),editDefinedUser.text.toString(),editAlteruser1.text.toString(),editAlterUser2.text.toString(),editUnitId.text.toString(),editUnitName.text.toString(),editUnitType.text.toString(),"N","Y",editTermId.text.toString(),"","","","")
        val url = "api/UpdateDeviceData"
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(deviceData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.updateDeviceData(url, encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callDialog()
                //    println("User data updated successfully")
                } else {
                    Toast.makeText(this@DeviceDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                   // println("Failed to update user data: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               // println("Failed to update user data: ${t.message}")
                Toast.makeText(this@DeviceDetailsActivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun deleteapicall(reason:String) {
        val call = ApiClient.apiService.deleteDevice(delDevId,reason,merchantdata?.merchant_rep_id.toString())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val response = response.body()?.string()
                    callDialog()
                } else {
                    Toast.makeText(this@DeviceDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                 //   println("Failed to delete user data: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  println("Failed to delete user data: ${t.message}")
                Toast.makeText(
                    this@DeviceDetailsActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    private fun validateFields(): Boolean {
        val deviceId = editDeviceId.text.toString()
        val devname = editDeviceName.text.toString()
        val devidentity = editDeviceIdNo.text.toString()
        val devmodel = editDeviceModel.text.toString()
        val unitid = editUnitId.text.toString()
        val unitname = editUnitName.text.toString()
        val terminalid = editTermId.text.toString()
        if (deviceId.isEmpty() || devname.isEmpty() || devidentity.isEmpty() || devmodel.isEmpty() || unitid.isEmpty() || unitname.isEmpty() || terminalid.isEmpty()){
            if (deviceId.isEmpty()){
                editDeviceId.error = "Please Enter Device Id"
            }
            if(devname.isEmpty()){
                editDeviceName.error = "Please Enter Device Name"
            }
            if(devidentity.isEmpty()){
                editDeviceIdNo.error = "Please Enter Device Identification Number"
            }
            if(devmodel.isEmpty()){
                editDeviceModel.error = "Please Enter Device Model"
            }
            if(unitid.isEmpty()){
                editUnitId.error = "Please Enter Unit Id"
            }
            if(terminalid.isEmpty()){
                editTermId.error = "Please Enter Terminal Id"
            }
            return false
        }
        return true
    }
    private fun setEditTextsColor(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.setTextColor(ContextCompat.getColor(this, R.color.lightgrey))
        }
    }
    private fun setEditTextsEditable(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.isEnabled = true
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
        }
    }
    private fun setEditTextsNonEditable(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.isEnabled = false
        }
    }
    private fun showDevStatus(anchor: EditText) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.dev_status_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.act -> {
                    anchor.setText(R.string.act)
                    true
                }
                R.id.inactive -> {
                    anchor.setText(R.string.inactive)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    private fun fetchUnitData(id: Int) {
        val call: Call<List<UnitData>> = ApiClient.apiService.getUnitList(merchantdata?.merchant_user_id!!.toString())
        call.enqueue(object : Callback<List<UnitData>> {
            override fun onResponse(call: Call<List<UnitData>>, response: Response<List<UnitData>>) {
                if (response.isSuccessful) {
                    unitList = response.body() ?: return
                    if (unitList.isNotEmpty()) {
                        val unitListData = unitList.map { "${it.unit_id} (${it.unit_name})" }
                        val editText = findViewById<EditText>(id)
                        editText.inputType = InputType.TYPE_NULL
                        editText.isFocusable = false
                        editText.isClickable = true
                        editText.setOnClickListener {
                            showUnitSelectionPopup(editText, unitListData)

                        }
                    } else {
                        val editTextView = findViewById<EditText>(id)
                        editTextView.isFocusable = true
                    }
                }
                else{
                    Toast.makeText(this@DeviceDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<UnitData>>, t: Throwable) {
              //  t.printStackTrace()
                NetworkUtils.hideProgress(this@DeviceDetailsActivity)
                Toast.makeText(
                    this@DeviceDetailsActivity,
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

        // Calculate the desired width (e.g., match the EditText width or set a specific width)
        val editTextWidth = anchorView.width
        // Create a PopupWindow with the calculated width
        val popupWindow = PopupWindow(
            popupView,
            editTextWidth, // Use the desired width here
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        val listView = popupView.findViewById<ListView>(R.id.deviceListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, unitListData)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedUnitData = unitList[position]
            anchorView.setText(selectedUnitData.unit_id)
            editUnitName.setText(selectedUnitData.unit_name)
            editUnitType.setText(selectedUnitData.unit_type)
            editDefinedUser.text.clear()
            editApproveduser.text.clear()
            editAlteruser1.text.clear()
            editAlterUser2.text.clear()
            isUserDataFetched = false
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.NO_GRAVITY)
    }
    private fun fetchUnitUserData(id: Int) {
        val merchantData = SharedMerchantDataObj.merchantData
        val merchantId = merchantData?.merchant_user_id ?: return
        val unitId = editUnitId.text.toString()
        val call: Call<List<UserData>> = ApiClient.apiService.getUserList(merchantId, unitId)

        call.enqueue(object : Callback<List<UserData>> {
            override fun onResponse(call: Call<List<UserData>>, response: Response<List<UserData>>) {
                if (response.isSuccessful) {
                    userList = response.body() ?: return // Ensure userList is not null

                    if (userList.isNotEmpty()) {
                        // Convert userList to display-friendly strings
                        val userListData = userList.map { "${it.user_id} (${it.user_name})" }

                        val editTextView = findViewById<EditText>(id)
                        // Make EditText non-editable, but clickable
                        editTextView.inputType = InputType.TYPE_NULL
                        editTextView.isFocusable = false
                        editTextView.isClickable = true
                        // Show user selection popup
                        showUserSelectionPopup(editTextView, userListData)
                        // Set flag indicating data has been fetched
                        isUserDataFetched = true
                    } else {
                        // Make EditText editable if no users are found
                        val editTextView = findViewById<EditText>(id)
                        editTextView.inputType = InputType.TYPE_CLASS_TEXT
                        editTextView.isFocusable = true
                        editTextView.isClickable = true
                        editTextView.isFocusableInTouchMode = true

                        // Show message for no users
                        AlertDialogBox().showDialog(this@DeviceDetailsActivity, "No users found for this unit")
                    }
                }
                else {
                //    println("Error: ${response.code()}")
                    // Handle unsuccessful response
                    Toast.makeText(this@DeviceDetailsActivity, "Failed to retrieve data. Error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<UserData>>, t: Throwable) {
                // Handle network or other failures
                Toast.makeText(this@DeviceDetailsActivity, "Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
               // t.printStackTrace() // Log the error for debugging
            }
        })
    }
    private fun showUserSelectionPopup(anchorView: EditText, userListData: List<String>) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_device_list, null)
        val editTextWidth = anchorView.width
        val popupWindow = PopupWindow(popupView, editTextWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        val listView = popupView.findViewById<ListView>(R.id.deviceListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userListData)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedUserData = userList[position]
            anchorView.setText(selectedUserData.user_id)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.NO_GRAVITY)
    }
    private fun playSound() {
        mediaPlayer?.start()
    }
    private fun callDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.sucessdialog, null)
        builder.setCancelable(false)
        builder.setView(view)
        playSound()
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            finish()
            val intent = Intent(this, DeviceManagement::class.java)
            startActivity(intent)
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun backToDM(){
        val intent = Intent(this, DeviceManagement::class.java)
        startActivity(intent)
        finish()
    }
    override fun onSupportNavigateUp(): Boolean {
        backToDM()
        return true
    }
    private fun hideHint(vararg editTexts: EditText){
        for (editText in editTexts) {
            editText.hint = null
        }
    }
    private fun calldeldialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.delete_popup, null)
        builder.setView(view)
        val sumbtn: Button = view.findViewById(R.id.del_user)
        val title:TextView=view.findViewById(R.id.titlefordelete)
        title.text="Enter the reason for deleting the device"
        val delReason: EditText = view.findViewById(R.id.del_reason)
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.setCancelable(false)
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
    private fun getDevUniqueId() {
        val call = ApiClient.apiService.getDeviceId(merchantdata?.merchant_user_id.toString())
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val response = response.body()
                    editDeviceId.setText(response.toString())
                }
                else {
                    Toast.makeText(this@DeviceDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                //    println("Error: ${response.code()}")
                //    println("Failed to get device id: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
              //  Log.d("Get Device Id", "Network error: ${t.message}")
            }
        })
    }
}
