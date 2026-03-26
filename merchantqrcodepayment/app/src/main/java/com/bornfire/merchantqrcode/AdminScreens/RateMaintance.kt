package com.bornfire.merchantqrcode.AdminScreens


import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.Rate
import com.bornfire.merchantqrcode.DetailScreen.Ratedetails

import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.TextforTable.createTextView
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RateMaintance : BaseActivity() {
    lateinit var addbtn: Button
    private lateinit var edFilter :EditText
    lateinit var rateData: List<Rate>
    lateinit var  tableLayout: TableLayout
    private var isDataFiltered = false
    lateinit var backBtn:ImageView
    lateinit var imgview : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ratemaintance)
        backBtn=findViewById(R.id.backBtn)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        backBtn.setOnClickListener(){
           finish() }
        edFilter = findViewById(R.id.editFilter)
        tableLayout = findViewById(R.id.tableLayout)
        //set the orientation for the tablet and mobile devices
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        imgview = findViewById<ImageView>(R.id.imageView)
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        supportActionBar?.hide()
        //add button navigate to rate details screen to add the rate data
        addbtn = findViewById(R.id.addBtn)
        addbtn.setOnClickListener(){
            val intent = Intent(this, Ratedetails::class.java)
            intent.putExtra("Button","Submit")
            startActivity(intent)
        }
        if (NetworkUtils.isNetworkAvailable(this)) {
            fetchData()
        } else {
            NetworkUtils.NoInternetAlert(this)
        }
        //create the table header
        if (isTablet) {
            val headerRow = TableRow(this)
            headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange)) // Replace with the actual color resource
            // Create TextViews for each header column
            val headers = arrayOf("SRL","Bill Currency", "Settle Currency", "Rate", "Effective Date", "Current Date")
            for (header in headers) {
                val textView = createTextView(this,header)
                textView.setTypeface(null, android.graphics.Typeface.BOLD)
                textView.textSize = 15f
                headerRow.addView(textView)
            }
            // Add the header row to the table layout
            tableLayout.addView(headerRow)

        }
        imgview.setOnClickListener {
            val filterQuery = edFilter.text.toString().trim()
            filterData(filterQuery)
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isDataFiltered) {
                    val intent = Intent(this@RateMaintance, RateMaintance::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@RateMaintance, AdminActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    //filter out the serached data from the table
    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterData(query: String) {
        val filteredList = rateData?.filter {fee ->

            val timestamp1 = fee.effective_date
            val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            val dateTime1 = LocalDateTime.parse(timestamp1, formatter1).toLocalDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val timestamp2 = fee.audit_date
            val formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            val dateTime2 = LocalDateTime.parse(timestamp2, formatter2).toLocalDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val regex = Regex(query, RegexOption.IGNORE_CASE)
                    fee.srl?.let { regex.containsMatchIn(it) } ?: false ||
                    fee.billing_currency?.let { regex.containsMatchIn(it) } ?: false ||
                    fee.settlement_currency?.let { regex.containsMatchIn(it) } ?: false ||
                    fee.rate.toString()?.let { regex.containsMatchIn(it) } ?: false ||
                            dateTime1?.let { regex.containsMatchIn(it) } ?: false ||
                            dateTime2?.let { regex.containsMatchIn(it) } ?: false
        }
        if (filteredList != null) {
            if (filteredList.isNotEmpty()) {
                isDataFiltered = true
                updateTable(filteredList)
            } else {
                // Show toast indicating no matching data found
                Toast.makeText(this, "No matching data found", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //create the tabled accroding to the searched data
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTable(filteredList: List<Rate>) {
        tableLayout.removeAllViews()
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange)) // Replace with the actual color resource

        // Create TextViews for each header column
        val headers = arrayOf("SRN","Bill Currency", "Settle Currency", "Rate", "Effective Date", "Current Date")
        for (header in headers) {
            val textView = createTextView(this,header)
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            textView.textSize = 15f
            headerRow.addView(textView)
        }
        // Add the header row to the table layout
        tableLayout.addView(headerRow)// Clear existing rows

        // Iterate through filtered list and add rows to the table
        filteredList.forEach { rateData ->
            val row = createTableRow(rateData)
            tableLayout.addView(row)
            row.setOnClickListener(){
                val timestamp1 = rateData.effective_date
                val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                val dateTime1 = LocalDateTime.parse(timestamp1, formatter1).toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                val effectiveDateTextView=createTextView(this,dateTime1.toString())
                //audit date format
                val timestamp = rateData.audit_date
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                val dateTime = LocalDateTime.parse(timestamp, formatter)
                val dateOnly = dateTime.toLocalDate()
                val formattedDate = dateOnly.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                val auditDateTextView=createTextView(this,formattedDate.toString())
                val intent = Intent(applicationContext, Ratedetails::class.java)
                intent.putExtra("SRL",rateData.srl)
                intent.putExtra("BillCurrency", rateData.billing_currency)
                intent.putExtra("SettleCurrency", rateData.settlement_currency)
                intent.putExtra("Rate", rateData.rate.toString())
                intent.putExtra("EffectiveDate", dateTime1.toString())
                intent.putExtra("AuditDate", formattedDate.toString())
                startActivity(intent)
                finish()
            }
        }
    }
    //create the table row
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTableRow(rateData: Rate): TableRow {
        // Create a row and add TextViews for each field in the data model
        val row = TableRow(this)
        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = params

        val srnTextView = createTextView(this,rateData.srl!!)
        val billCurrencyTextView = createTextView(this,rateData.billing_currency!!)
        val settleCurrencyTextView = createTextView(this,rateData.settlement_currency!!)
        val amount = rateData.rate!!
        val formattedAmount = formatNumberWithGroups(amount)
        val rateTextView = createdoubleTextView(formattedAmount)

        val timestamp1 = rateData.effective_date
        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dateTime1 = LocalDateTime.parse(timestamp1, formatter1).toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val effectiveDateTextView=createTextView(this,dateTime1.toString())
        //audit date format
        val timestamp = rateData.audit_date
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dateTime = LocalDateTime.parse(timestamp, formatter)
        val dateOnly = dateTime.toLocalDate()
        val formattedDate = dateOnly.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val auditDateTextView=createTextView(this,formattedDate.toString())

        row.addView(srnTextView)
        row.addView(billCurrencyTextView)
        row.addView(settleCurrencyTextView)
        row.addView(rateTextView)
        row.addView(effectiveDateTextView)
        row.addView(auditDateTextView)

        row.setOnClickListener {
            // Handle row click
        }

        return row
    }
    //convert the number with decimal points
    fun formatNumberWithGroups(number: Double): String {
        val numberString = number.toLong().toString()  // Convert the number to string

        val decimalPart = if (number.rem(1) != 0.0) {
            val formattedDecimal = "%.2f".format(number - number.toLong())
            if (formattedDecimal.startsWith("0")) formattedDecimal.substring(1) else formattedDecimal
        } else {
            ".00"
        }

        val formattedIntegerPart = numberString.reversed().chunked(3).joinToString(",").reversed()

        return "$formattedIntegerPart$decimalPart"
    }

    private fun createdoubleTextView(text: String): TextView {
        val textView = TextView(this)
        if(text==null || text=="null"){
            textView.text = "0.00"
        }
        else{
            textView.text = text
        }
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        textView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        textView.setPadding(8, 8, 220, 8)
        textView.gravity = android.view.Gravity.RIGHT
        return textView
    }
    //fetch the data for the rate maintenance list
    private fun fetchData() {
        NetworkUtils.showProgress(this)
        val call: Call<List<Rate>> = ApiClient.apiService.getRateMaintaincelist()

        // Enqueue the call
        call.enqueue(object : Callback<List<Rate>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<Rate>>,
                response: Response<List<Rate>>
            ) {

                if (response.isSuccessful) {
                    // Handle the successful response
                     rateData = response.body()!!
                    rateData = rateData.sortedBy { it.srl }
                    rateData?.forEach{ rateData->
                        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
                        val row = TableRow(this@RateMaintance)
                        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                        row.layoutParams = params
                        val srnTextView = createTextView(this@RateMaintance,rateData.srl!!)
                        val billCurrencyTextView = createTextView(this@RateMaintance,rateData.billing_currency!!)
                        val settleCurrencyTextView = createTextView(this@RateMaintance,rateData.settlement_currency!!)
                        val amount = rateData.rate!!
                        val formattedAmount = formatNumberWithGroups(amount)
                        val rateTextView = createdoubleTextView(formattedAmount)
                        //effective date format

                        val timestamp1 = rateData.effective_date
                        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        val dateTime1 = LocalDateTime.parse(timestamp1, formatter1).toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        val effectiveDateTextView=createTextView(this@RateMaintance,dateTime1.toString())
                        //audit date format
                        val timestamp = rateData.audit_date
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        val dateTime = LocalDateTime.parse(timestamp, formatter)
                        val dateOnly = dateTime.toLocalDate()
                        val formattedDate = dateOnly.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        val auditDateTextView=createTextView(this@RateMaintance,formattedDate.toString())

                        row.addView(srnTextView)
                        row.addView(billCurrencyTextView)
                        row.addView(settleCurrencyTextView)
                        row.addView(rateTextView)
                        row.addView(effectiveDateTextView)
                        row.addView(auditDateTextView)
                        // row.addView(select)
                        // Add more TextViews to the row for additional columns
                        tableLayout.addView(row)

                        NetworkUtils.hideProgress(this@RateMaintance)
                        row.setOnClickListener(){
                            val intent = Intent(applicationContext, Ratedetails::class.java)
                            intent.putExtra("SRL",rateData.srl)
                            intent.putExtra("BillCurrency", rateData.billing_currency)
                            intent.putExtra("SettleCurrency", rateData.settlement_currency)
                            intent.putExtra("Rate", rateData.rate.toString())
                            intent.putExtra("EffectiveDate", dateTime1.toString())
                            intent.putExtra("AuditDate", formattedDate.toString())
                            startActivity(intent)
                        }
                    }
                    }
                else{
                    Toast.makeText(this@RateMaintance, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                 //   println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Rate>>, t: Throwable) {
            //    t.printStackTrace()
                NetworkUtils.hideProgress(this@RateMaintance)
                Toast.makeText(this@RateMaintance,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()

                // Handle network errors or failures
               // You may want to log the error or display a message to the user
            }
        })
    }
}
