package com.bornfire.merchantqrcode.AdminScreens

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.CustomerData
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DetailScreen.CustomerDetails
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.NotifyDownload
import com.bornfire.merchantqrcode.Utils.NullCheck.getValidText
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.showDatePickerDialog
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.tranFrom
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.tranTo
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
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.FileInputStream
import kotlin.math.ceil

class CustomerTransaction : BaseActivity() {
    lateinit var custransData: List<CustomerData>
    lateinit var  tableLayout: TableLayout
    private lateinit var edFilter : EditText
    private var isDataFiltered = false
    lateinit var backBtn: ImageView
    lateinit var nodata:TextView
    val merchantdata = SharedMerchantDataObj.merchantData
    private lateinit var curDate:EditText
    private lateinit var pdfDown:ImageView
    private lateinit var ctHelpimage:ImageView
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customertransaction)
         tableLayout = findViewById(R.id.tableLayout)
         edFilter = findViewById(R.id.editFilter)
        nodata=findViewById(R.id.emptyTextView)
        pdfDown = findViewById(R.id.pdf_dwn)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        backBtn=findViewById(R.id.backBtn)
        backBtn.setOnClickListener{
            handleBackPress() }
        ctHelpimage = findViewById(R.id.ct_help_image)
        ctHelpimage.setOnClickListener{
            HelpInfo.getInfo(this,"20") }
        curDate = findViewById(R.id.cur_date)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentDate: Date = calendar.time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateString: String = dateFormat.format(currentDate)
        curDate.setText(dateString)
        tranFrom = dateString
        tranTo = dateString
        supportActionBar?.hide()
        pdfDown.setOnClickListener{
            callPdfDialog()
        }
        if (NetworkUtils.isNetworkAvailable(this)) {
            fetchCusTrans()
        } else {
            NetworkUtils.NoInternetAlert(this)
        }
        curDate.setOnClickListener{
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1) // Adding 1 to the month as it's 0-based
                val selectedDate = "$formattedDay-$formattedMonth-$selectedYear"
                curDate.setText(selectedDate)
                tranFrom = selectedDate
                tranTo = selectedDate
                fetchCusTrans()
                edFilter.text.clear()
                edFilter.isFocusable = false  // Make EditText unfocusable
                edFilter.isFocusableInTouchMode = false  // Prevent it from gaining focus in touch mode
                hideKeyboard()
            }, year, month, day)
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()

        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            } }
        onBackPressedDispatcher.addCallback(this, callback)
        edFilter.setOnClickListener {
            edFilter.isFocusable = true  // Make EditText focusable
            edFilter.isFocusableInTouchMode = true  // Allow focus in touch mode
            edFilter.requestFocus()  // Request focus for EditText
            showKeyboard()  // Show the keyboard when EditText is clicked
        }
        edFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if(edFilter.text.toString().isNotEmpty()){
                    val filterQuery = edFilter.text.toString().trim()
                    filterData(filterQuery)
                }
                else{
                    fetchCusTrans()
                }
            }
        })
    }
    private fun filterData(query: String) {
        val normalizedQuery = query.trim().lowercase()

        val filteredList = custransData.filter { fee ->
            val transactionDateMatch = fee?.tran_date?.contains(normalizedQuery, ignoreCase = true) ?: false
            val messageRefMatch = fee?.sequence_unique_id?.contains(normalizedQuery, ignoreCase = true) ?: false
            val billNumberMatch = fee?.merchant_bill_number?.contains(normalizedQuery, ignoreCase = true) ?: false
            val currencyMatch = fee?.tran_currency?.contains(normalizedQuery, ignoreCase = true) ?: false
            val statusMatch = fee?.tran_status?.contains(normalizedQuery, ignoreCase = true) ?: false
            val userIdMatch = fee?.user_id?.contains(normalizedQuery, ignoreCase = true) ?: false
            // Handle amount search for both decimal and non-decimal queries
            val amountMatch = fee.tran_amount.let { amount ->
                val formattedAmount = formatNumberWithGroups(amount.toDouble()).trim().lowercase()

                // Check if the query matches either the formatted amount or a version without the decimal point
                val noDecimalQuery = query.replace(".", "")
                val noDecimalAmount = formattedAmount.replace(".", "")

                formattedAmount.contains(normalizedQuery) || noDecimalAmount.contains(noDecimalQuery)
            }

            // Match any of the fields
            transactionDateMatch || messageRefMatch || billNumberMatch || currencyMatch || amountMatch || statusMatch || userIdMatch
        }

        if (filteredList.isNotEmpty()) {
            isDataFiltered = true
            updateTable(filteredList)
        } else {
            AlertDialogBox().showDialog(this, "No matching data found")
        }
    }
    private fun updateTable(filteredList: List<CustomerData>) {
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        tableLayout.removeAllViews()  // Clear existing rows

        addTableHeader(tableLayout) // Add the header

        filteredList.forEach { custransData ->
            val row = createTableRow()
            addRowData(row, custransData)
            tableLayout.addView(row)
            row.setOnClickListener {
                openCustomerDetails(custransData)

            }
        }
        NetworkUtils.hideProgress(this@CustomerTransaction)
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
    private fun fetchCusTrans() {
        tableLayout.removeAllViews()
        addTableHeader(tableLayout)

        val call: Call<List<CustomerData>> = ApiClient.apiService.getCustomerTransactionList(
            merchantdata!!.merchant_user_id.toString(),
            merchantdata.unit_id.toString(),
            tranFrom, tranTo, "MOBILEVIEW"
        )
        call.enqueue(object : Callback<List<CustomerData>> {
            override fun onResponse(call: Call<List<CustomerData>>, response: Response<List<CustomerData>>) {
                NetworkUtils.hideProgress(this@CustomerTransaction)
                if (response.isSuccessful) {
                    custransData = response.body()?.sortedByDescending { it.tran_date } ?: emptyList()
                  //  println("Customer Transactoion List ----->  $custransData")
                    if (custransData.isNotEmpty()) {
                        if (custransData.size == 100) {
                            AlertDialog.Builder(this@CustomerTransaction)
                                .setTitle("Alert")
                                .setMessage("You will be able to view only your latest 100 customer transactions. For complete list of customer transactions in the mentioned time period please use the pdf option.")
                                .setNegativeButton("Ok", null)
                                .setCancelable(false)
                                .show()
                        }
                        edFilter.visibility = View.VISIBLE
                        nodata.visibility = View.INVISIBLE
                        custransData.forEach { custransData ->
                            val row = createTableRow()
                            addRowData(row, custransData)
                            tableLayout.addView(row)
                            row.setOnClickListener {
                                openCustomerDetails(custransData)
                            }
                        }
                    }
                    else{
                        edFilter.visibility = View.INVISIBLE
                        nodata.visibility = View.VISIBLE
                    }
                }
                else {
                    Toast.makeText(this@CustomerTransaction, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                 //   println("Error: ${response.code()}")
                    Toast.makeText(this@CustomerTransaction, "Failed to load data", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<CustomerData>>, t: Throwable) {
                NetworkUtils.hideProgress(this@CustomerTransaction)
               // t.printStackTrace()
                Toast.makeText(this@CustomerTransaction, "Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun addTableHeader(tableLayout: TableLayout) {
        val headerRow = TableRow(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@CustomerTransaction, R.color.liteOrange))
        }

        val headers = arrayOf("DATE", "MESSAGE REF", "BILL NUMBER", "USER ID","CURRENCY", "AMOUNT", "STATUS")
        headers.forEach { header ->
            val textView = createTextView(this,header).apply {
                setTypeface(null, android.graphics.Typeface.BOLD)
                textSize = 15f
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)
    }
    private fun createTableRow(): TableRow {
        return TableRow(this@CustomerTransaction).apply {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        }
    }
    private fun addRowData(row: TableRow, custransData: CustomerData) {
        row.apply {
            addView(createTextView(this@CustomerTransaction,custransData.tran_date))
            addView(createTextView(this@CustomerTransaction,custransData.sequence_unique_id))
            addView(createTextView(this@CustomerTransaction,custransData.merchant_bill_number))
            addView(createTextView(this@CustomerTransaction,custransData.user_id))
           // addView(createTextView(custransData.ipsx_account_name ?: ""))
            addView(createTextView(this@CustomerTransaction,custransData.tran_currency))
            val amount = custransData.tran_amount
            addView(createTextView(this@CustomerTransaction,formatNumberWithGroups(amount.toDouble())))
            addView(createTextView(this@CustomerTransaction,custransData.tran_status))
        }
    }
    private fun openCustomerDetails(custransData: CustomerData) {
        val intent = Intent(applicationContext, CustomerDetails::class.java).apply {
            putExtra("Date", custransData.tran_date)
            putExtra("MessageRef", custransData.sequence_unique_id)
            putExtra("Bank", custransData.initiator_bank)
            putExtra("RemAcc", custransData.ipsx_account)
            putExtra("Currency", custransData.tran_currency)
            putExtra("BillNumber", custransData.merchant_bill_number)
            putExtra("userid", custransData.user_id)
            putExtra("CustomerName", custransData.ipsx_account_name)
            val amount = custransData.tran_amount
            putExtra("Amount", formatNumberWithGroups(amount.toDouble()))
            putExtra("Ben", custransData.cim_account)
            putExtra("Remarks", custransData.reversal_remarks)
            putExtra("Status", custransData.tran_status)
        }
        startActivity(intent)
    }
    private fun callPdfDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.tran_pdf, null)
        builder.setView(view)
        val pdfBtn: Button = view.findViewById(R.id.gen_pdf)
        val excelBtn: Button = view.findViewById(R.id.gen_excel)
        val fromdate:EditText = view.findViewById(R.id.tran_from_date)
        val todate:EditText = view.findViewById(R.id.tran_to_date)
        val alertDialog = builder.create()
        alertDialog.show()
        val window = alertDialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams
        val decorView = window?.decorView
        decorView?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_background))
        fromdate.setOnClickListener{
            showDatePickerDialog(true,fromdate,todate,this,false)
        }
        todate.setOnClickListener{
            showDatePickerDialog(false,fromdate,todate,this,false)
        }
        pdfBtn.setOnClickListener {
            genPdf()
            NetworkUtils.showProgress(this@CustomerTransaction)
            alertDialog.dismiss()
        }
        excelBtn.setOnClickListener{
            genExcel()
            alertDialog.dismiss()
        }
    }
    private fun handleBackPress() {
            val intent = Intent(this, AdminActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
    }
    private fun genPdf() {
        val call: Call<List<CustomerData>> = ApiClient.apiService.getCustomerTransactionList(merchantdata?.merchant_user_id.toString(),merchantdata?.unit_id.toString(),tranFrom,tranTo,"PDF")
        call.enqueue(object : Callback<List<CustomerData>> {
            override fun onResponse(
                call: Call<List<CustomerData>>,
                response: Response<List<CustomerData>>
            ) {
                if (response.isSuccessful) {
                    custransData = response.body()?.sortedByDescending { dateFormat.parse(it.tran_date) }.orEmpty()
                  //  println("Customer Transaction List ------>  $custransData")
                    if(custransData.isNotEmpty()){
                        generatePdfFromData(custransData)
                    }
                    else{
                        NetworkUtils.hideProgress(this@CustomerTransaction)
                        AlertDialogBox().showDialog(this@CustomerTransaction,"No data available to generate the PDF.")
                    }
                }
                else {
                    Toast.makeText(this@CustomerTransaction, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    NetworkUtils.hideProgress(this@CustomerTransaction)
                 //   println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<CustomerData>>, t: Throwable) {
              //  t.printStackTrace()
                NetworkUtils.hideProgress(this@CustomerTransaction)
                Toast.makeText(this@CustomerTransaction,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun generatePdfFromData(transDataList: List<CustomerData>) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            return
        }
        try {
            val cacheFolder = cacheDir
            val fileName = "Transaction_Details_" + Encryption.generatePID()
            val pdfFile = File(cacheFolder, "$fileName.pdf")
            if (pdfFile.exists()) {
                pdfFile.delete()
            }
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            // Define table parameters
            val headerFontSize = 10f
            val cellFontSize = 6f
            val rowsPerPage = 50
            val totalPages = ceil(transDataList.size / rowsPerPage.toDouble()).toInt()

            // Image setup
            val drawable = ContextCompat.getDrawable(this, R.drawable.bob_pdf)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val imageData = ImageDataFactory.create(bitmap.toByteArray())
            val image = Image(imageData)
            image.setWidth(100f)
            image.setHeight(50f)

            for (page in 0 until totalPages) {
                if (page > 0) {
                    document.add(AreaBreak()) // Adds a new page
                }
                // Add the image at the beginning of each page
                document.add(image)
                // Create the table and add headers
                val table = Table(floatArrayOf(0.15f, 0.2f, 0.1f, 0.1f, 0.1f, 0.1f, 0.15f))
                table.width = UnitValue.createPercentValue(100f)
                table.addHeaderCell(Cell().add(Paragraph("DATE").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("BILL NUMBER").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("CUSTOMER NAME").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("USER ID").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("CURRENCY").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("AMOUNT").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("STATUS").setFontSize(headerFontSize)))

                val start = page * rowsPerPage
                val end = (start + rowsPerPage).coerceAtMost(transDataList.size)
                for (i in start until end) {
                    val transaction = transDataList[i]
                    table.addCell(Cell().add(Paragraph(transaction.tran_date).setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.merchant_bill_number).setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.ipsx_account_name).setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.user_id).setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.tran_currency).setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(formatNumberWithGroups(transaction.tran_amount.toDouble())).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)))
                    table.addCell(Cell().add(Paragraph(transaction.tran_status).setFontSize(cellFontSize)))
                }
                document.add(table)
            }
            // Close document before adding page numbers
            document.close()
            // Open the document again to add page numbers
            val pdfReader = PdfReader(FileInputStream(pdfFile))
            val pdfWriterForPageNumbers = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocumentForPageNumbers = PdfDocument(pdfReader, pdfWriterForPageNumbers)
            val pageCount = pdfDocumentForPageNumbers.numberOfPages

            for (i in 1..pageCount) {
                val page = pdfDocumentForPageNumbers.getPage(i)
                val canvas = PdfCanvas(page.newContentStreamBefore(), page.resources, pdfDocumentForPageNumbers)
                canvas.beginText()
                val pageNumberFont: PdfFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD)
                canvas.setFontAndSize(pageNumberFont, 10f)
                val pageSize = page.pageSize
                canvas.moveText((pageSize.width / 2 - 15).toDouble(), 30.0)
                canvas.showText("Page $i of $pageCount")
                canvas.endText()
            }
            pdfDocumentForPageNumbers.close()
            NetworkUtils.hideProgress(this@CustomerTransaction)
          //  Toast.makeText(this, "PDF generated at ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", pdfFile)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
        } catch (e: Exception) {
            //e.printStackTrace()
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_LONG).show()
        }
    }
    private fun Bitmap.toByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
    private fun genExcel() {
        val call: Call<List<CustomerData>> = ApiClient.apiService.getCustomerTransactionList(
            merchantdata?.merchant_user_id.toString(), merchantdata?.unit_id.toString(), tranFrom, tranTo, "EXCEL")
        call.enqueue(object : Callback<List<CustomerData>> {
            override fun onResponse(call: Call<List<CustomerData>>, response: Response<List<CustomerData>>) {
                if (response.isSuccessful) {
                    custransData = response.body()?.sortedByDescending { dateFormat.parse(it.tran_date) }.orEmpty()
                    //println("Customer Transaction List --->  $custransData")
                    if (custransData.isNotEmpty()) {
                        generateExcelFromData(custransData)
                    } else {
                        AlertDialogBox().showDialog(this@CustomerTransaction,"No data available to generate Excel.")
                    }
                }
                else {
                    NetworkUtils.hideProgress(this@CustomerTransaction)
                  //  println("Error: ${response.code()}")
                    Toast.makeText(applicationContext, "Failed to retrieve data. Error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<CustomerData>>, t: Throwable) {
               // t.printStackTrace()
                NetworkUtils.hideProgress(this@CustomerTransaction)
                Toast.makeText(this@CustomerTransaction, "Something went wrong at the server end", Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    private fun generateExcelFromData(transDataList: List<CustomerData>) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
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
            val columnWidths = arrayOf(5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000)
            columnWidths.forEachIndexed { index, width -> sheet.setColumnWidth(index, width) }

            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf(
                "DATE", "MESSAGE REF", "BANK", "MERCHANT ID", "USER ID", "CURRENCY",
                "AMOUNT", "STATUS", "REMITTER ACC", "BENEFICIARY ACC", "REMITTER NAME",
                "BENEFICIARY NAME", "REVERSAL REMARK", "REVERSAL DATE", "REVERSAL AMOUNT",
                "AUTHORIZED USER", "AUTHORIZED TIME"
            )
            headers.forEachIndexed { index, title ->
                headerRow.createCell(index).setCellValue(title) }
            // Fill data rows
            transDataList.forEachIndexed { index, transaction ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(getValidText(transaction.tran_date))
                row.createCell(1).setCellValue(getValidText(transaction.sequence_unique_id))
                row.createCell(2).setCellValue(getValidText(transaction.initiator_bank))
                row.createCell(3).setCellValue(getValidText(transaction.merchant_id))
                row.createCell(4).setCellValue(getValidText(transaction.user_id))
                row.createCell(5).setCellValue(getValidText(transaction.tran_currency))
                row.createCell(6).setCellValue(getValidText(formatNumberWithGroups(transaction.tran_amount.toDouble())))
                row.createCell(7).setCellValue(getValidText(transaction.tran_status))
                row.createCell(8).setCellValue(getValidText(transaction.cim_account))
                row.createCell(9).setCellValue(getValidText(transaction.ipsx_account))
                row.createCell(10).setCellValue(getValidText(transaction.cim_account_name))
                row.createCell(11).setCellValue(getValidText(transaction.ipsx_account_name))
                row.createCell(12).setCellValue(getValidText(transaction.reversal_remarks))
                row.createCell(13).setCellValue(getValidText(transaction.reversal_date))
                row.createCell(14).setCellValue(formatNumberWithGroups(transaction.reversal_amount.toDouble()))
                row.createCell(15).setCellValue(getValidText(transaction.auth_user))
                row.createCell(16).setCellValue(getValidText(transaction.auth_time))
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
        } catch (e: Exception) {
          //  e.printStackTrace()
            Toast.makeText(this, "Error generating Excel", Toast.LENGTH_LONG).show()
        }
    }
}