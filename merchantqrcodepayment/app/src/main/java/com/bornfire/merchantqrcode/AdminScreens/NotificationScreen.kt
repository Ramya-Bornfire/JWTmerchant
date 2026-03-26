package com.bornfire.merchantqrcode.AdminScreens

import android.content.Context
import android.content.Intent
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
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.NotifyData
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DetailScreen.NotifyDetailsScreen
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.TextforTable.createTextView
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationScreen : BaseActivity() {
    lateinit var backBtn: ImageView
    val merchantdata = SharedMerchantDataObj.merchantData
    var notifyList: List<NotifyData>? = null
    var addSRLNum: String = "${merchantdata?.merchant_user_id}NP01"
    lateinit var addBtn: Button
    lateinit var edFilter: EditText
    lateinit var tableLayout: TableLayout
    private var isDataFiltered = false
    lateinit var nodata: TextView
    lateinit var noti_help_image: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_screen)
        backBtn = findViewById(R.id.backBtn)
        noti_help_image = findViewById(R.id.noti_help_image)
        noti_help_image.setOnClickListener() {
            HelpInfo.getInfo(this, "30")
        }
        nodata = findViewById(R.id.emptyTextView)
        backBtn.setOnClickListener() {
            handleBackPress()
        }
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        edFilter = findViewById(R.id.editFilter)
        edFilter.setOnClickListener {
            edFilter.isFocusable = true  // Make EditText focusable
            edFilter.isFocusableInTouchMode = true  // Allow focus in touch mode
            edFilter.requestFocus()  // Request focus for EditText
            showKeyboard()  // Show the keyboard when EditText is clicked
        }
        addBtn = findViewById(R.id.addBtn)
        addBtn.setOnClickListener() {
            val intent = Intent(this, NotifyDetailsScreen::class.java)
            val srl = intent.putExtra("recSrl", addSRLNum)
            startActivity(intent)
        }
        tableLayout = findViewById(R.id.tableLayout)
        getSupportActionBar()?.hide()
        if (NetworkUtils.isNetworkAvailable(this)) {
            fetchNotifyUnit()
        } else {
            NetworkUtils.NoInternetAlert(this)
        }
        edFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (edFilter.text.toString().length > 0) {
                    val filterQuery = edFilter.text.toString().trim()
                    filterData(filterQuery)
                }
                else{
                    fetchNotifyUnit()
                }
            }
        })
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }

    private fun filterData(query: String) {
        val filteredList = notifyList?.let { list ->
            list.filter {
                it.record_srl_no?.contains(query, ignoreCase = true) == true ||
                        (it.record_date)?.contains(query, ignoreCase = true) == true ||
                        it.notification_event_desc?.contains(query, ignoreCase = true) == true ||
                        it.usercategory?.contains(query, ignoreCase = true) == true ||
                        it.channel?.contains(query, ignoreCase = true) == true
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

    private fun updateTable(filteredList: List<NotifyData>) {
        tableLayout.removeAllViews()
        createHeaderRow()
        // Iterate through filtered list and add rows to the table
        filteredList.forEach { alert ->
            val row = createTableRow(alert)
            tableLayout.addView(row)
            row.setOnClickListener() {
                val intent = Intent(applicationContext, NotifyDetailsScreen::class.java)
                intent.putExtra("recSrl", alert.record_srl_no)
                intent.putExtra("recDate", alert.record_date)
                intent.putExtra("eveNo", alert.notification_event_no)
                intent.putExtra("tranCate", alert.tran_category)
                intent.putExtra("notiLimit", alert.notification_limit ?: "")
                intent.putExtra("eveDesc", alert.notification_event_desc)
                intent.putExtra("notiUser1", alert.notification_user_1 ?: "")
                intent.putExtra("notiUser2", alert.notification_user_2 ?: "")
                intent.putExtra("notiUser3", alert.notification_user_3 ?: "")
                intent.putExtra("email1", alert.notification_email_1 ?: "")
                intent.putExtra("email2", alert.notification_email_2 ?: "")
                intent.putExtra("email3", alert.notification_email_3 ?: "")
                intent.putExtra("countrycode1", alert.countrycode_1 ?: "")
                intent.putExtra("countrycode2", alert.countrycode_2 ?: "")
                intent.putExtra("countrycode3", alert.countrycode_3 ?: "")
                intent.putExtra("mob1", alert.notification_mobile_1 ?: "")
                intent.putExtra("mob2", alert.notification_mobile_2 ?: "")
                intent.putExtra("mob3", alert.notification_mobile_3 ?: "")
                intent.putExtra("sms", alert.notification_sms_flg ?: "")
                intent.putExtra("email", alert.notification_email_flg)
                intent.putExtra("alertt", alert.alert_flg)
                intent.putExtra("usercate", alert.usercategory)
                intent.putExtra("chanel", alert.channel)
                intent.putExtra("StartDate", alert.start_date)
                intent.putExtra("EndDate", alert.end_date)
                intent.putExtra("Frequency", alert.frequency)
                intent.putExtra("Button", "Update")
                startActivity(intent)
            }
        }
    }

    private fun createTableRow(userData: NotifyData): TableRow {
        // Create a row and add TextViews for each field in the data model
        val row = TableRow(this)
        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        row.layoutParams = params
        val userId = createTextView(this,userData.record_srl_no)
        val formattedDate = userData.record_date.toString()
        val recDate = createTextView(this,formattedDate)
        val defaultDevice = createTextView(this,userData.notification_event_desc)
        val device1 = createTextView(this,userData.usercategory)
        val device2 = createTextView(this,userData.channel)

        row.addView(userId)
        row.addView(recDate)
        row.addView(defaultDevice)
        row.addView(device1)
        row.addView(device2)
        isDataFiltered = true
        row.setOnClickListener {
            // Handle row click
        }
        return row
    }
    private fun createHeaderRow() {
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange))
        val headers = arrayOf("SRL", "DATE", "NOTIFICATION EVENT", "USER CATEGORY", "CHANNEL")
        headers.forEach { header ->
            val textView = createTextView(this,header)
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            textView.textSize = 15f
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)
    }
    private fun fetchNotifyUnit() {
        val call: Call<List<NotifyData>> = ApiClient.apiService.getNotificationList(
            merchantdata?.merchant_user_id!!,
            merchantdata.unit_id!!
        )
        tableLayout.removeAllViews()
        createHeaderRow()
        call.enqueue(object : Callback<List<NotifyData>> {
            override fun onResponse(
                call: Call<List<NotifyData>>,
                response: Response<List<NotifyData>>
            ) {
                    if (response.isSuccessful) {
                        notifyList = response.body()!!
                 //       println("PRINTTTTTTTTTT  " + notifyList.toString())
                        notifyList = notifyList!!.sortedByDescending { it.record_srl_no }
                        if (notifyList!!.isNotEmpty()) {
                            edFilter.visibility = View.VISIBLE
                            val lastRequestId = notifyList!!.firstOrNull()?.record_srl_no
                            val nextRecSrl = getNextRecSrl(lastRequestId!!)
                            addSRLNum = nextRecSrl
                        } else {
                            nodata.visibility = View.VISIBLE
                            edFilter.visibility = View.GONE
                        }
                        notifyList?.forEach { alert ->
                            val tableLayout: TableLayout = findViewById(R.id.tableLayout)
                            val row = TableRow(this@NotificationScreen)
                            val params = TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                            row.layoutParams = params

                            val recSrl = createTextView(this@NotificationScreen,alert.record_srl_no)
                            val formattedDate = alert.record_date
                            val recDate = createTextView(this@NotificationScreen,formattedDate)
                            val eveDesc = createTextView(this@NotificationScreen,alert.notification_event_desc)
                            val usercate = createTextView(this@NotificationScreen,alert.usercategory)
                            val chanel = createTextView(this@NotificationScreen,alert.channel)

                            fun getNextRecSrl(currentRecSrl: String): String {
                                val numericPart = currentRecSrl.filter { it.isDigit() }
                                val nextNumber = (numericPart.toIntOrNull() ?: 0) + 1
                                return "NP" + nextNumber.toString().padStart(2, '0')
                            }

                            val currentRecSrl = alert.record_srl_no
                            val nextRecSrl = getNextRecSrl(currentRecSrl)
                            addSRLNum = nextRecSrl
                            row.addView(recSrl)
                            row.addView(recDate)
                            row.addView(eveDesc)
                            row.addView(usercate)
                            row.addView(chanel)

                            tableLayout.addView(row)
                            NetworkUtils.hideProgress(this@NotificationScreen)
                            row.setOnClickListener() {
                                val intent =
                                    Intent(applicationContext, NotifyDetailsScreen::class.java)
                                intent.putExtra("recSrl", alert.record_srl_no)
                                intent.putExtra("recDate", alert.record_date)
                                intent.putExtra("eveNo", alert.notification_event_no)
                                intent.putExtra("tranCate", alert.tran_category)
                                intent.putExtra("notiLimit", alert.notification_limit ?: "")
                                intent.putExtra("eveDesc", alert.notification_event_desc)
                                intent.putExtra("notiUser1", alert.notification_user_1 ?: "")
                                intent.putExtra("notiUser2", alert.notification_user_2 ?: "")
                                intent.putExtra("notiUser3", alert.notification_user_3 ?: "")
                                intent.putExtra("email1", alert.notification_email_1 ?: "")
                                intent.putExtra("email2", alert.notification_email_2 ?: "")
                                intent.putExtra("email3", alert.notification_email_3 ?: "")
                                intent.putExtra("countrycode1", alert.countrycode_1 ?: "")
                                intent.putExtra("countrycode2", alert.countrycode_2 ?: "")
                                intent.putExtra("countrycode3", alert.countrycode_3 ?: "")
                                intent.putExtra("mob1", alert.notification_mobile_1 ?: "")
                                intent.putExtra("mob2", alert.notification_mobile_2 ?: "")
                                intent.putExtra("mob3", alert.notification_mobile_3 ?: "")
                                intent.putExtra("sms", alert.notification_sms_flg ?: "")
                                intent.putExtra("email", alert.notification_email_flg)
                                intent.putExtra("alertt", alert.alert_flg)
                                intent.putExtra("usercate", alert.usercategory)
                                intent.putExtra("chanel", alert.channel)
                                intent.putExtra("StartDate", alert.start_date)
                                intent.putExtra("EndDate", alert.end_date)
                                intent.putExtra("Frequency", alert.frequency)
                                intent.putExtra("Button", "Update")
                                startActivity(intent)
                            }
                        }
                        // Update UI or log the data
                    }
                else {
                    Toast.makeText(this@NotificationScreen, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                //    println("Error: ${response.code()}")
                    NetworkUtils.hideProgress(this@NotificationScreen)
                }
            }

            override fun onFailure(call: Call<List<NotifyData>>, t: Throwable) {
              //  t.printStackTrace()
                NetworkUtils.hideProgress(this@NotificationScreen)
                Toast.makeText(
                    this@NotificationScreen,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    fun getNextRecSrl(currentRecSrl: String): String {
        val numericPart = currentRecSrl.filter { it.isDigit() }
        val prefix = currentRecSrl.dropLast(numericPart.length)
        val nextNumber = (numericPart.toIntOrNull() ?: 0) + 1
        return prefix + nextNumber.toString().padStart(2, '0')
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

    fun handleBackPress() {
            val intent = Intent(this, AdminActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
    }

}