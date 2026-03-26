package com.bornfire.merchantqrcode.AdminScreens

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.DeviceData
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DetailScreen.DeviceDetailsActivity
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.TextforTable.createTextView
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class DeviceManagement : BaseActivity() {
    lateinit var addbtn: Button
    lateinit var edFilter:EditText
    lateinit var nodata:TextView
     var deviceList: List<DeviceData> ? = null
    lateinit var  tableLayout: TableLayout
    lateinit var merchantid : String
    private var isDataFiltered = false
    lateinit var backBtn:ImageView
    lateinit var status:String
    val merchantdata = SharedMerchantDataObj.merchantData
    var devsId:String = "${merchantdata?.merchant_user_id}D001"
    lateinit var dm_help_image:ImageView
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devicemanagement)
        supportActionBar?.hide()
        nodata=findViewById(R.id.emptyTextView)
        edFilter = findViewById(R.id.editFilter)
        tableLayout = findViewById(R.id.tableLayout)
        addbtn = findViewById(R.id.addBtn)
        backBtn=findViewById(R.id.backBtn)
        dm_help_image=findViewById(R.id.dm_help_image)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        //set back button
        backBtn.setOnClickListener(){
            handleBackPress()
        }
        dm_help_image.setOnClickListener(){
            HelpInfo.getInfo(this,"16")
        }

        //To check the merchant rep or unit rep and fetch the list of device data
        if (NetworkUtils.isNetworkAvailable(this)) {
                fetchUnitData()
        }
        else {
            NetworkUtils.NoInternetAlert(this)
        }
        //To navigate to the device details screen to add the nre device
        addbtn.setOnClickListener{
            val intent = Intent(this, DeviceDetailsActivity::class.java)
            intent.putExtra("Button","Submit")
            intent.putExtra("devsId",devsId)
            startActivity(intent)
        }
        //Create the header for the table

        edFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if(edFilter.text.toString().length>0){
                    val filterQuery = edFilter.text.toString().trim()
                    filterData(filterQuery)
                }
                else
                {
                    fetchUnitData()
                }
            }
        })
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
              handleBackPress()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
}
    //Filter the data we serached
    private fun filterData(query: String) {
        val filteredList = deviceList?.filter {
            it.device_id?.contains(query, ignoreCase = true) == true ||
                    it.device_status?.contains(query, ignoreCase = true) == true ||
                    it.unit_id_d?.contains(query, ignoreCase = true) == true ||
                    it.unit_name_d?.contains(query, ignoreCase = true) == true ||
                    it.device_name?.contains(query, ignoreCase = true) == true
        }
        if (filteredList != null) {
            if (filteredList.isNotEmpty()) {
                isDataFiltered = true
                updateTable(filteredList)
            } else {
                AlertDialogBox().showDialog(this, "No matching data found")
            }
        }
    }
    //which will removed the all data and create the new table for our serached result
    private fun updateTable(filteredList: List<DeviceData>) {
        tableLayout.removeAllViews()
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange)) // Replace with the actual color resource

        // Create TextViews for each header column
        val headers = arrayOf("DEVICE ID","DEVICE NAME", "DEVICE STATUS", "UNIT ID","UNIT NAME","STATUS")
        for (header in headers) {
            val textView = createTextView(this,header)
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            textView.textSize = 15f
            headerRow.addView(textView)
        }
        // Add the header row to the table layout
        tableLayout.addView(headerRow)// Clear existing rows
        // Iterate through filtered list and add rows to the table
        filteredList.forEach { deviceList ->
            val row = createTableRow(deviceList)
            tableLayout.addView(row)
            row.setOnClickListener{
                val intent = Intent(applicationContext, DeviceDetailsActivity::class.java)
                intent.putExtra("deviceId", deviceList.device_id)
                intent.putExtra("defineduser", deviceList.defined_user)
                intent.putExtra("approvedUser", deviceList.approved_user)
                intent.putExtra("user1", deviceList.user1)
                intent.putExtra("user2", deviceList.user2)
                intent.putExtra("merchantId",deviceList.merchant_user_id)
                intent.putExtra("DeviceName",deviceList.device_name)
                intent.putExtra("DeviceIdNo",deviceList.device_identification_no)
                intent.putExtra("DeviceModel",deviceList.device_model)
                intent.putExtra("DeviceMake",deviceList.device_make)
                intent.putExtra("Location",deviceList.location)
                intent.putExtra("StoreId",deviceList.store_id)
                intent.putExtra("FingerEnabled",deviceList.fingerprint_enable)
                intent.putExtra("FaceEnabled",deviceList.face_recognition_enabled)
                intent.putExtra("DeviceStatus",deviceList.device_status)
                intent.putExtra("unitId",deviceList.unit_id_d)
                intent.putExtra("unitName",deviceList.unit_name_d)
                intent.putExtra("unitType",deviceList.unit_type_d)
                intent.putExtra("verify",deviceList.verify_user)
                intent.putExtra("entryUser",deviceList.entry_user)
                intent.putExtra("modifyuser",deviceList.modify_user)
                intent.putExtra("entryFlag",deviceList.entry_flag)
                intent.putExtra("modifyFlag",deviceList.modify_flag)
                intent.putExtra("terminalId",deviceList.terminal_id)
                startActivity(intent)
                finish()
            }
        }
    }
    //create the row for the serached result
    private fun createTableRow(deviceList: DeviceData): TableRow {
        // Create a row and add TextViews for each field in the data model
        val row = TableRow(this)
        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = params

        val deviceId = createTextView(this,deviceList.device_id)
        val devstatus = createTextView(this,deviceList.device_status)
        val devUnit = createTextView(this,deviceList.unit_id_d)
        val devUnitName = createTextView(this,deviceList.unit_name_d)
        val devicename = createTextView(this,deviceList.device_name)
        val status: TextView = if((deviceList.entry_flag == "N" && deviceList.modify_flag == "N") || (deviceList.entry_flag == "N" && deviceList.modify_flag == "Y")){
            createTextView(this,"Unverified")
        } else{
            createTextView(this,"Verified")
        }
        row.addView(deviceId)
        row.addView(devicename)
        row.addView(devstatus)
        row.addView(devUnit)
        row.addView(devUnitName)
        row.addView(status)
        row.setOnClickListener {
            // Handle row click
        }
        return row
    }
    //To fetch the device data by unit wise
    private fun fetchUnitData() {
        tableLayout.removeAllViews()
        val merchantdata = SharedMerchantDataObj.merchantData
        val unit = ""
            val headerRow = TableRow(this)
            headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange)) // Replace with the actual color resource
            // Create TextViews for each header column
            val headers = arrayOf("DEVICE ID", "DEVICE NAME","DEVICE STATUS", "UNIT ID","UNIT NAME","STATUS")
            for (header in headers) {
                val textView = createTextView(this,header)
                textView.setTypeface(null, android.graphics.Typeface.BOLD)
                textView.textSize = 15f
                headerRow.addView(textView)
            }
            // Add the header row to the table layout
            tableLayout.addView(headerRow)
        val call: Call<List<DeviceData>> = ApiClient.apiService.getDeviceList(merchantdata?.merchant_user_id!!, unit)
        call.enqueue(object : Callback<List<DeviceData>> {
            override fun onResponse(
                call: Call<List<DeviceData>>,
                response: Response<List<DeviceData>>) {
                if (response.isSuccessful) {
                    deviceList = response.body()!!
                    deviceList = deviceList!!.sortedByDescending { it.device_id }

                    // Filter the list to include only items where disable_flag == "N"
                    val filteredDeviceList = deviceList!!

                    if (filteredDeviceList.isNotEmpty()) {
                        val lastRequestId = filteredDeviceList.firstOrNull()?.device_identification_no
                        val nextRecSrl = getNextRecSrl(lastRequestId!!)
                        devsId = nextRecSrl
                    } else {
                        nodata.visibility = View.VISIBLE
                        edFilter.visibility = View.GONE
                    }

                    val tableLayout: TableLayout = findViewById(R.id.tableLayout)
                    filteredDeviceList.forEach { deviceList ->
                        val row = TableRow(this@DeviceManagement)
                        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                        row.layoutParams = params
                            val deviceId = createTextView(this@DeviceManagement,deviceList.device_id)
                            val devstatus = createTextView(this@DeviceManagement,deviceList.device_status)
                            val devunit = createTextView(this@DeviceManagement,deviceList.unit_id_d)
                            val devunitname = createTextView(this@DeviceManagement,deviceList.unit_name_d)
                            val devname = createTextView(this@DeviceManagement,deviceList.device_name)
                            val status = if ((deviceList.entry_flag == "N" && deviceList.modify_flag == "N") || (deviceList.entry_flag == "N" && deviceList.modify_flag == "Y")) {
                                createTextView(this@DeviceManagement,"Unverified")
                            } else {
                                createTextView(this@DeviceManagement,"Verified")
                            }
                            row.addView(deviceId)
                             row.addView(devname)
                            row.addView(devstatus)
                            row.addView(devunit)
                            row.addView(devunitname)
                            row.addView(status)
                        tableLayout.addView(row)
                        NetworkUtils.hideProgress(this@DeviceManagement)

                        row.setOnClickListener {
                            val intent = Intent(applicationContext, DeviceDetailsActivity::class.java)
                            intent.putExtra("deviceId", deviceList.device_id)
                            intent.putExtra("defineduser", deviceList.defined_user)
                            intent.putExtra("approvedUser", deviceList.approved_user)
                            intent.putExtra("user1", deviceList.user1)
                            intent.putExtra("user2", deviceList.user2)
                            intent.putExtra("merchantId", deviceList.merchant_user_id)
                            intent.putExtra("DeviceName", deviceList.device_name)
                            intent.putExtra("DeviceIdNo", deviceList.device_identification_no)
                            intent.putExtra("DeviceModel", deviceList.device_model)
                            intent.putExtra("DeviceMake", deviceList.device_make)
                            intent.putExtra("Location", deviceList.location)
                            intent.putExtra("StoreId", deviceList.store_id)
                            intent.putExtra("FingerEnabled", deviceList.fingerprint_enable)
                            intent.putExtra("FaceEnabled", deviceList.face_recognition_enabled)
                            intent.putExtra("DeviceStatus", deviceList.device_status)
                            intent.putExtra("unitId", deviceList.unit_id_d)
                            intent.putExtra("unitName", deviceList.unit_name_d)
                            intent.putExtra("unitType", deviceList.unit_type_d)
                            intent.putExtra("verify", deviceList.verify_user)
                            intent.putExtra("entryUser", deviceList.entry_user)
                            intent.putExtra("modifyuser", deviceList.modify_user)
                            intent.putExtra("entryFlag", deviceList.entry_flag)
                            intent.putExtra("modifyFlag", deviceList.modify_flag)
                            intent.putExtra("terminalId", deviceList.terminal_id)
                            startActivity(intent)
                        }
                    }

                    // Update UI or log the data
                } else {
                //    println("Error: ${response.code()}")
                    Toast.makeText(this@DeviceManagement, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    NetworkUtils.hideProgress(this@DeviceManagement)
                }
            }

            override fun onFailure(call: Call<List<DeviceData>>, t: Throwable) {
              //  t.printStackTrace()
                NetworkUtils.hideProgress(this@DeviceManagement)
                Toast.makeText(this@DeviceManagement, "Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun handleBackPress() {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
            finish()
    }
    fun getNextRecSrl(currentRecSrl: String): String {
        val numericPart = currentRecSrl.takeLastWhile { it.isDigit() }
        val prefix = currentRecSrl.dropLast(numericPart.length)
        val nextNumber = (numericPart.toIntOrNull() ?: 0) + 1
        val desiredLength = numericPart.length
        val newNumericPart = nextNumber.toString().padStart(desiredLength, '0')
        return prefix + newNumericPart
    }
}