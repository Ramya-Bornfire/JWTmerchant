package com.bornfire.merchantqrcode.AdminScreens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.SerReqData
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DetailScreen.ServiceRequestDetails
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.TextforTable.createTextView
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class ServiceRequest : BaseActivity() {
     var serReqList: List<SerReqData>? = null
    val merchantdata = SharedMerchantDataObj.merchantData
    lateinit var addBtn:Button
    var reqid:String = "${merchantdata?.merchant_user_id}SR1"
    lateinit var backBtn:ImageView
    lateinit var edFilter : EditText
    lateinit var tableLayout: TableLayout
    private var isDataFiltered = false
    lateinit var nodata:TextView
    lateinit var sr_help_image:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_request)
        supportActionBar?.hide()
        tableLayout = findViewById(R.id.sertableLayout)
        addBtn = findViewById(R.id.addBtn)
        nodata=findViewById(R.id.emptyTextView)
        sr_help_image = findViewById(R.id.sr_help_image)
        sr_help_image.setOnClickListener(){
            HelpInfo.getInfo(this,"32")
        }
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        addBtn.setOnClickListener{
            val intent = Intent(this,ServiceRequestDetails::class.java)
            intent.putExtra("reqid",reqid)
            startActivity(intent)
        }
        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener{
            handleBackPress()
        }
        edFilter = findViewById(R.id.editFilter)
        edFilter.setOnClickListener {
            edFilter.isFocusable = true  // Make EditText focusable
            edFilter.isFocusableInTouchMode = true  // Allow focus in touch mode
            edFilter.requestFocus()  // Request focus for EditText
            showKeyboard()  // Show the keyboard when EditText is clicked
        }
        edFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if(edFilter.text.toString().length>0){
                    val filterQuery = edFilter.text.toString().trim()
                    filterData(filterQuery)
                }
                else{
                    getSerRequestUnit()
                }
            }
        })

            if (NetworkUtils.isNetworkAvailable(this)) {
                    getSerRequestUnit()
            }
            else {
                NetworkUtils.NoInternetAlert(this)
            }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }
    private fun createHeaderRow() {
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange))
        val headers = arrayOf("REQUEST ID", "REQUEST DATE", "STATUS", "ENTRY USER","PRIORITY")
        headers.forEach { header ->
            val textView = createTextView(this,header)
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            textView.textSize = 15f
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)
    }
    fun String?.orEmptyString() = this ?: ""
    private fun getSerRequestUnit(){
        tableLayout.removeAllViews()
        createHeaderRow()
      //  println("Start Get Request Api Call")
        val call: Call<List<SerReqData>> = ApiClient.apiService.getServiceRequestList(merchantdata?.merchant_user_id!!,merchantdata?.unit_id!!)
        call.enqueue(object : Callback<List<SerReqData>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<SerReqData>>,
                response: Response<List<SerReqData>>
            ) {
                    if (response.isSuccessful) {
                        serReqList = response.body()!!
                        serReqList = serReqList!!.sortedByDescending { it. request_id}
                        if(serReqList!!.isNotEmpty()){
                            edFilter.visibility = View.VISIBLE
                            val lastRequestId = serReqList!!.firstOrNull()?.request_id
                            val nextRecSrl = getNextRecSrl(lastRequestId!!)
                            reqid = nextRecSrl
                          //  println("Last Request ID: $lastRequestId")
                        }
                        else{
                            nodata.visibility=View.VISIBLE
                            edFilter.visibility = View.GONE
                        }
                        serReqList?.forEach{ serviceReqData->
                            val tableLayout: TableLayout = findViewById(R.id.sertableLayout)
                            val row = TableRow(this@ServiceRequest)
                            val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                            row.layoutParams = params

                            val recSrl = createTextView(this@ServiceRequest,serviceReqData.request_id.orEmptyString())
                            val priority = createTextView(this@ServiceRequest,serviceReqData.priority.orEmptyString())
                            val status = createTextView(this@ServiceRequest,serviceReqData.status.orEmptyString())
                            val entryUser = createTextView(this@ServiceRequest,serviceReqData.entry_user.orEmptyString())
                            val requestDate = createTextView(this@ServiceRequest,serviceReqData.request_date.toString())

                            row.addView(recSrl)
                            row.addView(requestDate)
                            row.addView(status)
                            row.addView(entryUser)
                            row.addView(priority)

                            tableLayout.addView(row)
                            NetworkUtils.hideProgress(this@ServiceRequest)
                            row.setOnClickListener{
                                val intent = Intent(applicationContext, ServiceRequestDetails::class.java)
                                intent.putExtra("request_id", serviceReqData.request_id.orEmpty())
                                intent.putExtra("merchant_id", serviceReqData.merchant_id.orEmpty())
                                intent.putExtra("request_type", serviceReqData.request_type.orEmpty())
                                intent.putExtra("request_description", serviceReqData.request_description.orEmpty())
                                intent.putExtra("steps_to_reproduce", serviceReqData.steps_to_reproduce.orEmpty())
                                intent.putExtra("error_message", serviceReqData.error_message.orEmpty())
                                intent.putExtra("priority", serviceReqData.priority.orEmpty())
                                intent.putExtra("contact_email", serviceReqData.contact_email.orEmpty())
                                intent.putExtra("contact_phone", serviceReqData.contact_phone.orEmpty())
                                intent.putExtra("additional_notes", serviceReqData.additional_notes.orEmpty())
                                intent.putExtra("status", serviceReqData.status.orEmpty())
                                intent.putExtra("approved_by", serviceReqData.approved_by.orEmpty())
                                intent.putExtra("assign_to", serviceReqData.assign_to.orEmpty())
                                intent.putExtra("entry_user", serviceReqData.entry_user.orEmpty())
                                val requestDateString = (serviceReqData.request_date)
                                val approvedDateString = (serviceReqData.approved_date)
                                val assignedDateString = (serviceReqData.assigned_date)
                                intent.putExtra("request_date", requestDateString)
                                intent.putExtra("approved_date", approvedDateString)
                                intent.putExtra("assigned_date", assignedDateString)
                                intent.putExtra("Button","Update")
                                startActivity(intent)
                            }
                        }
                    }
                 else {
                    Toast.makeText(this@ServiceRequest, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                   // println("Error: ${response.code()}")
                    NetworkUtils.hideProgress(this@ServiceRequest)
                }
            }
            override fun onFailure(call: Call<List<SerReqData>>, t: Throwable) {
               // t.printStackTrace()
                NetworkUtils.hideProgress(this@ServiceRequest)
                Toast.makeText(this@ServiceRequest,"Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
            }
        })

    }
    fun getNextRecSrl(currentRecSrl: String): String {
        val lastDigitIndex = currentRecSrl.indexOfLast { it.isDigit() }
        val prefix = currentRecSrl.substring(0, lastDigitIndex + 1)
        val numericPart = currentRecSrl.substring(lastDigitIndex + 1)
        val nextNumber = (numericPart.toIntOrNull() ?: 0) + 1
        return prefix + nextNumber.toString().padStart(numericPart.length, '0')
    }
    override fun onResume() {
        super.onResume()
        edFilter.text.clear()
        edFilter.isFocusable = false  // Make EditText unfocusable
        edFilter.isFocusableInTouchMode = false  // Prevent it from gaining focus in touch mode
        hideKeyboard()
    }
    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(edFilter.windowToken, 0)
    }
    private fun showKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(edFilter, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun handleBackPress() {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
            finish()

    }
    private fun filterData(query: String) {

        val filteredList = serReqList?.let {list ->
            list.filter {
                        it.request_id?.contains(query, ignoreCase = true) == true ||
                        it.request_date?.toString()?.contains(query, ignoreCase = true) == true ||
                        it.status?.contains(query, ignoreCase = true) == true ||
                        it.entry_user?.contains(query, ignoreCase = true) == true ||
                        it.priority?.contains(query, ignoreCase = true) == true
            }
        }
        if (filteredList != null) {
            if (filteredList.isNotEmpty()) {
                updateTable(filteredList)
            } else {
                AlertDialogBox().showDialog(this, "No matching data found")
            }
        }
    }
    private fun updateTable(filteredList: List<SerReqData>) {
        tableLayout.removeAllViews()
        createHeaderRow()
        filteredList.forEach { serviceReqData ->
            val row = createTableRow(serviceReqData)
            tableLayout.addView(row)
            row.setOnClickListener{
                val intent = Intent(applicationContext, ServiceRequestDetails::class.java)
                intent.putExtra("request_id", serviceReqData.request_id.orEmpty())
                intent.putExtra("merchant_id", serviceReqData.merchant_id.orEmpty())
                intent.putExtra("request_type", serviceReqData.request_type.orEmpty())
                intent.putExtra("request_description", serviceReqData.request_description.orEmpty())
                intent.putExtra("steps_to_reproduce", serviceReqData.steps_to_reproduce.orEmpty())
                intent.putExtra("error_message", serviceReqData.error_message.orEmpty())
                intent.putExtra("priority", serviceReqData.priority.orEmpty())
                intent.putExtra("contact_email", serviceReqData.contact_email.orEmpty())
                intent.putExtra("contact_phone", serviceReqData.contact_phone.orEmpty())
                intent.putExtra("additional_notes", serviceReqData.additional_notes.orEmpty())
                intent.putExtra("status", serviceReqData.status.orEmpty())
                intent.putExtra("approved_by", serviceReqData.approved_by.orEmpty())
                intent.putExtra("assign_to", serviceReqData.assign_to.orEmpty())
                intent.putExtra("entry_user", serviceReqData.entry_user.orEmpty())
                val requestDateString = (serviceReqData.request_date)
                val approvedDateString = (serviceReqData.approved_date)
                val assignedDateString = (serviceReqData.assigned_date)
                intent.putExtra("request_date", requestDateString)
                intent.putExtra("approved_date", approvedDateString)
                intent.putExtra("assigned_date", assignedDateString)
                intent.putExtra("Button","Update")
                startActivity(intent)
            }
        }
    }
    private fun createTableRow(userData: SerReqData): TableRow {
        val row = TableRow(this)
        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = params
        val userId = createTextView(this,userData.request_id!!)
        val requestDate = createTextView(this,userData.request_date.toString())
        val defaultDevice = createTextView(this,userData.status!!)
        val device1 = createTextView(this,userData.entry_user!!)
        val device2 = createTextView(this,userData.priority!!)
        row.addView(userId)
        row.addView(requestDate)
        row.addView(defaultDevice)
        row.addView(device1)
        row.addView(device2)
        isDataFiltered = true
        row.setOnClickListener {
        }
        return row
    }
    fun formatDate(originalDateString: String): String {
        // Define the original date format
        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)
        originalFormat.timeZone = TimeZone.getTimeZone("GMT")

        // Define the desired date format
        val desiredFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

        return try {
            // Parse the original date string into a Date object
            val date: Date = originalFormat.parse(originalDateString) ?: Date()
            // Format the Date object into the desired format
            desiredFormat.format(date)
        } catch (e: Exception) {
            // Handle parsing exceptions
       //     e.printStackTrace()
            ""
        }
    }
}