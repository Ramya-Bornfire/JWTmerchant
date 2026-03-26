package com.bornfire.merchantqrcode.AdminScreens

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.Feesdata
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DetailScreen.FeesDetails
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.NotifyDownload
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.showDatePickerDialog
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.tranFrom
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.tranTo
import com.bornfire.merchantqrcode.Utils.TextforTable.createTextView
import com.bornfire.merchantqrcode.Utils.TextforTable.formatNumberWithGroups
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
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
import com.itextpdf.layout.properties.TextAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.bornfire.merchantqrcode.BuildConfig

class FeesAndCharge : BaseActivity() {
    lateinit var feesList: List<Feesdata>
    lateinit var  tableLayout: TableLayout
    private lateinit var edFilter : EditText
    private var isDataFiltered = false
    lateinit var backBtn: ImageView
    lateinit var fcSearch:FrameLayout
    lateinit var nodata:TextView
    lateinit var cur_date:EditText
    lateinit var pdf_dwn:ImageView

    val merchantdata = SharedMerchantDataObj.merchantData
    lateinit var fc_help_image:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feesandcharge)
        tableLayout = findViewById(R.id.tableLayout)
        supportActionBar?.hide()
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        //set the back button
        backBtn=findViewById(R.id.backBtn)
        fcSearch = findViewById(R.id.fcSearch)
        nodata=findViewById(R.id.emptyTextView)
        edFilter = findViewById(R.id.editFilter)
        backBtn.setOnClickListener{
            handleBackPress()
        }
        fc_help_image = findViewById(R.id.fc_help_image)
        fc_help_image.setOnClickListener(){
            HelpInfo.getInfo(this,"22")
        }
        pdf_dwn = findViewById(R.id.pdf_dwn)
        pdf_dwn.setOnClickListener(){
            callPdfDialog()
        }
        edFilter.setOnClickListener {
            edFilter.isFocusable = true  // Make EditText focusable
            edFilter.isFocusableInTouchMode = true  // Allow focus in touch mode
            edFilter.requestFocus()  // Request focus for EditText
            showKeyboard()  // Show the keyboard when EditText is clicked
        }
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
        tranFrom = dateString
        tranTo = dateString
        cur_date.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected day and month to always be two digits
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1) // Adding 1 to the month as it's 0-based
                val selectedDate = "$formattedDay-$formattedMonth-$selectedYear"

                // Set the formatted date in the EditText
                cur_date.setText(selectedDate)
                tranFrom = selectedDate
                tranTo = selectedDate

                // Call the function to fetch customer transactions
                fetchUnitData()
                edFilter.text.clear()
                edFilter.isFocusable = false  // Make EditText unfocusable
                edFilter.isFocusableInTouchMode = false  // Prevent it from gaining focus in touch mode
                hideKeyboard()
            }, year, month, day)

            // Restrict future dates
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis

            // Show the DatePickerDialog
            datePickerDialog.show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })

        //check the merchant rep or unit rep to fetch the fees and charges list
        if (NetworkUtils.isNetworkAvailable(this)) {
                fetchUnitData()
        }
        else {
            NetworkUtils.NoInternetAlert(this)
        }


        edFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if(edFilter.text.toString().length>0){
                    val filterQuery = edFilter.text.toString().trim()
                    filterData(filterQuery)
                }
                else{
                    fetchUnitData()
                }
            }
        })
    }
    private fun filterData(query: String?) {
        val filteredList = feesList?.filter { fee ->
            val dateTime1 = fee.tran_date
            val containsQuery = { value: String? ->
                value?.contains(query!!, ignoreCase = true) ?: false
            }
            val formattedAmount = fee.tran_amount?.let { amount ->
                formatNumberWithGroups(amount.toDouble())
            } ?: "0.00"
            val formattedAmount1 = fee.conv_fee?.let { amount ->
                formatNumberWithGroups(amount.toDouble())
            } ?: "0.00"
            containsQuery(dateTime1) ||
            containsQuery(fee.merchant_bill_number)||containsQuery(fee.user_id)||
                    containsQuery(fee.sequence_unique_id) ||
                    containsQuery(fee.initiator_bank) ||
                    containsQuery(fee.tran_currency) ||
                    containsQuery(formattedAmount) ||
                    containsQuery(formattedAmount1)
        }
        if (filteredList != null) {
            if (filteredList.isNotEmpty()) {
                isDataFiltered = true
                updateTable(filteredList)
            } else {
                AlertDialogBox().showDialog(this, "No matching data found")
            }
        }
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
    private fun fetchUnitData() {
        tableLayout.removeAllViews()
        createHeaderRow() // Use the new function to create headers

        val call: Call<List<Feesdata>> = ApiClient.apiService.getFeesChargesList(
            merchantdata!!.merchant_user_id.toString(),
            merchantdata.unit_id.toString(),
            tranFrom, tranTo,
            "MOBILEVIEW"
        )
        call.enqueue(object : Callback<List<Feesdata>> {
            override fun onResponse(call: Call<List<Feesdata>>, response: Response<List<Feesdata>>) {
                if (response.isSuccessful) {
                    feesList = response.body() ?: listOf()
                    if (feesList.isNotEmpty()) {
                        fcSearch.visibility = View.VISIBLE
                        nodata.visibility = View.INVISIBLE
                        feesList = feesList.sortedByDescending { it.tran_date }
                        if (feesList.size == 100) {
                            AlertDialog.Builder(this@FeesAndCharge)
                                .setTitle("Alert")
                                .setMessage("You will be able to view only your latest 100 fees and charges list. For complete list of feesd and charged in the mentioned time period please use the pdf option.")
                                .setNegativeButton("Ok", null)
                                .setCancelable(false)
                                .show()
                        }
                        updateTable(feesList) // Use the updateTable method to populate the table
                    } else {
                        fcSearch.visibility = View.INVISIBLE
                        nodata.visibility = View.VISIBLE
                    }
                    NetworkUtils.hideProgress(this@FeesAndCharge)
                }
                else{
                    Toast.makeText(this@FeesAndCharge, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Feesdata>>, t: Throwable) {
               // t.printStackTrace()
                NetworkUtils.hideProgress(this@FeesAndCharge)
                Toast.makeText(this@FeesAndCharge, "Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun updateTable(filteredList: List<Feesdata>) {
        tableLayout.removeAllViews()
        createHeaderRow() // Reuse the header creation method

        filteredList.forEach { feesList ->
            val row = createTableRow(feesList)
            tableLayout.addView(row)
        }
    }
    private fun createHeaderRow() {
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.liteOrange))
        val headers = arrayOf("DATE", "MESSAGE REF", "BILL NUMBER", "USER ID","CURRENCY", "AMOUNT", "TRANSACTION FEES")
        headers.forEach { header ->
            val textView = createTextView(this,header)
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            textView.textSize = 15f
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)
    }
    private fun createTableRow(feesList: Feesdata): TableRow {
        val row = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        }
        val formattedDate = feesList.tran_date
        val formattedAmount = formatNumberWithGroups(feesList.tran_amount?.toDouble() ?: 0.0)
        val formattedFees = formatNumberWithGroups(feesList.conv_fee?.toDouble() ?: 0.0)
        row.apply {
            addView(createTextView(this@FeesAndCharge,formattedDate))
            addView(createTextView(this@FeesAndCharge,feesList.sequence_unique_id ?: " "))
            addView(createTextView(this@FeesAndCharge,feesList.merchant_bill_number ?: ""))
            addView(createTextView(this@FeesAndCharge,feesList.user_id ?: ""))
                // addView(createTextView(feesList.ipsx_account_name ?: ""))
       //     addView(createTextView(feesList.initiator_bank ?: ""))
            addView(createTextView(this@FeesAndCharge,feesList.tran_currency ?: ""))
            addView(createTextView(this@FeesAndCharge,formattedAmount))
            addView(createTextView(this@FeesAndCharge,formattedFees))

        }

        row.setOnClickListener {
            navigateToDetails(feesList, formattedAmount, formattedFees)
            edFilter.text.clear()
        }
        return row
    }
    private fun navigateToDetails(feesList: Feesdata, formattedAmount: String, formattedFees: String) {
        val intent = Intent(applicationContext, FeesDetails::class.java).apply {
            putExtra("Date", feesList.tran_date)
            putExtra("MessageRef", feesList.sequence_unique_id)
            putExtra("BillNumber", feesList.merchant_bill_number)
            putExtra("Userid", feesList.user_id)
            putExtra("CustomerName", feesList.ipsx_account_name)
            putExtra("Bank", feesList.initiator_bank)
            putExtra("Remacc", feesList.ipsx_account)
            putExtra("Currency", feesList.tran_currency)
            putExtra("Amount", formattedAmount)
            putExtra("Fees", formattedFees)
            putExtra("AppliedFlag", feesList.charge_app_flg)
        }
        startActivity(intent)
    }

    private fun handleBackPress() {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
            finish()

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
        fromdate.setOnClickListener(){
            showDatePickerDialog(true,fromdate,todate,this,false)
        }
        todate.setOnClickListener(){
            showDatePickerDialog(false,fromdate,todate,this,false)
        }
        pdfBtn.setOnClickListener {
            NetworkUtils.showProgress(this@FeesAndCharge)
            genPdf(true)
            alertDialog.dismiss()
        }
        excelBtn.setOnClickListener{
            NetworkUtils.showProgress(this@FeesAndCharge)
            genPdf(false)
            alertDialog.dismiss()
        }
    }
    private fun genPdf(ispdf:Boolean) {
        val call: Call<List<Feesdata>> = ApiClient.apiService.getFeesChargesList(
            merchantdata!!.merchant_user_id.toString(), merchantdata.unit_id.toString(), tranFrom, tranTo, "PDF")
        call.enqueue(object : Callback<List<Feesdata>> {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onResponse(
                call: Call<List<Feesdata>>,
                response: Response<List<Feesdata>>
            ) {
                if (response.isSuccessful) {
                    feesList = response.body()!!
                 //   println(feesList)
                    if(feesList.isNotEmpty()){
                        if(ispdf){
                            generatePdfFromData(feesList)
                        }
                        else{
                            generateExcelFromData(feesList)
                        }
                    }
                    else{
                        NetworkUtils.hideProgress(this@FeesAndCharge)
                        AlertDialogBox().showDialog(this@FeesAndCharge,"No data available to generate the PDF.")
                    }
                } else {
                    Toast.makeText(this@FeesAndCharge, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    NetworkUtils.hideProgress(this@FeesAndCharge)
                  //  println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Feesdata>>, t: Throwable) {
               // t.printStackTrace()
                NetworkUtils.hideProgress(this@FeesAndCharge)
                Toast.makeText(this@FeesAndCharge,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun generatePdfFromData(transDataList: List<Feesdata>) {
        // Check for write permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            return
        }
        try {
            val cacheFolder = cacheDir
            val fileName = "Fees and Charges" + Encryption.generatePID()
            val pdfFile = File(cacheFolder, "$fileName.pdf")
            if (pdfFile.exists()) {
                pdfFile.delete()
            }
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            // Prepare the image
            val drawable = ContextCompat.getDrawable(this, R.drawable.bob_pdf) // Replace with your drawable name
            val bitmap = (drawable as BitmapDrawable).bitmap
            val imageData = ImageDataFactory.create(bitmap.toByteArray())
            val image = com.itextpdf.layout.element.Image(imageData)
            image.setWidth(100f) // Set the desired image width
            image.setHeight(50f) // Set the desired image height
            // Define table parameters
            val headerFontSize = 10f
            val cellFontSize = 6f
            val rowsPerPage = 30 // Define the number of rows per page (adjust as needed)
            val totalPages = Math.ceil(transDataList.size / rowsPerPage.toDouble()).toInt()
            for (page in 0 until totalPages) {
                if (page > 0) {
                    document.add(AreaBreak()) // Adds a new page
                }
                // Add image to every page
                document.add(image)
                // Define a table with 7 columns
                val table = Table(floatArrayOf(0.15f, 0.2f, 0.1f, 0.1f, 0.1f, 0.1f, 0.15f))
                table.setWidth(PageSize.A4.width - document.leftMargin - document.rightMargin)
                // Add table headers
                table.addHeaderCell(Cell().add(Paragraph("BILL DATE").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("BILL NUMBER").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("BANK").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("CURRENCY").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("AMOUNT").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("FEES").setFontSize(headerFontSize)))
                table.addHeaderCell(Cell().add(Paragraph("APPLY FLAG").setFontSize(headerFontSize)))
                // Add rows to the table
                val start = page * rowsPerPage
                val end = Math.min(start + rowsPerPage, transDataList.size)
                for (i in start until end) {
                    val transaction = transDataList[i]
                    table.addCell(Cell().add(Paragraph(transaction.tran_date ?: "N/A").setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.merchant_bill_number ?: "N/A").setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.initiator_bank ?: "N/A").setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.tran_currency ?: "N/A").setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(formatNumberWithGroups(transaction.tran_amount?.toDouble() ?: 0.0)).setFontSize(cellFontSize).setTextAlignment(
                        TextAlignment.RIGHT)))
                    table.addCell(Cell().add(Paragraph(formatNumberWithGroups(transaction.conv_fee?.toDouble() ?: 0.0)).setFontSize(cellFontSize)))
                    table.addCell(Cell().add(Paragraph(transaction.charge_app_flg ?: "N/A").setFontSize(cellFontSize)))
                }
                // Add the table to the document
                document.add(table)
            }
            // Close document before adding page numbers
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
            NetworkUtils.hideProgress(this@FeesAndCharge)
            // Notify user of PDF generation
          //  Toast.makeText(this, "PDF generated at ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
            // Open the generated PDF
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
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun generateExcelFromData(transDataList: List<Feesdata>) {
        // Check for write permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
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
            val fileName = "Fees_charges_" + Encryption.generatePID()
            val excelFile = File(documentsFolder, "$fileName.xlsx")
            // If the file already exists, delete it
            if (excelFile.exists()) {
                excelFile.delete()
            }
            // Create a new workbook and a sheet named "Transaction Data"
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Fees and Charges Data")
            // Set column widths (approximate, adjust as needed)
            sheet.setColumnWidth(0, 5000) // TRANSACTION DATE
            sheet.setColumnWidth(1, 5000) // INVOICE NUMBER
            sheet.setColumnWidth(2, 7000) // CUSTOMER NAME
            sheet.setColumnWidth(3, 5000) // USER ID
            sheet.setColumnWidth(4, 4000) // CURRENCY
            sheet.setColumnWidth(5, 5000) // AMOUNT
            sheet.setColumnWidth(6, 5000) // STATUS
            sheet.setColumnWidth(7, 5000) // STATUS
            // Create header row
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("DATE")
            headerRow.createCell(1).setCellValue("MESSAGE REF")
            headerRow.createCell(2).setCellValue("BANK")
            headerRow.createCell(3).setCellValue("BENEFICIARY ACCOUNT")
            headerRow.createCell(4).setCellValue("CURRENCY")
            headerRow.createCell(5).setCellValue("CHARGE APPLIED FLAG")
            headerRow.createCell(6).setCellValue("CONVENIENCE FEES")
            headerRow.createCell(7).setCellValue("AMOUNT")
            // Fill data rows
            for ((index, transaction) in transDataList.withIndex()) {
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(transaction.tran_date ?: "N/A")
                row.createCell(1).setCellValue(transaction.sequence_unique_id ?: "N/A")
                row.createCell(2).setCellValue(transaction.initiator_bank ?: "N/A")
                row.createCell(3).setCellValue(transaction.ipsx_account ?: "N/A")
                row.createCell(4).setCellValue(transaction.tran_currency ?: "N/A")
                row.createCell(5).setCellValue(transaction.charge_app_flg ?: "N/A")
                row.createCell(6).setCellValue(transaction.conv_fee)
                row.createCell(7).setCellValue(formatNumberWithGroups(transaction.tran_amount!!.toDouble())?: "N/A") }
            // Write the Excel file to disk using 'use' to auto-close the resource
            FileOutputStream(excelFile).use { fileOut ->
                workbook.write(fileOut)
            }
            workbook.close()
            // Show success message
            Toast.makeText(this, "Excel generated at ${excelFile.absolutePath}", Toast.LENGTH_LONG).show()
            NotifyDownload.createNotificationChannel(this)
            NotifyDownload.openExcelFile(this,excelFile)
        } catch (e: Exception) {
           // e.printStackTrace()
            Toast.makeText(this, "Error generating Excel", Toast.LENGTH_LONG).show()
        }
    }
}