package com.bornfire.merchantqrcode.AdminScreens

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.*
import com.bornfire.merchantqrcode.DataModel.ChargeData
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.DataModel.TransData
import com.bornfire.merchantqrcode.DetailScreen.ChargeBackDetailsActivity
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Utils.EditNonEdit
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.NotifyDownload
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.showDatePickerDialog
import com.bornfire.merchantqrcode.Utils.TextforTable.createAmountTextView
import com.bornfire.merchantqrcode.Utils.TextforTable.createTextView
import com.bornfire.merchantqrcode.Utils.TextforTable.formatNumberWithGroups
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.bornfire.merchantqrcode.BuildConfig
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Chargebacks : BaseActivity() {
    val merchantdata = SharedMerchantDataObj.merchantData
    val userdata = SharedUserDataObj.userData
    val usercategory= SharedusercatDataObj.UserCategory
    lateinit var merchantid:String
    lateinit var userid:String
    lateinit var chargeList: List<ChargeData>
    lateinit var  tableLayout: TableLayout
    lateinit var transList: List<TransData>
    lateinit var backBtn:ImageView
    lateinit var headertext:TextView
    lateinit var nodata:TextView
    lateinit var editFilter:EditText
    lateinit var cbFrom:String
    lateinit var cbTo:String
    lateinit var cur_date:EditText
    lateinit var pdf_dwn:ImageView
    lateinit var cb_help_image:ImageView
    var screenid:String = "5"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chargebacks)
        supportActionBar?.hide()
        tableLayout = findViewById(R.id.tableLayout)
        nodata=findViewById(R.id.emptyTextView)
        backBtn=findViewById(R.id.backBtn)
        editFilter = findViewById(R.id.editFilter)
        headertext=findViewById(R.id.headertext)
        pdf_dwn = findViewById(R.id.pdf_dwn)
        cur_date = findViewById(R.id.cur_date)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentDate: Date = calendar.time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateString: String = dateFormat.format(currentDate)
        cur_date.setText(dateString)
        cbFrom = dateString
        cbTo = dateString
        pdf_dwn.setOnClickListener(){
            callPdfDialog()
        }
        cb_help_image= findViewById(R.id.cb_help_image)
        cb_help_image.setOnClickListener(){
            HelpInfo.getInfo(this,screenid)
        }
        cur_date.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1) // Adding 1 to the month as it's 0-based
                val selectedDate = "$formattedDay-$formattedMonth-$selectedYear"
                cur_date.setText(selectedDate)
                editFilter.text.clear()
                cbFrom = selectedDate
                cbTo = selectedDate
                val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
                if(usercategory?.user_category == "Representative"){
                    // Handle the spinner's selection and perform the appropriate action
                    val spinnerOptions: Spinner = findViewById(R.id.spinnerOptions)
                    when (spinnerOptions.selectedItemPosition) {
                        0 -> updateHeaderAndFetchData(getString(R.string.pending_charge_back_transaction)) {
                            fetchPendingChargeBack()
                        }
                        1 -> updateHeaderAndFetchData(getString(R.string.reverted_charge_back_transaction)) {
                            fetchRevertChargeBack()
                        }
                        2 -> updateHeaderAndFetchData(getString(R.string.all_charge_back_transaction)) {
                            fetchAllChargeBack()
                        }
                        3 -> updateHeaderAndFetchData(getString(R.string.all_charge_back_transaction)) {
                            fetchUserChargeBack()
                        }
                        else -> fetchChargeBacksData() // Fallback to original function if no spinner selection matches
                    }
                }
                else{
                        fetchUserChargeBack()
                }
            }, year, month, day)
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            } })
        backBtn.setOnClickListener {
            handleBackPress()
        }
        editFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val searchText = editFilter.text.toString().trim()
                val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
                if (isTablet) {
                    if(usercategory?.user_category == "Representative"){
                        if(editFilter.text.toString().length>0) {
                            val filterQuery = searchText
                            filterRepData(filterQuery) }
                        else{
                            val spinnerOptions: Spinner = findViewById(R.id.spinnerOptions)
                            when (spinnerOptions.selectedItemPosition) {
                                0 -> updateHeaderAndFetchData(getString(R.string.pending_charge_back_transaction)) {
                                    fetchPendingChargeBack()
                                }
                                1 -> updateHeaderAndFetchData(getString(R.string.reverted_charge_back_transaction)) {
                                    fetchRevertChargeBack()
                                }
                                2 -> updateHeaderAndFetchData(getString(R.string.all_charge_back_transaction)) {
                                    fetchAllChargeBack()
                                }
                                3 -> updateHeaderAndFetchData(getString(R.string.all_charge_back_transaction)) {
                                    fetchUserChargeBack()
                                }
                                else -> fetchChargeBacksData() // Fallback to original function if no spinner selection matches
                            }
                        }
                    }
                    else{
                        if(editFilter.text.toString().length>0) {
                            val filterQuery = searchText
                            filterData1(filterQuery) }
                        else{
                            fetchUserChargeBack()
                        }
                    }
                }
                else{
                    if(editFilter.text.toString().length>0) {
                        val filterQuery = searchText
                        filterData1(filterQuery)}
                    else{
                       fetchUserChargeBack()
                    }
                }
            }
        })
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (NetworkUtils.isNetworkAvailable(this@Chargebacks)) {
            fetchChargeBacksData()
        }
        else {
            NetworkUtils.NoInternetAlert(this@Chargebacks)
        }
    }
    private fun updateHeaderAndFetchData(headerText: String, fetchFunction: () -> Unit) {
        headertext.text = headerText
        tableLayout.removeAllViews()
        createheaderfortab()
        fetchFunction()
    }
    private fun createheaderformobile() {
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(ContextCompat.getColor(this,R.color.liteOrange)) // Replace with the actual color resource
        val headers = arrayOf("TRAN DATE", "BILL NUMBER","AMOUNT","REMARK")
        for (header in headers) {
            val textView = createTextView(this,header)
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterData1(query: String?) {
        // Ensure query is not null and normalize it
        val normalizedQuery = query?.trim()?.lowercase() ?: return
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

        val filteredUserList = transList!!.filter { it.tran_status == "SUCCESS" }
        val filteredList = filteredUserList?.filter {
            // Extract fields with null-safe checks and normalize them
            val tranDate = it.tran_date?.trim()?.lowercase() ?: ""
            val merchantBillNumber = it.merchant_bill_number?.trim()?.lowercase() ?: ""

            // Safely parse amounts to Double and format to two decimal places
            val tranAmount = it.tran_amount?.let { amount ->
                try {
                    String.format("%.2f", amount.toDouble())
                } catch (e: NumberFormatException) {
                    "0.00" // Fallback value if parsing fails
                }
            } ?: "0.00"

            val tranStatus = it.reversal_remarks?.trim()?.lowercase() ?: ""
            val msgref = it.sequence_unique_id?.toString()?.trim()?.lowercase() ?: ""
            val revdate = it.reversal_date?.trim()?.lowercase() ?: ""

            // Safely parse reversal amounts to Double and format to two decimal places
            val revAmt = it.reversal_amount?.let { amount ->
                try {
                    String.format("%.2f", amount.toDouble())
                } catch (e: NumberFormatException) {
                    "0.00" // Fallback value if parsing fails
                }
            } ?: "0.00"

            val revRemarks = it.reversal_remarks?.trim()?.lowercase() ?: ""

            // Check if the normalized query is contained in any of the fields
            if (isTablet) {
                merchantBillNumber.contains(normalizedQuery) ||
                        tranAmount.contains(normalizedQuery) ||
                        tranDate.contains(normalizedQuery) ||
                        msgref.contains(normalizedQuery) ||
                        revdate.contains(normalizedQuery) ||
                        revAmt.contains(normalizedQuery) ||
                        revRemarks.contains(normalizedQuery) ||
                        tranStatus.contains(normalizedQuery)
            } else {
                // Filtering logic for non-tablets
                merchantBillNumber.contains(normalizedQuery) ||
                        tranAmount.contains(normalizedQuery) ||
                        tranDate.contains(normalizedQuery) ||
                        tranStatus.contains(normalizedQuery)
            }
        }

        // Handle the filtered list
        if (!filteredList.isNullOrEmpty()) {
            updateTable1(filteredList)
        } else {
            AlertDialogBox().showDialog(this, "No matching data found")
        }
    }

    private fun updateTable1(filteredList: List<TransData>) {
        tableLayout.removeAllViews()
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(
            ContextCompat.getColor(this, R.color.liteOrange)
        ) // Replace with the actual color resource

        // Create TextViews for each header column

        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        val headers = if(isTablet){arrayOf("TRAN DATE","MESSAGE ID", "BILL No","BILL AMOUNT","REVERSAL DATE","REVERSAL AMOUNT","REVERSAL REMARK")}
        else{ arrayOf("TRAN DATE", "BILL NUMBER","AMOUNT","REMARK") }

        for (header in headers) {
            val textView = createTextView(this,header)
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            headerRow.addView(textView)
        }
        // Add the header row to the table layout
        tableLayout.addView(headerRow)// Clear existing rows

        // Iterate through filtered list and add rows to the table
        filteredList.forEach { transList ->
            val row = createTableRow1(transList)
            tableLayout.addView(row)
            row.setOnClickListener(){
                EditNonEdit.hideKeyboard(editFilter)
                val intent = Intent(applicationContext, ChargeBackDetailsActivity::class.java)
                intent.putExtra("TransDate", transList.tran_date)
                intent.putExtra("MsgId", transList.sequence_unique_id)
                intent.putExtra("BillNo", transList.merchant_bill_number)
                intent.putExtra("BillDate",transList.tran_date)
                val formattedAmount = formatNumberWithGroups(transList.tran_amount.toString().toDouble())
                intent.putExtra("BillAmt", formattedAmount)
                intent.putExtra("Currency", transList.tran_currency)
                intent.putExtra("Remarks", transList.reversal_remarks?:" ")
                intent.putExtra("Revert","N")
                intent.putExtra("Initiate","Y")
                intent.putExtra("APPROVEDUSER", transList.auth_user)
                intent.putExtra("APPROVEDDATE",transList.auth_time)
                startActivity(intent)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTableRow1(transList: TransData?): TableRow {
        val row = TableRow(this)
        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = params
        val timestamp1 =  transList?.tran_date
        val billDate = createTextView(this,timestamp1.toString())
        val formattedAmount1 = formatNumberWithGroups(transList?.tran_amount!!.toDouble())
        val msgId = createTextView(this,transList?.sequence_unique_id)
        val reversalDate = transList.reversal_date?.toString() ?: ""
        val reversalAmt = formatNumberWithGroups(transList.reversal_amount?.toDouble() ?: 0.0)
        val billAmount = createAmountTextView(this,formattedAmount1)
        val merchantBillNumber = createTextView(this,transList.merchant_bill_number!!)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if(isTablet){
            row.addView(billDate)
            row.addView(msgId)
            row.addView(merchantBillNumber)
            row.addView(billAmount)
            row.addView(createTextView(this,reversalDate))
            row.addView(createAmountTextView(this, reversalAmt))
            row.addView(createTextView(this, transList.reversal_remarks ?: ""))
        }
        else{
            row.addView(billDate)
            row.addView(merchantBillNumber)
            row.addView(billAmount)
            row.addView(createTextView(this, transList.reversal_remarks ?: ""))
        }
        return row
    }
    private fun createheaderfortab() {
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(ContextCompat.getColor(this,R.color.liteOrange)) // Replace with the actual color resource
        val headers = arrayOf ("TRAN DATE", "MESSAGE ID", "BILL NO", "BILL AMOUNT","REVERSAL DATE","REVERSAL AMOUNT", "REVERSAL REMARK")
        for (header in headers) {
            val textView = createTextView(this,header)
            textView.textSize = 15f
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)
    }
    private fun filterRepData(query: String) {
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        tableLayout.removeAllViews()
        createheaderfortab() // Function to create table headers

        // Filter the list based on the query and conditions
        val filteredList = chargeList.filter { chargeData ->
            val formattedAmount = chargeData.tran_amount?.let { amount ->
                formatNumberWithGroups(amount.toDouble())
            } ?: "0.00" // Format amount, default to "0.00" if null

            // Check if any of the fields match the query
            val matchesQuery = chargeData.tran_date?.contains(query, ignoreCase = true) == true ||
                    chargeData.sequence_unique_id?.contains(query, ignoreCase = true) == true ||
                    chargeData.merchant_bill_number?.contains(query, ignoreCase = true) == true ||
                    formattedAmount.contains(query, ignoreCase = true) ||
                    chargeData.reversal_date?.contains(query,ignoreCase = true)==true||
                    chargeData.reversal_remarks?.contains(query, ignoreCase = true) == true

            // Determine if the charge data matches the selected filter criteria
            val matchesFilter = when (headertext.text) {
                "PENDING CHARGE BACK TRANSACTION" -> chargeData.reversal_remarks.equals("PENDING", ignoreCase = true)
                "ALL CHARGE BACK TRANSACTION" -> true
                "REVERTED CHARGE BACK TRANSACTION" -> chargeData.reversal_remarks.equals("REVERTED", ignoreCase = true)
                else -> false
            }
            // Return true only if both query and filter match
            matchesQuery && matchesFilter
        }
        // Populate the filtered data into the TableLayout
        filteredList.forEach { chargeData ->
            val row = TableRow(this@Chargebacks).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }
            val transId = createTextView(this@Chargebacks, chargeData.tran_date)
            val msgid = createTextView(this@Chargebacks, chargeData.sequence_unique_id ?: " ")
            val billNo = createTextView(this@Chargebacks, chargeData.merchant_bill_number ?: " ")
            val billAmt = formatNumberWithGroups(chargeData.tran_amount?.toDouble() ?: 0.0)
            val reversalAmountString = chargeData.reversal_amount?.toString()
            val reversalAmount = reversalAmountString?.toDoubleOrNull() ?: 0.0
            val formattedReversalAmount = formatNumberWithGroups(reversalAmount)
            val revAmt = createAmountTextView(this@Chargebacks, formattedReversalAmount)
            val reversalremark = createTextView(this@Chargebacks, chargeData.reversal_remarks?.toString())
            row.addView(transId)
            row.addView(msgid)
            row.addView(billNo)
            row.addView(createAmountTextView(this, billAmt))
            row.addView(createTextView(this@Chargebacks, chargeData.reversal_date?.toString() ?: ""))
            row.addView(revAmt)
            row.addView(reversalremark)
            // Add row to the table layout
            tableLayout.addView(row)
            // Set click listener for navigating to ChargeBackDetailsActivity
            row.setOnClickListener {
                EditNonEdit.hideKeyboard(editFilter)
                val intent = Intent(applicationContext, ChargeBackDetailsActivity::class.java).apply {
                    putExtra("TransDate", chargeData.tran_date)
                    putExtra("MsgId", chargeData.sequence_unique_id)
                    putExtra("AudRef", chargeData.tran_audit_number)
                    putExtra("BillNo", chargeData.merchant_bill_number)
                    putExtra("BillDate", chargeData.tran_date)
                    putExtra("BillAmt", billAmt)
                    putExtra("Currency", chargeData.tran_currency)
                    putExtra("Remarks", chargeData.reversal_remarks)
                    putExtra("Revert", if (chargeData.reversal_remarks.equals("REVERTED", ignoreCase = true)) "Y" else "N")
                    putExtra("Initiate", "N")
                    putExtra("APPROVEDUSER", chargeData.auth_user)
                    putExtra("APPROVEDDATE", chargeData.auth_time)
                }
                startActivity(intent)
            }
        }
    }
    private fun fetchUserChargeBack() {
        pdf_dwn.visibility = View.GONE
        tableLayout.removeAllViews()
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if(isTablet) {

             createheaderfortab()
        }else{
            createheaderformobile()
        }

        val call: Call<List<TransData>> = ApiClient.apiService.getTransactionDetails(merchantid,userid,cbFrom,cbTo,"MOBILEVIEW")
        call.enqueue(object : Callback<List<TransData>> {
            override fun onResponse(
                call: Call<List<TransData>>,
                response: Response<List<TransData>>
            ) {
                if (response.isSuccessful) {
                    NetworkUtils.hideProgress(this@Chargebacks)
                    transList = response.body()!!
              //      println("ChargeBack List --->>> ${transList}")

                    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    transList = transList.sortedByDescending {
                        LocalDate.parse(it.tran_date, dateFormatter)
                    }
                    val filteredUserList = transList!!.filter { it.tran_status == "SUCCESS" }
                    if(filteredUserList!!.isNotEmpty()){
                        editFilter.visibility = View.VISIBLE
                        nodata.visibility=View.INVISIBLE
                    }
                    else{
                        editFilter.visibility = View.GONE
                        nodata.visibility=View.VISIBLE
                    }
                    filteredUserList?.forEach{ transList->
                        val row = TableRow(this@Chargebacks)
                        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                        row.layoutParams = params
                        val remitterAccount = createTextView(this@Chargebacks,transList.ipsx_account_name)
                        val userId = createTextView(this@Chargebacks,transList.user_id.toString())
                        val timestamp = transList.tran_date ?: ""
                        val billDate = createTextView(this@Chargebacks,timestamp)
                        val formattedAmount1 = formatNumberWithGroups(transList.tran_amount!!.toDouble())
                        val billAmount = createAmountTextView(this@Chargebacks,formattedAmount1)
                        val msgid = createTextView(this@Chargebacks,transList.sequence_unique_id?:" ")
                        val merchantBillNumber = createTextView(this@Chargebacks,transList.merchant_bill_number?: "N/A")
                        val remark=createTextView(this@Chargebacks,transList.reversal_remarks)
                        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
                        if(isTablet) {
                            row.addView(billDate)
                            row.addView(msgid)
                            row.addView(merchantBillNumber)
                            row.addView(billAmount)
                            val timestamp = transList.reversal_date ?: ""
                            val revDate = createTextView(this@Chargebacks,timestamp)
                            row.addView(revDate)
                            val formattedAmount = transList.reversal_amount?.let {
                                formatNumberWithGroups(it.toDouble())
                            } ?: ""
                            val revAmount = createAmountTextView(this@Chargebacks,formattedAmount)
                            row.addView(revAmount)
                            val revRemarks = createTextView(this@Chargebacks,transList.reversal_remarks ?: "")
                            row.addView(revRemarks)

                            tableLayout.addView(row)
                        }
                        else{
                            row.addView(billDate)
                            row.addView(merchantBillNumber)
                            row.addView(billAmount)
                            val tranRemark = createTextView(this@Chargebacks,transList.reversal_remarks ?: "")
                            row.addView(tranRemark)
                            tableLayout.addView(row)
                        }
                        row.setOnClickListener(){
                            EditNonEdit.hideKeyboard(editFilter)
                            val intent = Intent(applicationContext, ChargeBackDetailsActivity::class.java)
                            intent.putExtra("TransDate", transList.tran_date)
                            intent.putExtra("MsgId", transList.sequence_unique_id)
                            intent.putExtra("BillNo", transList.merchant_bill_number)
                            intent.putExtra("BillDate",transList.tran_date)
                            val formattedAmount = formatNumberWithGroups(transList.tran_amount.toString().toDouble())
                            intent.putExtra("BillAmt", formattedAmount)
                            intent.putExtra("Currency", transList.tran_currency)
                            intent.putExtra("Remarks", transList.reversal_remarks?:" ")
                            intent.putExtra("Revert","N")
                            intent.putExtra("Initiate","Y")
                            intent.putExtra("APPROVEDUSER", transList.auth_user)
                            intent.putExtra("APPROVEDDATE",transList.auth_time)
                            startActivity(intent)
                        }
                    }
                }
                else {
                    Toast.makeText(this@Chargebacks, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    NetworkUtils.hideProgress(this@Chargebacks)
              //      println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<TransData>>, t: Throwable) {
             //   t.printStackTrace()
                NetworkUtils.hideProgress(this@Chargebacks)
                Toast.makeText(this@Chargebacks,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun fetchChargeBackList(apiCall: Call<List<ChargeData>>, isReverted: Boolean) {
        screenid = when {
            isReverted -> "24"
            else -> "26"
        }
        tableLayout.removeAllViews()
        createheaderfortab()
        apiCall.enqueue(object : Callback<List<ChargeData>> {
            override fun onResponse(call: Call<List<ChargeData>>, response: Response<List<ChargeData>>) {
                if (response.isSuccessful) {
                    chargeList = response.body()!!
                  //  println("Chargeback List ---> ${chargeList}")
                    chargeList = chargeList.sortedByDescending { it.tran_date }

                    if (chargeList.isNotEmpty()) {
                        if (chargeList.size == 100) {
                            AlertDialog.Builder(this@Chargebacks)
                                .setTitle("Alert")
                                .setMessage("You will be able to view only your latest 100 charge back list. For the complete list, please use the PDF option.")
                                .setNegativeButton("Ok", null)
                                .show()
                        }
                        editFilter.visibility = View.VISIBLE
                        nodata.visibility = View.INVISIBLE

                        chargeList.forEach { chargeData ->
                            addTableRow(chargeData, isReverted)
                        }
                    }
                    else{
                        editFilter.visibility = View.INVISIBLE
                        nodata.visibility = View.VISIBLE
                    }
                    NetworkUtils.hideProgress(this@Chargebacks)
                }
                else {
                    Toast.makeText(this@Chargebacks, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                   // println("Error: ${response.code()}")
                    NetworkUtils.hideProgress(this@Chargebacks)
                }
            }
            override fun onFailure(call: Call<List<ChargeData>>, t: Throwable) {
              //  t.printStackTrace()
                NetworkUtils.hideProgress(this@Chargebacks)
                Toast.makeText(this@Chargebacks, "Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun addTableRow(chargeData: ChargeData, isReverted: Boolean) {
        val row = TableRow(this@Chargebacks)
        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = params

        val transId = createTextView(this@Chargebacks, chargeData.tran_date)
        val msgid = createTextView(this@Chargebacks, chargeData.sequence_unique_id ?: " ")
        val billNo = createTextView(this@Chargebacks, chargeData.merchant_bill_number ?: " ")
        val billamt = chargeData.tran_amount?.toString() ?: "0"
        val formattedAmount = formatNumberWithGroups(billamt.toDouble())
        val billAmt = createAmountTextView(this@Chargebacks, formattedAmount)
        val reversalAmountString = chargeData.reversal_amount?.toString()
        val reversalAmount = reversalAmountString?.toDoubleOrNull() ?: 0.0
        val formattedReversalAmount = formatNumberWithGroups(reversalAmount)
        val revAmt = createAmountTextView(this@Chargebacks, formattedReversalAmount)
        val reversalremark = createTextView(this@Chargebacks, chargeData.reversal_remarks?.toString())

        row.addView(transId)
        row.addView(msgid)
        row.addView(billNo)
        row.addView(billAmt)
        row.addView(createTextView(this@Chargebacks, chargeData.reversal_date?.toString() ?: ""))
        row.addView(revAmt)
        row.addView(reversalremark)

        tableLayout.addView(row)

        row.setOnClickListener {
            EditNonEdit.hideKeyboard(editFilter)
            val intent = Intent(applicationContext, ChargeBackDetailsActivity::class.java).apply {
                putExtra("TransDate", chargeData.tran_date)
                putExtra("MsgId", chargeData.sequence_unique_id)
                putExtra("AudRef", chargeData.tran_audit_number)
                putExtra("BillNo", chargeData.merchant_bill_number)
                putExtra("BillDate", chargeData.tran_date)
                putExtra("BillAmt", formattedAmount)
                putExtra("Currency", chargeData.tran_currency)
                putExtra("Remarks", chargeData.reversal_remarks)
                putExtra("Revert", if (isReverted) "N" else "Y")
                putExtra("Initiate", "N")
                putExtra("APPROVEDUSER", chargeData.auth_user)
                putExtra("APPROVEDDATE", chargeData.auth_time)
            }
            startActivity(intent)
        }
    }

    private fun fetchRevertChargeBack() {
        val call: Call<List<ChargeData>> = ApiClient.apiService.getRevertedChargebackList(
            merchantid, merchantdata?.unit_id.toString(), cbFrom, cbTo
        )
        fetchChargeBackList(call, isReverted = true)
    }

    private fun fetchPendingChargeBack() {
        val call: Call<List<ChargeData>> = ApiClient.apiService.getPendingChargebackList(
            merchantid, merchantdata?.unit_id.toString(), cbFrom, cbTo
        )
        fetchChargeBackList(call, isReverted = false)
    }

    private fun fetchAllChargeBack() {
        val call: Call<List<ChargeData>> = ApiClient.apiService.getChargeBackList(
            merchantid, merchantdata?.unit_id.toString(), cbFrom, cbTo
        )
        fetchChargeBackList(call, isReverted = false)
    }

    fun handleBackPress() {
        var ssfsdtring=usercategory?.user_category
        if (usercategory?.user_category == "Representative") {
                val intent = Intent(this, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
        }
        else{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchChargeBacksData(){
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if(isTablet) {
            if (usercategory?.user_category == "User") {
                headertext.text= getString(R.string.charge_back_transaction)
                createheaderfortab()
                fetchUserChargeBack()
                merchantid=userdata?.merchant_user_id.toString()
                userid=userdata?.user_id.toString()
            }
            else{
                merchantid=merchantdata?.merchant_user_id.toString()
                userid=merchantdata?.merchant_rep_id.toString()
                val spinnerOptions: Spinner = findViewById(R.id.spinnerOptions)
                spinnerOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        nodata.visibility=View.VISIBLE
                        when (position) {
                            0 -> {
                                headertext.text= getString(R.string.pending_charge_back_transaction)
                                tableLayout.removeAllViews()
                                createheaderfortab()
                                fetchPendingChargeBack()
                            }
                            1 -> {
                                headertext.text=getString(R.string.reverted_charge_back_transaction)
                                tableLayout.removeAllViews()
                                createheaderfortab()
                                fetchRevertChargeBack()
                            }
                            2 -> {
                                headertext.text=getString(R.string.all_charge_back_transaction)
                                tableLayout.removeAllViews()
                                createheaderfortab()
                                fetchAllChargeBack()
                            }
                            3->{
                                headertext.text=getString(R.string.all_charge_back_transaction)
                                tableLayout.removeAllViews()
                                createheaderfortab()
                                fetchUserChargeBack()
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
                spinnerOptions.visibility=View.VISIBLE
            }
        }
        else{
            merchantid=userdata?.merchant_user_id.toString()
            userid=userdata?.user_id.toString()
            createheaderformobile()
            fetchUserChargeBack()
        }
    }
    private fun genpdf(type:String,ispdf:Boolean) {
        val call: Call<List<ChargeData>> = when (type) {
            "PENDING" -> ApiClient.apiService.getPendingChargebackList(
                merchantid,
                merchantdata?.unit_id.toString(),
                cbFrom,
                cbTo
            )
            "REVERTED" -> ApiClient.apiService.getRevertedChargebackList(
                merchantid,
                merchantdata?.unit_id.toString(),
                cbFrom,
                cbTo
            )
            "ALL" -> ApiClient.apiService.getChargeBackList(
                merchantid,
                merchantdata?.unit_id.toString(),
                cbFrom,
                cbTo
            )
            else -> return // Exit function if no matching type
        }
        call.enqueue(object : Callback<List<ChargeData>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<ChargeData>>,
                response: Response<List<ChargeData>>
            ) {
                if (response.isSuccessful) {
                    chargeList = response.body()!!
               //     println("ChargeBack List -->>> ${chargeList}")
                    if(chargeList!!.isNotEmpty()){
                        if(ispdf){
                            generatePdfFromData(chargeList) }
                        else{
                            generateExcelFromData(chargeList) }
                    }
                    else{
                        AlertDialogBox().showDialog(this@Chargebacks,"No data available to generate the PDF.")
                    }
                } else {
                    Toast.makeText(this@Chargebacks, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    NetworkUtils.hideProgress(this@Chargebacks)
                    //println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<ChargeData>>, t: Throwable) {
            //    t.printStackTrace()
                NetworkUtils.hideProgress(this@Chargebacks)
                Toast.makeText(this@Chargebacks,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun generatePdfFromData(chargeDataList: List<ChargeData>) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            return
        }
        try {
            val cacheFolder = cacheDir
            val fileName = "Charge Back" + Encryption.generatePID()
            val pdfFile = File(cacheFolder, "$fileName.pdf")
            if (pdfFile.exists()) {
                pdfFile.delete()
            }
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            val drawable = ContextCompat.getDrawable(this, R.drawable.bob_pdf)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val imageData = ImageDataFactory.create(bitmap.toByteArray())
            val image = Image(imageData)
            image.setWidth(100f)
            image.setHeight(50f)

            // Define table parameters
            val headerFontSize = 10f
            val cellFontSize = 6f
            val rowsPerPage = 30
            val totalPages = Math.ceil(chargeDataList.size / rowsPerPage.toDouble()).toInt()

            for (page in 0 until totalPages) {
                if (page > 0) {
                    document.add(AreaBreak()) // Adds a new page
                }
                document.add(image)
                val table = Table(floatArrayOf(0.15f, 0.2f, 0.1f, 0.1f, 0.1f, 0.1f))
                table.setWidth(UnitValue.createPercentValue(100f))
                // Add table headers
                table.addHeaderCell(Cell().add(Paragraph("TRAN DATE").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("BILL NO").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("BILL AMOUNT").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("REVERSAL DATE").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("REVERSAL AMOUNT").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("REVERSAL REMARK").setFontSize(headerFontSize)))

                val start = page * rowsPerPage
                val end = Math.min(start + rowsPerPage, chargeDataList.size)

                for (i in start until end) {
                    val transaction = chargeDataList[i]
                    table.addCell(Cell().add(Paragraph(transaction.tran_date).setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.merchant_bill_number).setFontSize(cellFontSize)))
                    table.addCell(
                        Cell().add(Paragraph(transaction.bill_amount.toString() ?: "N/A")
                            .setFontSize(cellFontSize)
                            .setTextAlignment(TextAlignment.RIGHT))
                    )
                    table.addCell(Cell().add(Paragraph(transaction.reversal_date ?: "N/A").setFontSize(cellFontSize)))
                    table.addCell(
                        Cell().add(Paragraph(formatNumberWithGroups(transaction.reversal_amount?.toDouble() ?: 0.0))
                            .setFontSize(cellFontSize)
                            .setTextAlignment(TextAlignment.RIGHT)))
                    table.addCell(Cell().add(Paragraph(transaction.reversal_remarks).setFontSize(cellFontSize)))
                }

                // Add the table to the document
                document.add(table)
            }

            // Close the document after all pages are added
            document.close()

            // Reopen document to add page numbers
            val pdfReader = PdfReader(FileInputStream(pdfFile))
            val pdfWriterForPageNumbers = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocumentForPageNumbers = PdfDocument(pdfReader, pdfWriterForPageNumbers)
            val pageCount = pdfDocumentForPageNumbers.numberOfPages

            for (i in 1..pageCount) {
                val page = pdfDocumentForPageNumbers.getPage(i)
                val canvas = PdfCanvas(
                    page.newContentStreamBefore(),
                    page.resources,
                    pdfDocumentForPageNumbers
                )
                canvas.beginText()
                val pageNumberFont: PdfFont =
                    PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD)
                canvas.setFontAndSize(pageNumberFont, 10f)
                val pageSize = page.pageSize
                canvas.moveText((pageSize.width / 2 - 15).toDouble(), 30.0)
                canvas.showText("Page $i of $pageCount")
                canvas.endText()
            }
            pdfDocumentForPageNumbers.close()
            NetworkUtils.hideProgress(this@Chargebacks)
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", pdfFile)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
            // Show success message

          //  Toast.makeText(this, "PDF generated at ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
           // e.printStackTrace()
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_LONG).show()
        }
    }

    private fun Bitmap.toByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun generateExcelFromData(transDataList: List<ChargeData>) {
        // Check for write permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
            return
        }
        try {
            // Get the public Documents directory
            val documentsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            // Ensure the directory exists
            if (!documentsFolder.exists()) {
                documentsFolder.mkdirs()
            }
            // Generate a unique file name
            val fileName = "Transaction_Details_" + Encryption.generatePID()
            val excelFile = File(documentsFolder, "$fileName.xlsx")
            // Delete the file if it already exists
            if (excelFile.exists()) {
                excelFile.delete()
            }
            // Create a new workbook and a sheet named "Transaction Data"
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Transaction Data")
            // Set column widths (approximate, adjust as needed)
            val columnWidths = arrayOf(5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000)
            columnWidths.forEachIndexed { index, width -> sheet.setColumnWidth(index, width) }
            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf(
                "DATE", "MESSAGE REF", "MERCHANT BILL NUMBER", "BILL DATE", "BILL AMOUNT",
                "TRANSACTION AMOUNT", "CURRENCY", "REVERSAL DATE", "REVERSAL AMOUNT", "REVERSAL REMARK",
                "AUTHORIZED USER", "AUTHORIZED TIME")
            headers.forEachIndexed { index, title ->
                headerRow.createCell(index).setCellValue(title)
            }
            // Fill data rows
            transDataList.forEachIndexed { index, transaction ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(transaction.tran_date ?: "N/A")
                row.createCell(1).setCellValue(transaction.sequence_unique_id ?: "N/A")
                row.createCell(2).setCellValue(transaction.merchant_bill_number ?: "N/A")
                row.createCell(3).setCellValue(transaction.bill_date ?: "N/A")
                row.createCell(4).setCellValue(formatNumberWithGroups(transaction.bill_amount?.toDouble() ?: 0.0))
                row.createCell(5).setCellValue(formatNumberWithGroups(transaction.tran_amount?.toDouble() ?: 0.0))
                row.createCell(6).setCellValue(transaction.tran_currency ?: "N/A")
                row.createCell(7).setCellValue(transaction.reversal_date ?: "N/A")
                row.createCell(8).setCellValue(formatNumberWithGroups(transaction.reversal_amount?.toDouble() ?: 0.0))
                row.createCell(9).setCellValue(transaction.reversal_remarks ?: "N/A")
                row.createCell(10).setCellValue(transaction.auth_user ?: "N/A")
                row.createCell(11).setCellValue(transaction.auth_time ?: "N/A")
            }
            // Write the Excel file to disk
            FileOutputStream(excelFile).use { fileOut ->
                workbook.write(fileOut)
            }
            workbook.close()
            // Show success message
            Toast.makeText(this, "Excel generated at ${excelFile.absolutePath}", Toast.LENGTH_LONG).show()
            NotifyDownload.createNotificationChannel(this)
            NotifyDownload.openExcelFile(this,excelFile)
        }
        catch (e: Exception) {
           // e.printStackTrace()
            Toast.makeText(this, "Error generating Excel", Toast.LENGTH_LONG).show()
        }
    }
    private fun callPdfDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.tran_pdf, null)
        builder.setView(view)
        val pdfBtn: Button = view.findViewById(R.id.gen_pdf)
        val excelBtn: Button = view.findViewById(R.id.gen_excel)
        val fromdate: EditText = view.findViewById(R.id.tran_from_date)
        val todate: EditText = view.findViewById(R.id.tran_to_date)
        val alertDialog = builder.create()
        alertDialog.show()
        val window = alertDialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams
        val decorView = window?.decorView
        decorView?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_background))
        fromdate.setOnClickListener() {
            showDatePickerDialog(true, fromdate, todate,this,false)
        }
        todate.setOnClickListener() {
            showDatePickerDialog(false, fromdate, todate,this,false)
        }
        pdfBtn.setOnClickListener {
            NetworkUtils.showProgress(this@Chargebacks)
            if (usercategory?.user_category == "Representative") {
                if (headertext.text == "PENDING CHARGE BACK TRANSACTION") {
                    genpdf("PENDING",true)
                } else if (headertext.text == "ALL CHARGE BACK TRANSACTION") {
                    genpdf("ALL",true)
                } else if (headertext.text == "REVERTED CHARGE BACK TRANSACTION") {
                    genpdf("REVERTED",true)
                }
                alertDialog.dismiss()
            }
        }
            excelBtn.setOnClickListener {
                if (headertext.text == "PENDING CHARGE BACK TRANSACTION") {
                    genpdf("PENDING",false)
                } else if (headertext.text == "ALL CHARGE BACK TRANSACTION") {
                    genpdf("ALL",false)
                } else if (headertext.text == "REVERTED CHARGE BACK TRANSACTION") {
                    genpdf("REVERTED",false)
                }
                alertDialog.dismiss()
            }
    }
}