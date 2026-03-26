package com.bornfire.merchantqrcode.DetailScreen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import androidx.core.content.FileProvider
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.CustomDialogFragment
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.SharedUserDataObj
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.retrofit.Encryption
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TransactionDetails : BaseActivity() {
    lateinit var scroll:ScrollView
    val userData = SharedUserDataObj.userData
    private val merchantSharedData = SharedMerchantDataObj.merchantData
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val userSharedCategory= SharedusercatDataObj.UserCategory
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        setupActionBar()
        setContentView(R.layout.activity_transaction_details)
        val editTexts = initializeEditTexts()
        populateEditTexts(editTexts)
        val texts = initializeTexts()
        disableEditTexts(*editTexts.toTypedArray())
        scroll = findViewById(R.id.tran_view)
        val printBtn = findViewById<Button>(R.id.pdf_btn)
        val saveBtn =findViewById<Button>(R.id.save_btn)
        val status = findViewById<EditText>(R.id.transEditStatus)
        val amt = findViewById<EditText>(R.id.transEditAmount)
        val billNo = findViewById<EditText>(R.id.tranSEditBillNumber)
        val ref = findViewById<EditText>(R.id.transEditDeviceId)
        val tranDate = findViewById<EditText>(R.id.transEditTransactionDate)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: throw IllegalStateException("Bluetooth not supported")
        printBtn.setOnClickListener(){
            changeEditTextColors(editTexts)
            changeTextColors(texts)
            printBtn.visibility = View.INVISIBLE
            saveBtn.visibility = View.INVISIBLE
           if(userSharedCategory?.user_category=="Representative") {
                showImageAlertDialog(
                    merchantSharedData?.merchant_user_id.toString(),
                    "DeviceId",
                    status.text.toString(),
                    amt.text.toString(),
                    "dwoijoi32",
                    ref.text.toString(),tranDate.text.toString(),
                    merchantSharedData?.merchant_name.toString(),
                    "address",
                    "GABORONE",
                    "Terminal12",
                    billNo.text.toString(),
                    merchantSharedData?.email_address.toString(),
                    merchantSharedData?.mobile_no.toString()
                )
            }
            else{
                showImageAlertDialog(
                    userData?.merchant_user_id.toString(),
                    "DeviceId",
                    status.text.toString(),
                    amt.text.toString(),
                    "dwoijoi32",
                    ref.text.toString(),tranDate.text.toString(),
                    userData?.merchant_name.toString(),
                    "address",
                    "Salem",
                    "Terminal12",
                    billNo.text.toString(),
                    userData?.email_address1.toString(),
                    userData?.mobile_no1.toString()
                )
            }
        }
        saveBtn.setOnClickListener(){
            changeEditTextColors(editTexts)
            changeTextColors(texts)
            printBtn.visibility = View.INVISIBLE
            saveBtn.visibility = View.INVISIBLE
            generatePDF(scroll)
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
    private fun setupActionBar() {
        supportActionBar?.apply {
            val customView = layoutInflater.inflate(R.layout.custom_action_bar, null)
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(customView)
            customView.findViewById<TextView>(R.id.action_bar_title).text = getString(R.string.tran_det)
            customView.findViewById<ImageView>(R.id.help_image).setOnClickListener {
                HelpInfo.getInfo(this@TransactionDetails, "23")
            }
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    private fun initializeEditTexts(): List<EditText> {
        val editTextIds = listOf(
            R.id.tranSEditBillNumber, R.id.transEditName, R.id.transEditUserId, R.id.transEditBillDate, R.id.transEditCurrency,
            R.id.transEditBillAmount, R.id.transEditTransactionDate, R.id.transEditAmount, R.id.transEditStatus, R.id.aprby, R.id.aprdate,R.id.revremark,
            R.id.transEditDeviceId)
        return editTextIds.map { findViewById(it) }
    }
    private fun initializeTexts(): List<TextView> {
        val editTextIds = listOf(
            R.id.tran_date_txt, R.id.bill_num_txt, R.id.cus_name_txt, R.id.usr_id_txt, R.id.curncy_txt,
            R.id.tran_amt_txt, R.id.bill_date_txt, R.id.bill_amt_txt, R.id.sts_txt, R.id.apr_usr_txt, R.id.apr_date_txt,R.id.rmk_txt)
        return editTextIds.map { findViewById(it) }
    }
    private fun populateEditTexts(editTexts: List<EditText>) {
        val intentKeys = listOf(
            "billNumber", "name", "userId", "billDate", "currency","billAmount",
            "transactionDate", "transactionAmount", "status", "approvedUser", "approvedDate", "reversalRemark","RefId")
        intent?.let {
            editTexts.forEachIndexed { index, editText ->
                editText.setSafeText(it.getStringExtra(intentKeys[index]) ?: "")
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    private fun EditText.setSafeText(text: String?, defaultText: String = "") {
        this.setText(if (text.isNullOrBlank() || text == "null") defaultText else text)
    }
    private fun disableEditTexts(vararg editTexts: EditText) {
        editTexts.forEach { it.isEnabled = false }
    }
    // Function to generate PDF from ScrollView content
    private fun generatePDF(scrollView: ScrollView) {
        // Create a PDF document
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(scrollView.width, scrollView.height, 1).create()
        val page = document.startPage(pageInfo)

        // Draw the ScrollView content onto the PDF page
        val canvas: Canvas = page.canvas
        val titleText = "Transaction Details"  // Your title
        val paint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) // Bold text
            textSize = 48f  // Title text size
            color = Color.BLACK  // Title color
            textAlign = Paint.Align.CENTER  // Align title at the center
        }
        canvas.drawText(titleText, (canvas.width / 2).toFloat(), 80f, paint)  // Draw the title at the top, adjust y as needed
        // Draw the ScrollView content below the title
        canvas.translate(0f, 100f)
        scrollView.draw(canvas)
        document.finishPage(page)
        // Create the file in cache directory
        val cacheDir = cacheDir
        val pdfFile = File(cacheDir, "tran_detail ${Encryption.generatePID()}.pdf")
        try {
            val fileOutputStream = FileOutputStream(pdfFile)
            document.writeTo(fileOutputStream)
            document.close()
            Toast.makeText(this, "PDF generated", Toast.LENGTH_SHORT).show()
            openPDF(pdfFile)
            finish()
        } catch (e: IOException) {
          //  e.printStackTrace()
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show()
        }
    }
    // Function to open the generated PDF from cache
    private fun openPDF(pdfFile: File) {
        val pdfUri = FileProvider.getUriForFile(this, "${packageName}.provider", pdfFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Ensure the permission is granted
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }
    private fun changeEditTextColors(editTexts: List<EditText>) {
        for (editText in editTexts) {
            editText.setTextColor(Color.BLACK)  // Replace with desired color
        }
    }
    private fun changeTextColors(texts: List<TextView>) {
        for (Text in texts) {
            Text.setTextColor(Color.BLACK)  // Replace with desired color
        }
    }
    private fun showImageAlertDialog(
        merchantId: String,
        DeviceId: String,
        tranStatus: String,
        amount: String,
        referenceLabel: String,
        tranId: String,
        tranDate: String,
        merchantName: String,
        merchantAddress: String,
        merchantCity: String,
        merchantTerminal: String,
        billnum:String,
        email:String,
        contact:String
    ) {
        if (!isFinishing && !isDestroyed) {
            val dialogFragment = CustomDialogFragment.newInstance(
                merchantId,
                DeviceId,
                tranStatus,
                amount,
                referenceLabel,
                tranId,
                tranDate,
                merchantName,
                merchantAddress,
                merchantCity,
                merchantTerminal,
                billnum,
                email,
                contact
            )
            dialogFragment.show(supportFragmentManager, "CustomDialogFragment")
            dialogFragment.dialog?.setCancelable(false)
        }
    }
}