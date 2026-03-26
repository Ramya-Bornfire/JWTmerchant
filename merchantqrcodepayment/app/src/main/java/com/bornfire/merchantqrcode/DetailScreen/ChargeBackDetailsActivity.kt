package com.bornfire.merchantqrcode.DetailScreen

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AdminScreens.Chargebacks
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.SharedUserDataObj
import com.bornfire.merchantqrcode.Utils.EditNonEdit
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.NullCheck
import com.bornfire.merchantqrcode.retrofit.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ChargeBackDetailsActivity : BaseActivity() {
    val merchantdata = SharedMerchantDataObj.merchantData
    val userData = SharedUserDataObj.userData
    val usercategory = SharedusercatDataObj.UserCategory
    lateinit var editTransDate: EditText
    lateinit var editMessageId: EditText
    lateinit var editBillNo: EditText
    lateinit var editBillDate: EditText
    lateinit var editBillAmt: EditText
    lateinit var editCurncy: EditText
    lateinit var editappby: EditText
    lateinit var editappdate: EditText
    lateinit var merchantid: String
    lateinit var userid: String
    lateinit var btn: Button
    lateinit var TransDate: String
    lateinit var MsgId: String
    lateinit var AuditRef: String
    lateinit var Billno: String
    lateinit var Billdate: String
    lateinit var BillAmt: String
    lateinit var Currency: String
    lateinit var Remark: String
    lateinit var appby: String
    lateinit var appdate: String
    lateinit var date: String
    lateinit var editRemark: EditText
    private var mediaPlayer: MediaPlayer? = null
    var screenid:String = "7"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge_back_details)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            // Set the title
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.cb_det)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener() {
                HelpInfo.getInfo(this,screenid)
            }
        }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        editRemark = findViewById(R.id.editRemarks)
        val isTablet =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        editTransDate = findViewById(R.id.editTranDate)
        editMessageId = findViewById(R.id.editMsgId)
        editBillNo = findViewById(R.id.editBillNo)
        editBillDate = findViewById(R.id.editBillDate)
        editBillAmt = findViewById(R.id.editBillAmount)
        editCurncy = findViewById(R.id.Crncy)
        editappby = findViewById(R.id.aprby)
        editappdate = findViewById(R.id.aprdate)

        btn = findViewById(R.id.submitbtn)
        mediaPlayer = MediaPlayer.create(this, R.raw.iphone)
        // Retrieve data from the intent
        TransDate = intent.getStringExtra("TransDate").toString()
        MsgId = intent.getStringExtra("MsgId").toString()
        AuditRef = intent.getStringExtra("AudRef").toString()
        Billno = intent.getStringExtra("BillNo").toString()
        Billdate = intent.getStringExtra("BillDate").toString()
        BillAmt = intent.getStringExtra("BillAmt").toString()
        Currency = intent.getStringExtra("Currency").toString()
        Remark = intent.getStringExtra("Remarks").toString()
        appby = intent.getStringExtra("APPROVEDUSER").toString()
        appdate = intent.getStringExtra("APPROVEDDATE").toString()
        if (usercategory?.user_category == "Representative") {
            if (!appdate.isNullOrEmpty() && appdate != "null") {
                appdate = convertDate(appdate)
            } else {
            }
        }
        editRemark.setText(Remark.toString())
        val revert = intent.getStringExtra("Revert")
        val initiate = intent.getStringExtra("Initiate")
        date = LocalDate.now().toString()
        if (Remark == "INITIATED" || Remark == "REVERTED") {
            btn.visibility = View.GONE
        }

        editTransDate.setText(NullCheck.getValidText(TransDate))
        editMessageId.setText(NullCheck.getValidText(MsgId))
        editBillNo.setText(NullCheck.getValidText(Billno))
        editBillDate.setText(NullCheck.getValidText(Billdate))
        editBillAmt.setText(NullCheck.getValidText(BillAmt))
        editCurncy.setText(NullCheck.getValidText(Currency))
        editappby.setText(NullCheck.getValidText(appby))
        editappdate.setText(NullCheck.getValidText(appdate))

        if (initiate.toString() == "Y" && revert.toString() == "N") {
            EditNonEdit.setEditTextsNonEditable(
                editTransDate, editMessageId, editBillNo,
                editBillDate, editBillAmt, editCurncy, editappby, editappdate
            )
            editRemark.isEnabled = true
            btn.text = "Initiate"
            btn.setOnClickListener() {
                if (NetworkUtils.isNetworkAvailable(this)) {
                    btn.isEnabled = false
                    InitiateChargeBack()
                } else {
                    NetworkUtils.NoInternetAlert(this)
                }
            }
        } else if (initiate.toString() == "N" && revert.toString() == "Y") {
            EditNonEdit.setEditTextsNonEditable(
                editTransDate,
                editMessageId,
                editBillNo,
                editBillDate,
                editBillAmt,
                editCurncy,
                editappby,
                editappdate
            )
            editRemark.isEnabled = true
            btn.text = "Approve"
            screenid = "27"
            btn.setOnClickListener() {
                if (NetworkUtils.isNetworkAvailable(this)) {
                    btn.isEnabled=false
                    updateChargeBack()
                } else {
                    NetworkUtils.NoInternetAlert(this)
                }
            }

        } else {
            editRemark.visibility = View.VISIBLE
            editRemark.setText(Remark)
            EditNonEdit.setEditTextsNonEditable(
                editTransDate,
                editMessageId,
                editBillNo,
                editBillDate,
                editBillAmt,
                editCurncy,
                editRemark,
                editappby,
                editappdate
            )
            screenid = "28"

            btn.visibility = View.GONE
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onSupportNavigateUp()
            }
        })
    }

    private fun InitiateChargeBack() {
        if (usercategory?.user_category == "Representative") {
            userid = merchantdata?.merchant_rep_id.toString()
            merchantid = merchantdata?.merchant_user_id.toString()
        } else {
            userid = userData?.user_id.toString()
            merchantid = userData?.merchant_user_id.toString()
        }

        val call = ApiClient.apiService.initiateChargeBack(userid, MsgId, merchantid)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    var repo = response.body()
                   // println(repo)
                    callDialog()
                } else {
                    Toast.makeText(this@ChargeBackDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                 //   println("Error: ${response.code()}")
                    showResponseDialog(response.body().toString())
                  //  println("Failed to update user data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  println("Failed to update user data: ${t.message}")
                Toast.makeText(this@ChargeBackDetailsActivity, R.string.Failed, Toast.LENGTH_LONG)
                    .show()

            }
        })

    }

    private fun updateChargeBack() {
        val user = merchantdata?.merchant_rep_id.toString()
        val call = ApiClient.apiService.approveChargeBack(user, MsgId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val repo = response.body()
                  //  println(repo)
                    callDialog()
                } else {
                    Toast.makeText(this@ChargeBackDetailsActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                //    println("Error: ${response.code()}")
                    showResponseDialog(response.body().toString())
                 //   println("Failed to update user data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
             //   println("Failed to update user data: ${t.message}")
                Toast.makeText(this@ChargeBackDetailsActivity, R.string.Failed, Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun callDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.sucessdialog, null)
        builder.setCancelable(false)
        builder.setView(view)
        playSound()
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
            val intent = Intent(this, Chargebacks::class.java)
            startActivity(intent)
            finish()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun playSound() {
        mediaPlayer?.start()
    }

    private fun showResponseDialog(response: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage(response)
        builder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    fun convertDate(inputDate: String): String {
        // Fix the timezone format by inserting a colon before the last two digits
        val correctedInput = inputDate.substring(0, 26) + ":" + inputDate.substring(26)
        // Parse the corrected input string to a ZonedDateTime
        val zonedDateTime = ZonedDateTime.parse(correctedInput)
        // Format the date to 'dd-MM-yyyy'
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return zonedDateTime.format(formatter)
    }
}
