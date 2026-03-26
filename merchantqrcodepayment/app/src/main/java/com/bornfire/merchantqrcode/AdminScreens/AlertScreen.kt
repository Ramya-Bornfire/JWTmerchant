package com.bornfire.merchantqrcode.AdminScreens

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.AlertData
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.TextforTable.createTextView
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlertScreen : BaseActivity() {
    lateinit var backBtn: ImageView
    lateinit var alertList: List<AlertData>
    lateinit var alert_help_image:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_screen)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        backBtn=findViewById(R.id.backBtn)
        alert_help_image = findViewById(R.id.alert_help_image)
        alert_help_image.setOnClickListener(){
            HelpInfo.getInfo(this,"29")
        }
        backBtn.setOnClickListener{
            finish()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange))// Replace with the actual color resource
        // Create TextViews for each header column
        val headerParams = mapOf(
            "ALERT ID" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f),
            "ALERT TYPE" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f),
            "ALERT MESSAGE" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2.5f),
            "SCREEN NAME" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
        )
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange))
        val headers = arrayOf("ALERT ID", "ALERT TYPE", "ALERT MESSAGE", "SCREEN NAME")
        for (header in headers) {
            val textView = createTextView(this, header)
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            textView.textSize = 15f
            textView.layoutParams = headerParams[header]
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)
        supportActionBar?.hide()
            fetchAlert()
    }
    private fun fetchAlert() {
        val call: Call<List<AlertData>> = ApiClient.apiService.getAlert()
        call.enqueue(object : Callback<List<AlertData>> {
            override fun onResponse(
                call: Call<List<AlertData>>,
                response: Response<List<AlertData>>
            ) {
                if (response.isSuccessful) {
                    alertList = response.body()!!
                   // println("Alert List ------>> ${alertList}")
                    alertList = alertList.sortedByDescending { it. alertId}
                    alertList?.forEach{ alert->
                        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
                        val row = TableRow(this@AlertScreen)
                        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                        row.layoutParams = params
                        val alertid = createTextView(this@AlertScreen,alert.alertId.toString())
                        val alertType = createTextView(this@AlertScreen,alert.alertType)
                        val alertMsg = createTextView(this@AlertScreen,alert.alertMessage)
                        val screenName = createTextView(this@AlertScreen,alert.screenName)

                        val alertIdParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
                        val alertTypeParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
                        val alertMsgParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2.5f)
                        val screenNameParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)

                        alertid.layoutParams = alertIdParams
                        alertType.layoutParams = alertTypeParams
                        alertMsg.layoutParams = alertMsgParams
                        screenName.layoutParams = screenNameParams

                        row.addView(alertid)
                        row.addView(alertType)
                        row.addView(alertMsg)
                        row.addView(screenName)
                        tableLayout.addView(row)
                        NetworkUtils.hideProgress(this@AlertScreen)
                        row.setOnClickListener {
                        }
                    }
                    // Update UI or log the data
                }
                else {
                    Toast.makeText(this@AlertScreen, "Error: "+response.message(), Toast.LENGTH_LONG).show()
              //      println("Error: ${response.code()}")
                    NetworkUtils.hideProgress(this@AlertScreen)
                }
            }
            override fun onFailure(call: Call<List<AlertData>>, t: Throwable) {
           //     t.printStackTrace()
                NetworkUtils.hideProgress(this@AlertScreen)
                Toast.makeText(this@AlertScreen,"Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
            }
        })
    }

}