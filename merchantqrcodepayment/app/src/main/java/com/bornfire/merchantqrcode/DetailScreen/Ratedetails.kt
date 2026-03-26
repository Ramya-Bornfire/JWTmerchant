package com.bornfire.merchantqrcode.DetailScreen

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AdminScreens.RateMaintance
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.EncryptedRequest
import com.bornfire.merchantqrcode.DataModel.Rate
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.NullCheck
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Ratedetails : BaseActivity() {
    private val calendar = Calendar.getInstance()
    private var mediaPlayer: MediaPlayer? = null
    lateinit var editSrl: EditText
    lateinit var editBillCurrency: EditText
    lateinit var editSettleCurrency: EditText
    lateinit var editRate: EditText
    lateinit var editEffectiveDate: EditText
    lateinit var editAuditDate: EditText

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ratedetails)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        editSrl= findViewById(R.id.editSRL)
        editBillCurrency = findViewById(R.id.editBillCurrency)
        editSettleCurrency = findViewById(R.id.editSettleCurrency)
        editRate = findViewById(R.id.editRate)
        editEffectiveDate = findViewById(R.id.editEffectiveRate)
        editAuditDate = findViewById(R.id.editAuditDate)
        val btn: Button = findViewById(R.id.submitbtn)
        mediaPlayer = MediaPlayer.create(this, R.raw.iphone)
        supportActionBar?.title = "Rate Details"
        // Retrieve data from the intent
        val srlNum = intent.getStringExtra("SRL")
        val billCurrency = intent.getStringExtra("BillCurrency")
        val settleCurrency = intent.getStringExtra("SettleCurrency")
        val rate = intent.getStringExtra("Rate")
        val effectiveDate = intent.getStringExtra("EffectiveDate")
        val auditDate = intent.getStringExtra("AuditDate")

        editEffectiveDate.setOnClickListener(){
            showDatePickerDialog1()
        }
        editAuditDate.setOnClickListener(){
            showDatePickerDialog()
        }
        // Set data to EditText fields
        val buttonName = intent.getStringExtra("Button")
        // Set data to EditText fields
        if (buttonName != null) {
            editSrl.isEnabled = true
            btn.setText(buttonName)
            btn.setOnClickListener() {
                if (validateFields()) {
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        postData()
                    } else {
                        NetworkUtils.NoInternetAlert(this)
                    }
                } else {
                    Toast.makeText(this, "Validation Failed", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            editSrl.isEnabled = false
            editSrl.setText(NullCheck.getValidText(srlNum))
            editBillCurrency.setText(NullCheck.getValidText(billCurrency))
            editSettleCurrency.setText(NullCheck.getValidText(settleCurrency))
            editRate.setText(NullCheck.getValidText(rate))
            editEffectiveDate.setText(NullCheck.getValidText(effectiveDate))
            editAuditDate.setText(NullCheck.getValidText(auditDate))
            btn.setOnClickListener(){
                if (validateFields()) {
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        UpdateData(srlNum)
                    } else {
                        NetworkUtils.NoInternetAlert(this)
                    }
                } else {
                    Toast.makeText(this, "Validation Failed", Toast.LENGTH_LONG).show()
                }
            }
            }
    }
    private fun showDatePickerDialog1() {
        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener1,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Optional: Set max date

        datePickerDialog.show()
    }
    private val dateSetListener1 = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        // Update the calendar when the user sets a date
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        editEffectiveDate.setText(formattedDate)
    }
    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Optional: Set max date

        datePickerDialog.show()
    }
    private val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        // Update the calendar when the user sets a date
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        editAuditDate.setText(formattedDate)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun UpdateData(srlNum: String?) {
        val inputDate = editEffectiveDate.text.toString()
        val inputFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date: LocalDate = LocalDate.parse(inputDate, inputFormat) // Parse input date string
        val formattedDate: String = date.format(outputFormat)

        val inputDate1 = editAuditDate.text.toString()
        val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date1: LocalDate = LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
        val formattedDate1: String = date1.format(outputFormat1)

        val rateData = Rate(srlNum,editBillCurrency.text.toString(), editSettleCurrency.text.toString()
            , editRate.text.toString().toDouble(), formattedDate, formattedDate1)
        val url = "api/UpdateExistRate" // Replace "your_user_id" with the actual user ID
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(rateData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.updateRateMaintenance(url,encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                  //  println("User data updated successfully")

                } else {
                    Toast.makeText(this@Ratedetails, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Failed to update data: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               // println("Failed to update user data: ${t.message}")
                Toast.makeText(this@Ratedetails,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()

            }
        })
        callDialog()
    }

    fun validateFields(): Boolean {
        val srl = editSrl.text.toString()
        val billCurrency = editBillCurrency.text.toString()
        val settleCurrency = editSettleCurrency.text.toString()
        val rate = editRate.text.toString()
        val effectiveDate = editEffectiveDate.text.toString()
        val auditDate = editAuditDate.text.toString()
        if (srl.isEmpty()) {
            editSrl.error = "SRL cannot be empty"
            return false
        }
        val alphaRegex = Regex("[a-zA-Z]+")
        if (billCurrency.isEmpty()) {
            editBillCurrency.error = "Bill Currency cannot be empty"
            return false
        }
        else if (!billCurrency.matches(alphaRegex)) {
            editBillCurrency.error = "Bill Currency must contain only alphabetic characters"
            return false
        }
        if (settleCurrency.isEmpty()) {
            editSettleCurrency.error = "Settle Currency cannot be empty"
            return false
        }
        else if (!settleCurrency.matches(alphaRegex)) {
            editSettleCurrency.error = "Bill Currency must contain only alphabetic characters"
            return false
        }
        if (rate.isEmpty()) {
            editRate.error = "Rate cannot be empty"
            return false
        } else {
            val rateValue = rate.toDoubleOrNull()
            if (rateValue == null || rateValue <= 0) {
                editRate.error = "Rate must be a valid positive number"
                return false
            }
        }
        if (effectiveDate.isEmpty()) {
            editEffectiveDate.error = "Effective Date cannot be empty"
            return false
        }
        if (auditDate.isEmpty()) {
            editAuditDate.error = "Audit Date cannot be empty"
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postData() {
        val inputDate = editEffectiveDate.text.toString()
        val inputFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date: LocalDate = LocalDate.parse(inputDate, inputFormat) // Parse input date string
        val formattedDate: String = date.format(outputFormat)

        val inputDate1 = editAuditDate.text.toString()
        val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date1: LocalDate = LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
        val formattedDate1: String = date1.format(outputFormat1)
        val rateData = Rate(editSrl.text.toString(), editBillCurrency.text.toString(),editSettleCurrency.text.toString() , editRate.text.toString().toDouble(),formattedDate ,formattedDate1 )
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(rateData)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.addRateMaintenance(encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    callDialog()
                    val responseBody = response.body()
                //    println("Response: $responseBody")
                } else {
                    Toast.makeText(this@Ratedetails, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                //    println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  println("Network error: ${t.message}")
                Toast.makeText(this@Ratedetails,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()

            }
        })
    }

    private fun playSound() {
            mediaPlayer?.start() }
      private fun callDialog() {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.sucessdialog, null)
          builder.setCancelable(false)
            builder.setView(view)
            playSound()
            builder.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
                val intent = Intent(this, RateMaintance::class.java)
                startActivity(intent)
                finish()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    }