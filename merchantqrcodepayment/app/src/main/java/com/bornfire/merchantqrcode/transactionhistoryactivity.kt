package com.bornfire.merchantqrcode

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.DataModel.TransData
import com.bornfire.merchantqrcode.DetailScreen.TransactionDetails
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.Utils.EditNonEdit
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.NotifyDownload
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.showDatePickerDialog
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.tranFrom
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.tranTo
import com.bornfire.merchantqrcode.Utils.TextforTable.createAmountTextView
import com.bornfire.merchantqrcode.Utils.TextforTable.createTextView
import com.bornfire.merchantqrcode.Utils.TextforTable.formatNumberWithGroups
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.bornfire.merchantqrcode.BuildConfig
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

class TransactionHistoryActivity : BaseActivity() {
    lateinit var merchantId: String
    lateinit var userid: String
    lateinit var transList: List<TransData>

    lateinit var tableLayout: TableLayout
    val userData = SharedUserDataObj.userData
    private val merchantSharedData = SharedMerchantDataObj.merchantData
    private val userSharedCategory = SharedusercatDataObj.UserCategory
    lateinit var backBtn: ImageView
    private lateinit var noData: TextView
    lateinit var editFilter: EditText
    lateinit var currentDate: EditText
    private lateinit var pdfDownload: ImageView
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())


    private lateinit var tranHelpImage: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactionhistoryactivity)
        editFilter = findViewById(R.id.editFilter)
        noData = findViewById(R.id.emptyTextView)
        pdfDownload = findViewById(R.id.pdf_dwn)
        currentDate = findViewById(R.id.cur_date)
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayDate: Date = calendar.time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateString: String = dateFormat.format(todayDate)
        currentDate.setText(dateString)
        tranFrom = dateString
        tranTo = dateString
        supportActionBar?.hide()
        tableLayout = findViewById(R.id.transtableLayout)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
        pdfDownload.setOnClickListener {
            callPdfDialog()
        }
        tranHelpImage = findViewById(R.id.tran_help_image)
        tranHelpImage.setOnClickListener {
            HelpInfo.getInfo(this, "5")
        }
        currentDate.setOnClickListener {
            editFilter.text.clear()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDay = String.format("%02d", selectedDay)
                    val formattedMonth = String.format(
                        "%02d",
                        selectedMonth + 1
                    ) // Adding 1 to the month as it's 0-based
                    val selectedDate = "$formattedDay-$formattedMonth-$selectedYear"
                    currentDate.setText(selectedDate)
                    tranFrom = selectedDate
                    tranTo = selectedDate
                    fetchMerchantTransData()
                }, year, month, day)
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        editFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (editFilter.text.toString().isNotEmpty()) {
                    val searchText = editFilter.text.toString().trim()
                    val isTablet =
                        resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
                    if (isTablet) {
                        filterDataTab(searchText)
                    } else {
                        filterDataMob(searchText)
                    }
                } else {
                    fetchMerchantTransData()
                }
            }
        })
        if (userSharedCategory?.user_category == "Representative") {
            merchantId = merchantSharedData?.merchant_user_id.toString()
            userid = merchantSharedData?.merchant_rep_id.toString()
        } else {
            merchantId = userData?.merchant_user_id.toString()
            userid = userData?.user_id.toString()
        }
        fetchMerchantTransData()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
               finish()
            }
        })
    }

    private fun filterDataMob(query: String?) {
        val normalizedQuery = query?.trim()?.lowercase() ?: return // Safely handle null query
        val filteredList = transList.filter { fee ->
            val transactionDateMatch = fee.tran_date.contains(normalizedQuery, ignoreCase = true)
            val billNumberMatch = fee.merchant_bill_number.contains(normalizedQuery, ignoreCase = true)
            val statusMatch = fee.tran_status.contains(normalizedQuery, ignoreCase = true)
            val amountMatch = fee.tran_amount.let { amount ->
                val formattedAmount = String.format("%.2f", amount.toDouble())
                formattedAmount.contains(normalizedQuery)
            }
            transactionDateMatch || billNumberMatch || amountMatch || statusMatch
        }
        if (filteredList.isNotEmpty()) {
            updateTableMob(filteredList)
        } else {
            AlertDialogBox().showDialog(this, "No matching data found")
        }
    }
    override fun onResume() {
        super.onResume()
        editFilter.text.clear()
    }
    private fun handleRowClick(transList: TransData) {
        val intent = Intent(applicationContext, TransactionDetails::class.java).apply {
            putExtra("billNumber", transList.merchant_bill_number)
            putExtra("name", transList.ipsx_account_name)
            putExtra("userId", transList.user_id)
            putExtra("billDate", transList.tran_date)
            putExtra("billAmount", formatNumberWithGroups(transList.tran_amount.toDouble()))
            putExtra("currency", transList.tran_currency)
            putExtra("transactionDate", transList.tran_date)
            putExtra("transactionAmount", formatNumberWithGroups(transList.tran_amount.toDouble()))
            putExtra("status", transList.tran_status)
            putExtra("approvedUser", transList.auth_user)
            putExtra("approvedDate", transList.auth_time)
            putExtra("reversalRemark", transList.reversal_remarks)
            putExtra("RefId",transList.sequence_unique_id)
        }
        startActivity(intent)
    }

    private fun updateTableMob(filteredList: List<TransData>) {
        tableLayout.removeAllViews()
        createHeader()

        filteredList.forEach { transList ->
            val row = createTableRowMob(transList)
            tableLayout.addView(row)

            row.setOnClickListener {
                EditNonEdit.hideKeyboard(editFilter)
                handleRowClick(transList)
            }
        }
    }

    private fun createTableRowMob(transList: TransData?): TableRow {
        val row = TableRow(this)
        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        row.layoutParams = params
        val timestamp1 = transList?.tran_date
        val billDate = createTextView(this,timestamp1.toString())
        val formattedAmount1 = formatNumberWithGroups(transList?.tran_amount!!.toDouble())
        val billAmount = createAmountTextView(this,formattedAmount1)
        val merchantBillNumber = createTextView(this,transList.merchant_bill_number)
        val tranStatus = createTextView(this,transList.tran_status)
        row.addView(billDate)
        row.addView(merchantBillNumber)
        row.addView(billAmount)
        row.addView(tranStatus)
        return row
    }

    private fun filterDataTab(query: String?) {
        val normalizedQuery = query?.trim()?.lowercase() ?: return
        val filteredList = transList.filter { fee ->
            val transactionDateMatch = fee.tran_date.contains(normalizedQuery, ignoreCase = true)
            val accName = fee.ipsx_account_name.contains(normalizedQuery, ignoreCase = true)
            val currency = fee.tran_currency.contains(normalizedQuery, ignoreCase = true)
            val userId = fee.user_id.contains(normalizedQuery, ignoreCase = true)
            val billNumberMatch = fee.merchant_bill_number.contains(normalizedQuery, ignoreCase = true)
            val statusMatch = fee.tran_status.contains(normalizedQuery, ignoreCase = true)
            val amountMatch = fee.tran_amount.let { amount ->
                val formattedAmount = String.format("%.2f", amount.toDouble())
                formattedAmount.contains(normalizedQuery)
            }
            transactionDateMatch || billNumberMatch || amountMatch || statusMatch || accName || currency || userId
        }
        if (filteredList.isNotEmpty()) {
            updateTableTab(filteredList)
        } else {
            AlertDialogBox().showDialog(this, "No matching data found")
        }
    }

    private fun updateTableTab(filteredList: List<TransData>) {
        val tableLayout: TableLayout = findViewById(R.id.transtableLayout)
        tableLayout.removeAllViews()
        createHeader()
        filteredList.forEach { transList ->
            val row = createTableRowTab(transList)
            tableLayout.addView(row)
            row.setOnClickListener {
                handleRowClick(transList)
                EditNonEdit.hideKeyboard(editFilter)
            }

        }
    }

    private fun createTableRowTab(transList: TransData?): TableRow {
        val row = TableRow(this)
        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        row.layoutParams = params
        val tranStatus = createTextView(this,transList?.tran_status)
        val formattedAmount = formatNumberWithGroups(transList?.tran_amount!!.toDouble())
        val tranAmount = createAmountTextView(this,formattedAmount)
        val remitterAccount = createTextView(this,transList.ipsx_account_name)
        val userId = createTextView(this,transList.user_id)
        val billDate = createTextView(this,transList.tran_date)
        val remark = createTextView(this,transList.reversal_remarks)
        val currency = createTextView(this,transList.tran_currency)
        val tranDate = createTextView(this,transList.tran_date)
        val merchantBillNumber = createTextView(this,transList.merchant_bill_number)
        row.addView(billDate)
        row.addView(tranDate)
        row.addView(merchantBillNumber)
        row.addView(remitterAccount)
        row.addView(userId)
        row.addView(currency)
        row.addView(tranAmount)
        row.addView(tranStatus)
        row.addView(remark)
        return row
    }
    fun isTablet(context: Context): Boolean {
        return (context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    private fun createHeader() {
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange))
        val headers: Array<String> = if (isTablet(this)) {
            arrayOf("BILL DATE", "TRAN DATE", "BILL NUMBER", "CUSTOMER NAME", "USER ID", "CURRENCY", "AMOUNT", "STATUS", "REMARK")
        } else {
            arrayOf("DATE", "BILL NUMBER", "AMOUNT", "STATUS")
        }
        val weights = if (isTablet(this)) {
            FloatArray(headers.size) { 1f }// Adjust weights for each column
        } else {
            floatArrayOf(1f, 1.5f, 1.2f, 1.2f) // Different weights for smaller screens
        }
        for (i in headers.indices) {
            val textView = createTextView(this, headers[i])
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            textView.textSize = 15f
         val params = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weights[i])
            textView.layoutParams = params

            headerRow.addView(textView)
        }

        tableLayout.addView(headerRow)
    }
        private fun fetchMerchantTransData() {
        tableLayout.removeAllViews()
            createHeader()
        val call: Call<List<TransData>> = ApiClient.apiService.getTransactionDetails(
            merchantId,
            userid,
            tranFrom,
            tranTo,
            "MOBILEVIEW"
        )
        call.enqueue(object : Callback<List<TransData>> {
            override fun onResponse(
                call: Call<List<TransData>>,
                response: Response<List<TransData>>
            ) {
                if (response.isSuccessful) {
                    transList = response.body()!!
                 //   println(transList)
                    if (transList.isNotEmpty()) {
                        if (transList.size == 100) {
                            AlertDialog.Builder(this@TransactionHistoryActivity)
                                .setTitle("Alert")
                                .setMessage("You will be able to view only your latest 100 transactions. For complete list of transactions in the mentioned time period please select the use option.")
                                .setNegativeButton("Ok", null)
                                .show()
                        }
                        editFilter.visibility = View.VISIBLE
                        noData.visibility = View.GONE
                    } else {
                        editFilter.visibility = View.GONE
                        noData.visibility = View.VISIBLE
                    }
                    transList.forEach { transList ->
                        val tableLayout: TableLayout = findViewById(R.id.transtableLayout)
                        val row = TableRow(this@TransactionHistoryActivity)
                        val params = TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                        )
                        row.layoutParams = params
                        val tranStatus = createTextView(this@TransactionHistoryActivity,transList.tran_status)
                        val formattedAmount =
                            formatNumberWithGroups(transList.tran_amount.toDouble())
                        val tranAmount = createAmountTextView(this@TransactionHistoryActivity,formattedAmount)
                        val remitterAccount = createTextView(this@TransactionHistoryActivity,transList.ipsx_account_name)
                        val userId = createTextView(this@TransactionHistoryActivity,transList.user_id)
                        val timestamp1 = transList.tran_date
                        val billDate = createTextView(this@TransactionHistoryActivity,timestamp1)
                        val timestamp = transList.tran_date
                        val tranDate = createTextView(this@TransactionHistoryActivity,timestamp)
                        val currency = createTextView(this@TransactionHistoryActivity,transList.tran_currency)
                        val remark = createTextView(this@TransactionHistoryActivity,transList.reversal_remarks)

                        val merchantBillNumber =
                            createTextView(this@TransactionHistoryActivity,transList.merchant_bill_number)
                        if (isTablet(this@TransactionHistoryActivity)) {
                            row.addView(billDate)
                            row.addView(tranDate)
                            row.addView(merchantBillNumber)
                            row.addView(remitterAccount)
                            row.addView(userId)
                            row.addView(currency)
                            row.addView(tranAmount)
                            row.addView(tranStatus)
                            row.addView(remark)
                            tableLayout.addView(row)
                        } else {
                            row.addView(tranDate)
                            row.addView(merchantBillNumber)
                            row.addView(tranAmount)
                            row.addView(tranStatus)
                            tableLayout.addView(row)
                        }
                        NetworkUtils.hideProgress(this@TransactionHistoryActivity)
                        row.setOnClickListener {
                            handleRowClick(transList)
                            EditNonEdit.hideKeyboard(editFilter)
                        }
                    }
                } else {
                    Toast.makeText(this@TransactionHistoryActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    NetworkUtils.hideProgress(this@TransactionHistoryActivity)
                //    println("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<TransData>>, t: Throwable) {
          //      t.printStackTrace()
                NetworkUtils.hideProgress(this@TransactionHistoryActivity)
                Toast.makeText(
                    this@TransactionHistoryActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    private fun callPdfDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.tran_pdf, null)
        builder.setView(view)
        val pdfBtn: Button = view.findViewById(R.id.gen_pdf)
        val excelBtn: Button = view.findViewById(R.id.gen_excel)
        val fromDate: EditText = view.findViewById(R.id.tran_from_date)
        val toDate: EditText = view.findViewById(R.id.tran_to_date)
        val alertDialog = builder.create()
        alertDialog.show()
        val window = alertDialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams
        val decorView = window?.decorView
        decorView?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_background))
        fromDate.setOnClickListener {
            showDatePickerDialog(true, fromDate, toDate,this,false)
        }
        toDate.setOnClickListener {
            showDatePickerDialog(false, fromDate, toDate,this,false)
        }
        pdfBtn.setOnClickListener {
                genPdf()
            alertDialog.dismiss()
        }
        excelBtn.setOnClickListener {
            genExcel()
            alertDialog.dismiss()
        }
    }
    private fun genPdf() {
        val call: Call<List<TransData>> = ApiClient.apiService.getTransactionDetails(
            merchantId,
            userid,
            tranFrom,
            tranTo,
            "PDF"
        )
        call.enqueue(object : Callback<List<TransData>> {
            override fun onResponse(
                call: Call<List<TransData>>,
                response: Response<List<TransData>>
            ) {
                if (response.isSuccessful) {
                    transList = response.body()?.sortedByDescending { dateFormat.parse(it.tran_date) }.orEmpty()
                  //  println(transList)
                    if (transList!!.isNotEmpty()) {
                        generatePdfFromData(transList)
                    }
                    else{
                        AlertDialogBox().showDialog(this@TransactionHistoryActivity,"No data available to generate the PDF.")
                    }
                } else {
                    Toast.makeText(this@TransactionHistoryActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    NetworkUtils.hideProgress(this@TransactionHistoryActivity)
               //     println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<TransData>>, t: Throwable) {
              //  t.printStackTrace()
                NetworkUtils.hideProgress(this@TransactionHistoryActivity)
                Toast.makeText(
                    this@TransactionHistoryActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    private fun generatePdfFromData(transDataList: List<TransData>) {
        try {
            // Create a temporary file in the app's private cache directory
            val cacheFolder = cacheDir
            val fileName = "TransactionDetails_${Encryption.generatePID()}.pdf"
            val pdfFile = File(cacheFolder, fileName)

            if (pdfFile.exists()) {
                pdfFile.delete()
            }
            // Create PDF document
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            // Define table parameters
            val headerFontSize = 10f
            val cellFontSize = 6f
            val rowsPerPage = 30
            val totalPages = Math.ceil(transDataList.size / rowsPerPage.toDouble()).toInt()
            // Add header image (optional)
            val drawable = ContextCompat.getDrawable(this, R.drawable.bob_pdf)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val imageData = ImageDataFactory.create(bitmap.toByteArray())
            val image = com.itextpdf.layout.element.Image(imageData).setWidth(100f).setHeight(50f)
            for (page in 0 until totalPages) {
                if (page > 0) {
                    document.add(AreaBreak()) // Add new page
                }
                document.add(image)
                // Define a table with 7 columns
                val table = Table(floatArrayOf(0.15f, 0.2f, 0.1f, 0.1f, 0.1f, 0.1f, 0.15f))
                table.setWidth(PageSize.A4.width - document.leftMargin - document.rightMargin)
                // Add table headers
                table.addHeaderCell(Cell().add(Paragraph("DATE").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("BILL NUMBER").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("CUSTOMER NAME").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("USER ID").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("CURRENCY").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("AMOUNT").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("STATUS").setFontSize(headerFontSize)))
                // Populate table rows
                val start = page * rowsPerPage
                val end = Math.min(start + rowsPerPage, transDataList.size)
                for (i in start until end) {
                    val transaction = transDataList[i]
                    table.addCell(Cell().add(Paragraph(transaction.tran_date).setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.merchant_bill_number ?: "N/A").setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.ipsx_account_name).setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.user_id ?: "N/A").setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.tran_currency ?: "N/A").setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(formatNumberWithGroups(transaction.tran_amount.toDouble()))
                        .setFontSize(cellFontSize)
                        .setTextAlignment(TextAlignment.RIGHT)))
                    table.addCell(Cell().add(Paragraph(transaction.tran_status).setFontSize(cellFontSize)))
                }
                // Add the table to the document
                document.add(table)
            }
            // Close the document
            document.close()
            // Show a success message
            Toast.makeText(this, "PDF generated successfully", Toast.LENGTH_LONG).show()
            // Open the PDF file using an intent
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", pdfFile)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
        } catch (e: Exception) {
          //  e.printStackTrace()
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_LONG).show()
        }
    }
    private fun Bitmap.toByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
    private fun genExcel() {
        val call: Call<List<TransData>> = ApiClient.apiService.getTransactionDetails(merchantId, userid, tranFrom, tranTo, "EXCEL")
        call.enqueue(object : Callback<List<TransData>> {
            override fun onResponse(
                call: Call<List<TransData>>,
                response: Response<List<TransData>>
            ) {
                if (response.isSuccessful) {
                    transList = response.body()?.sortedByDescending { it.tran_date }.orEmpty()
                  //  println(transList)

                    if (transList.isNotEmpty()) {
                        generateExcelFromData(transList)
                    } else {
                        AlertDialogBox().showDialog(this@TransactionHistoryActivity,"No data available to generate the Excel.")
                    }
                } else {
                    NetworkUtils.hideProgress(this@TransactionHistoryActivity)
                  //  println("Error: ${response.code()}")
                    Toast.makeText(this@TransactionHistoryActivity, "Failed to retrieve data. Error code: ${response.code()}", Toast.LENGTH_LONG
                    ).show()
                }
            }
            override fun onFailure(call: Call<List<TransData>>, t: Throwable) {
              //  t.printStackTrace()
                NetworkUtils.hideProgress(this@TransactionHistoryActivity)
                Toast.makeText(
                    this@TransactionHistoryActivity,
                    "Something went wrong at the server end",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    private fun generateExcelFromData(transDataList: List<TransData>) {
        // Check for write permissions
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
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
            // If the file already exists, delete it
            if (excelFile.exists()) {
                excelFile.delete()
            }

            // Create a new workbook and a sheet named "Transaction Data"
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Transaction Data")

            // Set column widths (approximate, adjust as needed)
            sheet.setColumnWidth(0, 5000) // TRANSACTION DATE
            sheet.setColumnWidth(1, 5000) // INVOICE NUMBER
            sheet.setColumnWidth(2, 7000) // CUSTOMER NAME
            sheet.setColumnWidth(3, 5000) // USER ID
            sheet.setColumnWidth(4, 4000) // CURRENCY
            sheet.setColumnWidth(5, 5000) // AMOUNT
            sheet.setColumnWidth(6, 5000) // STATUS

            // Create header row
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("DATE")
            headerRow.createCell(1).setCellValue("BILL NUMBER")
            headerRow.createCell(2).setCellValue("CUSTOMER NAME")
            headerRow.createCell(3).setCellValue("USER ID")
            headerRow.createCell(4).setCellValue("CURRENCY")
            headerRow.createCell(5).setCellValue("AMOUNT")
            headerRow.createCell(6).setCellValue("STATUS")

            // Fill data rows
            for ((index, transaction) in transDataList.withIndex()) {
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(transaction.tran_date)
                row.createCell(1).setCellValue(transaction.merchant_bill_number ?: "N/A")
                row.createCell(2).setCellValue(transaction.ipsx_account_name)
                row.createCell(3).setCellValue(transaction.user_id ?: "N/A")
                row.createCell(4).setCellValue(transaction.tran_currency ?: "N/A")
                row.createCell(5)
                    .setCellValue(formatNumberWithGroups(transaction.tran_amount.toDouble()))
                row.createCell(6).setCellValue(transaction.tran_status)
            }
            // Write the Excel file to disk using 'use' to auto-close the resource
            FileOutputStream(excelFile).use { fileOut ->
                workbook.write(fileOut)
            }
            workbook.close()
            // Show success message
            Toast.makeText(this, "Excel generated  at ${excelFile.absolutePath}", Toast.LENGTH_LONG).show()
            NotifyDownload.createNotificationChannel(this)
            NotifyDownload.openExcelFile(this,excelFile)
        } catch (e: Exception) {
          //  e.printStackTrace()
            Toast.makeText(this, "Error generating Excel", Toast.LENGTH_LONG).show()
        }
    }
}