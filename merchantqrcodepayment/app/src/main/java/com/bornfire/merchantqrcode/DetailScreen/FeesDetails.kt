package com.bornfire.merchantqrcode.DetailScreen

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AdminScreens.FeesAndCharge
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
class FeesDetails : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fees_details)
        startAppMonitorService()
        setupActionBar()
        val editTexts = initializeEditTexts()
        populateEditTexts(editTexts)
        disableEditTexts(*editTexts.toTypedArray())
    }
    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, FeesAndCharge::class.java)
        startActivity(intent)
        finish()
        return true
    }
    private fun startAppMonitorService() {
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
    }
    private fun setupActionBar() {
        supportActionBar?.apply {
            val customView = layoutInflater.inflate(R.layout.custom_action_bar, null)
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(customView)
            customView.findViewById<TextView>(R.id.action_bar_title).text = getString(R.string.fee_det)
            customView.findViewById<ImageView>(R.id.help_image).setOnClickListener {
                HelpInfo.getInfo(this@FeesDetails, "23")
            }
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    private fun initializeEditTexts(): List<EditText> {
        val editTextIds = listOf(
            R.id.date, R.id.msgref, R.id.billnum, R.id.userid, R.id.custname,
            R.id.bank1, R.id.remacc, R.id.currency, R.id.amtText, R.id.transFee, R.id.appFlag
        )
        return editTextIds.map { findViewById(it) }
    }
    private fun populateEditTexts(editTexts: List<EditText>) {
        val intentKeys = listOf(
            "Date", "MessageRef", "BillNumber", "Userid", "CustomerName",
            "Bank", "Remacc", "Currency", "Amount", "Fees", "AppliedFlag"
        )
        intent?.let {
            editTexts.forEachIndexed { index, editText ->
                val data = it.getStringExtra(intentKeys[index]) ?: if (index == 10) "N" else ""
                editText.setSafeText(if (index == 6) maskMiddleDigits(data) else data)
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