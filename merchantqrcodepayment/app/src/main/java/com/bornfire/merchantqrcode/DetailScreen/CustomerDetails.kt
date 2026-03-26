package com.bornfire.merchantqrcode.DetailScreen

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
class CustomerDetails : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_details)
        startAppMonitorService()
        setupActionBar()
        val editTexts = initializeEditTexts()
        populateEditTexts(editTexts)
        disableEditTexts(*editTexts.toTypedArray())
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    private fun startAppMonitorService() {
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
    }
    private fun setupActionBar() {
        supportActionBar?.apply {
            val decorView = window.decorView as ViewGroup
            val actionBarRoot = decorView.findViewById<ViewGroup>(android.R.id.content)
            val customView = layoutInflater.inflate(R.layout.custom_action_bar, actionBarRoot, false)
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(customView)
            customView.findViewById<TextView>(R.id.action_bar_title).text = getString(R.string.ct_det)
            customView.findViewById<ImageView>(R.id.help_image).setOnClickListener {
                HelpInfo.getInfo(this@CustomerDetails, "21")
            }
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    private fun initializeEditTexts(): List<EditText> {
        val editTexts = listOf(
            R.id.date, R.id.msgref, R.id.billnum, R.id.userid, R.id.custname,
            R.id.bank1, R.id.remacc, R.id.currency, R.id.amtText, R.id.benacc,
            R.id.particular, R.id.remarks, R.id.status
        ).map { findViewById<EditText>(it) }
        editTexts.forEachIndexed { index, editText ->
            if (editText == null) {
              //  print("CustomerDetails EditText at index $index is null")
            }
        }
        return editTexts
    }
    private fun populateEditTexts(editTexts: List<EditText>) {
        val intentData = listOf(
            "Date", "MessageRef", "BillNumber", "userid", "CustomerName",
            "Bank", "RemAcc", "Currency", "Amount", "Ben", "Particular", "Remarks", "Status"
        )
        intent?.let {
            editTexts.forEachIndexed { index, editText ->
                val data = it.getStringExtra(intentData[index]) ?: ""
                editText.setSafeText(
                    if (index == 6 || index == 9) maskMiddleDigits(data) else data
                )
            }
        }
    }
    private fun disableEditTexts(vararg editTexts: EditText) {
        editTexts.forEach { it.isEnabled = false }
    }
    private fun EditText.setSafeText(text: String?, defaultText: String = "") {
        this.setText(if (text.isNullOrBlank() || text == "null") defaultText else text)
    }
    private fun maskMiddleDigits(input: String): String {
        return if (input.length <= 7) {
            input // If the string is 8 characters or shorter, return it as is.
        } else {
            input.substring(0, 4) + "XXXXX" + input.substring(input.length - 3)
        }
    }
}