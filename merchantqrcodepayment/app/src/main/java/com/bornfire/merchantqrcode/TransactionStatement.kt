package com.bornfire.merchantqrcode

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.StatemenResponse
import com.bornfire.merchantqrcode.retrofit.ApiClient
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.util.Date
import org.apache.poi.ss.usermodel.Workbook
import java.time.LocalDate
import android.Manifest
import android.content.ContentValues
import android.provider.MediaStore
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.SuccesssDialog
import com.bornfire.merchantqrcode.retrofit.Encryption
import org.apache.poi.ss.usermodel.Sheet

class TransactionStatement : BaseActivity() {
    val userData = SharedUserDataObj.userData
    val merchantdata = SharedMerchantDataObj.merchantData
    lateinit var downloadbtn:ImageView
    lateinit var accType:TextView
    lateinit var address1:TextView
    lateinit var address2:TextView
    lateinit var address3:TextView
    lateinit var address4:TextView
    lateinit var accStatementDate:TextView
    lateinit var repId:EditText
    lateinit var repName:EditText
    lateinit var unitName:EditText
    lateinit var unitId:EditText
    lateinit var deviceId:EditText
    lateinit var accNumber:EditText
    lateinit var accCurrency:EditText
    lateinit var searchBy:EditText
    lateinit var searchQuery:EditText
    lateinit var stmtbutton:Button
    lateinit var editfromDate:EditText
    lateinit var edittoDate:EditText
    private var fromDate: Calendar = Calendar.getInstance()
    private var toDate: Calendar = Calendar.getInstance()
    var FromDate = ""
    var ToDate =""
    lateinit var back_img:ImageView
    lateinit var place:TextView
    lateinit var date:TextView
    lateinit var searchLayout:LinearLayout
    lateinit var deviceLay:LinearLayout
    lateinit var dateLay:LinearLayout
    lateinit var filterButton:ImageView
    lateinit var refreshBtn:ImageView
    lateinit var topmerchantid:EditText
    lateinit var topmerchantname:EditText
    lateinit var statement:TextView
    lateinit var ts_help_image:ImageView
    lateinit var  tableLayout: TableLayout

    private val REQUEST_PERMISSION_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_statement)
        supportActionBar?.hide()
        downloadbtn = findViewById(R.id.downloadbtn)
        topmerchantid=findViewById(R.id.topmerchantid)
        topmerchantname=findViewById(R.id.topmerchantname)
        ts_help_image = findViewById(R.id.ts_help_image)
        ts_help_image.setOnClickListener(){ HelpInfo.getInfo(this,"34") }
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        accType = findViewById(R.id.accType)
        address1 = findViewById(R.id.address1)
        address1.text = merchantdata?.merchant_name.toString()
        address2 = findViewById(R.id.address2)
        address3 = findViewById(R.id.address3)
        address4 = findViewById(R.id.address4)
        accStatementDate = findViewById(R.id.accStatementDate)
        accStatementDate.text = ("Account statement")
        searchLayout = findViewById(R.id.searchLayout)
        deviceLay = findViewById(R.id.deviceLay)
        dateLay = findViewById(R.id.dateLay)
        refreshBtn = findViewById(R.id.refreshBtn)
        filterButton = findViewById(R.id.filterButton)
        repId= findViewById(R.id.repId)
        repName= findViewById(R.id.repName)
        unitName= findViewById(R.id.unitName)
        unitId= findViewById(R.id.unitId)
        deviceId= findViewById(R.id.deviceId)
        accNumber = findViewById(R.id.accNumber)
        accCurrency = findViewById(R.id.accCurrency)
        searchBy = findViewById(R.id.searchBy)
        searchQuery = findViewById(R.id.searchQuery)
        stmtbutton = findViewById(R.id.stmtbutton)
        editfromDate = findViewById(R.id.fromdate)
        edittoDate = findViewById(R.id.toDate)
        place = findViewById(R.id.place)
        date = findViewById(R.id.datebottom)
        back_img = findViewById(R.id.back_img)
        statement = findViewById(R.id.statement)
        repId.setText(merchantdata?.merchant_rep_id!!)
        repName.setText(merchantdata?.mer_representative_name!!)
        topmerchantid.setText(merchantdata?.merchant_user_id!!)
        topmerchantname.setText(merchantdata?.merchant_name!!)
        accCurrency.setText("BWP")
        accNumber.setText("951098652638192")
        tableLayout = findViewById(R.id.StatetableLayout)
        val currentDate = LocalDate.now()
        val inputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val date1: LocalDate = LocalDate.parse(currentDate.toString(), inputFormat1) // Parse input date string
        val formattedDate2 = date1.format(outputFormat1)
        date.setText(formattedDate2)
        statement.setText("Statement generated on "+formattedDate2)
        filterButton.setOnClickListener(){ header(true) }
        refreshBtn.setOnClickListener(){
            stmtbutton.visibility = View.VISIBLE
            editfromDate.text.clear()
            edittoDate.text.clear()
            searchBy.text.clear()
            unitId.text.clear()
            unitName.text.clear()
            searchQuery.text.clear()
            dateLay.visibility = View.VISIBLE
        }
        back_img.setOnClickListener(){
            onBackPressed()
        }
        searchBy.setOnClickListener(){
            showSearchMenu(searchBy)
        }
        stmtbutton.setOnClickListener(){
           searchValidation()
        }
        editfromDate.setOnClickListener(){
            showDatePickerDialog(true)
        }
        edittoDate.setOnClickListener(){
            showDatePickerDialog(false)
        }
        downloadbtn.setOnClickListener(){
            onDownloadButtonClicked()
        }
    }
    private fun onDownloadButtonClicked() {
        val options = arrayOf("CSV format", "Excel","PDF")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Choose format to download file")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> createCSVFromTableLayout()
                    1 -> {
                        if (checkStoragePermissions()) {
                            createExcelSheetFromTableLayout()
                        }
                    }
                    2 -> {
                        checkAndRequestPermissions {
                            generatePDFFromView(findViewById(R.id.scrollView))
                        }
                    }
                }
            }
            .show()
    }
    private fun createCSVFromTableLayout() {
        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "External storage is not writable", Toast.LENGTH_SHORT).show()
            return
        }
        // Define the file name and path
        val fileName = "MerchantStatements_${Encryption.generatePID()}.csv"
        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/$fileName"
        val file = File(filePath)

        try {
            // Create file output stream
            val fileOutputStream = FileOutputStream(file)
            val writer = OutputStreamWriter(fileOutputStream)

            // Get the TableLayout
            val tableLayout: TableLayout = findViewById(R.id.StatetableLayout)

            // Define header labels based on searchBy criteria
            val headerLabels = when (searchBy.text.toString()) {
                "Unit" -> listOf(
                    "Date", "Message Ref", "Bill Number", "User Id", "Bank", "Currency", "Amount", "Status"
                )
                "All" -> listOf(
                    "Date", "Message Ref", "Bill Number", "Unit", "User Id", "Bank", "Currency", "Amount", "Status"
                )
                "User" -> listOf(
                    "Date", "Message Ref", "Bill Number", "Unit Id", "Bank", "Currency", "Amount", "Status"
                )
                "Device" -> listOf(
                    "Date", "Message Ref", "Bill Number", "Unit Id", "User Id", "Bank", "Currency", "Amount", "Status"
                )
                else -> listOf()
            }
            // Write the header row
            writer.write(headerLabels.joinToString(",") + "\n")
            // Iterate over TableLayout rows
            for (i in 0 until tableLayout.childCount) {
                val rowLayout = tableLayout.getChildAt(i) as? TableRow
                if (rowLayout != null) {
                    // Check if it's a data row and not a header
                    if (i == 0) {
                        continue // Skip the first row if it's a header
                    }
                    val rowData = mutableListOf<String>()
                    // Iterate over each cell in the row
                    for (j in 0 until rowLayout.childCount) {
                        val view = rowLayout.getChildAt(j)

                        if (view is TextView) {
                            // Add the text from TextView to the row data
                            rowData.add(view.text.toString())
                        } else {
                            // Handle non-TextView cases
                            rowData.add("N/A")
                        }
                    }
                    // Write the row data as a CSV line
                    writer.write(rowData.joinToString(",") + "\n")
                }
            }
            // Close the writer
            writer.flush()
            writer.close()

            Toast.makeText(this, "CSV file created at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
          //  e.printStackTrace()
            Toast.makeText(this, "Failed to create CSV file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkStoragePermissions(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
            false
        } else {
            true
        }
    }
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    private fun createExcelSheetFromTableLayout() {
        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "External storage is not writable", Toast.LENGTH_SHORT).show()
            return
        }
        // Create a new workbook and sheet
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("Merchant Statements")

        // Get the TableLayout
        val tableLayout: TableLayout = findViewById(R.id.StatetableLayout)

        // Create header row in Excel
        val headerRow = sheet.createRow(0)
        // Define header labels based on searchBy criteria
        val headerLabels = when (searchBy.text.toString()) {
            "Unit" -> listOf(
                "Date", "Message Ref", "Bill Number", "User Id", "Bank", "Currency", "Amount", "Status"
            )
            "All" -> listOf(
                "Date", "Message Ref", "Bill Number", "Unit", "User Id", "Bank", "Currency", "Amount", "Status"
            )
            "User" -> listOf(
                "Date", "Message Ref", "Bill Number", "Unit Id", "Bank", "Currency", "Amount", "Status"
            )
            "Device" -> listOf(
                "Date", "Message Ref", "Bill Number", "Unit Id", "User Id", "Bank", "Currency", "Amount", "Status"
            )
            else -> listOf()
        }
        // Add header labels to the first row of Excel sheet
        headerLabels.forEachIndexed { index, label ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(label)
        }
        // Start from the second row (data starts from row 1 in Excel)
        var dataRowIndex = 1
        // Iterate over TableLayout rows (children)
        for (i in 0 until tableLayout.childCount) {
            val rowLayout = tableLayout.getChildAt(i) as? TableRow
            if (rowLayout != null) {
                // Check if this is a data row and not the header row
                if (i == 0) {
                    // Skip the first row if it's a header in the TableLayout
                    continue
                }

                // Create a new row in the Excel sheet for data
                val row: Row = sheet.createRow(dataRowIndex++)

                // Iterate over TableRow children (TextViews)
                for (j in 0 until rowLayout.childCount) {
                    val cell: Cell = row.createCell(j)
                    val view = rowLayout.getChildAt(j)

                    if (view is TextView) {
                        // Add the text value from the TableRow into the Excel cell
                        cell.setCellValue(view.text.toString())
                    } else {
                        // Handle non-TextView cases
                        cell.setCellValue("N/A")
                    }
                }
            }
        }
        // Define the file name and path
        val fileName = "MerchantStatements_${Encryption.generatePID()}.xlsx"
        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/$fileName"
        val file = File(filePath)

        // Write the workbook to the file
        try {
            file.parentFile?.mkdirs() // Ensure directory exists
            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()

            Toast.makeText(this, "Excel sheet created at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
           // e.printStackTrace()
            Toast.makeText(this, "Failed to create Excel sheet: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    private fun checkAndRequestPermissions(onPermissionsGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE)
        } else {
            onPermissionsGranted()
        }
    }
    private fun generatePDFFromView(view: ScrollView) {
        // Check for write permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            return
        }
        // Create a bitmap of the entire content of the ScrollView
        val bitmap = getBitmapFromView(view)
        if (bitmap == null) {
            Toast.makeText(this, "Failed to create bitmap from view", Toast.LENGTH_LONG).show()
            return
        }
        // Define padding
        val padding = 60 // adjust the padding as needed (in pixels)
        // Create a new PdfDocument
        val document = PdfDocument()
        // Calculate page dimensions
        val pageHeight = view.height
        val pageWidth = view.width
        // Calculate number of pages needed
        val totalHeight = bitmap.height
        val totalPages = Math.ceil(totalHeight / pageHeight.toDouble()).toInt()
        var currentPage = 0
        while (currentPage < totalPages) {
            // Create a page info with the view width and view height
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, view.height, currentPage + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val srcRect = Rect(0, currentPage * pageHeight, bitmap.width, Math.min((currentPage + 1) * pageHeight, totalHeight))
            val dstRect = Rect(0, 0, pageWidth, srcRect.height())
            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
            document.finishPage(page)
            currentPage++
        }
        savePdfToMediaStore(document)
    }
    private fun savePdfToMediaStore(document: PdfDocument) {
        // Define file name and MIME type
        val fileName = Encryption.generatePID()
        val mimeType = "application/pdf"
        // Define ContentValues for file metadata
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType)
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }
        // Insert the file metadata into MediaStore
        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
        uri?.let {
            try {
                // Open output stream to the file URI
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    document.writeTo(outputStream)
                   SuccesssDialog.showImageAlertDialog(this,"PDF Generated Successfully.",MainActivity::class.java)
                }
            } catch (e: IOException) {
                SuccesssDialog.showImageAlertDialog(this,"Failed to generate PDF: ${e.message}",MainActivity::class.java)
            } finally {
                // Close the document
                document.close()
            }
        } ?: run {
            SuccesssDialog.showImageAlertDialog(this,"Failed to get file URI.",MainActivity::class.java)
        }
    }
    private fun getBitmapFromView(view: ScrollView): Bitmap? {
        // Measure and layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Create a bitmap
        val bitmap = Bitmap.createBitmap(view.width, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    // Function to show a response dialog (for user feedback)
    private fun searchValidation() {
        if(searchBy.text.toString().isEmpty()){
            showResponseDialog("Please choose search by")
        }
        if(searchBy.text.toString()=="Unit"){
            dateCheck()
        }
        else if(searchBy.text.toString()=="User"){
            if(searchQuery.text.toString().isEmpty()){
                searchQuery.error = "Please enter user id"
            }
            else{
                dateCheck()
            }
        }
        else if(searchBy.text.toString()=="Device"){
            if(searchQuery.text.toString().isEmpty()){
                searchQuery.error = "Please enter device id"
            }
            else{
                dateCheck()
            }
        }
        else if(searchBy.text.toString()=="All"){
            dateCheck()
        }
    }
    fun dateCheck(){
        if(editfromDate.text.toString().isEmpty() && edittoDate.text.toString().isEmpty()){
            showResponseDialog("Please choose from date and to date")
        }
        else{
            stmtbutton.visibility = View.GONE
            tableLayout.removeAllViews()
               header(false)
        }
    }
    private fun header(showFilters: Boolean = false) {
        val tableLayout: TableLayout = findViewById(R.id.StatetableLayout)
        tableLayout.removeAllViews() // Clear existing headers if any
        val headerRow = TableRow(applicationContext)
        headerRow.setBackgroundColor(resources.getColor(R.color.liteOrange)) // Replace with the actual color resource
        // Define layout parameters with specific widths
        val headerParams = mapOf(
            "date" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f),
            "msgRef" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.7f),
            "userId" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f),
            "bank" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f),
            "currency" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f),
            "amount" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f),
            "status" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f),
            "unit" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.8f),
            "bill" to TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        )
        // Create TextViews for each header column
        val headersAndParams = when (searchBy.text.toString()) {
            "Unit" -> listOf(
                "Date" to headerParams["date"],
                "Message Ref" to headerParams["msgRef"],
                "Bill Number" to headerParams["bill"],
                "User Id" to headerParams["userId"],
                "Bank" to headerParams["bank"],
                "Currency" to headerParams["currency"],
                "Amount" to headerParams["amount"],
                "Status" to headerParams["status"]
            )
            "All" -> listOf(
                "Date" to headerParams["date"],
                "Message Ref" to headerParams["msgRef"],
                "Bill Number" to headerParams["bill"],
                "Unit" to headerParams["unit"],
                "User Id" to headerParams["userId"],
                "Bank" to headerParams["bank"],
                "Currency" to headerParams["currency"],
                "Amount" to headerParams["amount"],
                "Status" to headerParams["status"]
            )
            "User" -> listOf(
                "Date" to headerParams["date"],
                "Message Ref" to headerParams["msgRef"],
                "Bill Number" to headerParams["bill"],
                "Unit Id" to headerParams["unit"],
                "Bank" to headerParams["bank"],
                "Currency" to headerParams["currency"],
                "Amount" to headerParams["amount"],
                "Status" to headerParams["status"]
            )
            "Device" -> listOf(
                "Date" to headerParams["date"],
                "Message Ref" to headerParams["msgRef"],
                "Bill Number" to headerParams["bill"],
                "Unit Id" to headerParams["unit"],
                "User Id" to headerParams["userId"],
                "Bank" to headerParams["bank"],
                "Currency" to headerParams["currency"],
                "Amount" to headerParams["amount"],
                "Status" to headerParams["status"]
            )
            else -> emptyList()
        }
        for ((header, params) in headersAndParams) {
            val linearLayout = LinearLayout(applicationContext).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = params
            }

            val textView = createTextView(header).apply {
                setTypeface(null, Typeface.BOLD)
                textSize = 15f
            }

            linearLayout.addView(textView)
            if (showFilters) {
                val filterIcon = ImageView(applicationContext).apply {
                    setImageResource(R.drawable.ic_filter) // Replace with your filter icon
                    setPadding(0, 0, 0, 0)
                    setOnClickListener {
                        showFilterDialog(header)
                    }
                }
                linearLayout.addView(filterIcon)
            }
            headerRow.addView(linearLayout)
        }
        tableLayout.addView(headerRow)
      /*  when (searchBy.text.toString()) {
            "Unit" -> fetchUnitStatement()
            "All" -> fetchMerchantStatement()
            "User" -> fetchUserStatement()
            "Device" -> fetchDeviceStatement()
        }*/
        fetchMerchantStatement()
    }
    // Show filter dialog for the specified column
    private fun showFilterDialog(column: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Filter by $column")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            val query = input.text.toString()
            filterTable(column, query)
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }
    // Function to filter table rows based on the input in the filter dialog for a specific column
    private fun filterTable(column: String, query: String) {
        val tableLayout: TableLayout = findViewById(R.id.StatetableLayout)
        // Skip the first row (header row)
        for (i in 1 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as TableRow
            var matches = false
            val columnIndex = when (column) {
                "Date" -> 0
                "Message Ref" -> 1
                "Bill Number" -> 2
                "Unit Id" -> 3
                "User Id" -> 4
                "Device Id" -> 5
                "Bank" -> 6
                "Currency" -> 7
                "Amount" -> 8
                "Status" -> 9
                else -> -1
            }

            if (columnIndex != -1) {
                val cellText = (row.getChildAt(columnIndex) as TextView).text.toString().toLowerCase(Locale.getDefault())
                if (cellText.contains(query.toLowerCase(Locale.getDefault()))) {
                    matches = true
                }
            }
            row.visibility = if (matches) View.VISIBLE else View.GONE
        }
    }
    private fun showSearchMenu(anchor: EditText) {
        stmtbutton.visibility = View.VISIBLE
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.trans_statement_menu, popupMenu.menu)
        if(merchantdata?.pwlog_flg=="MERCHANT"){
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.item1 -> {
                        // Handle selection
                        anchor.setText("All")
                        searchQuery.setText(merchantdata!!.merchant_user_id)
                        true
                    }
                    R.id.item2 -> {
                        // Handle selection
                        anchor.setText("Unit")
                        true
                    }
                    R.id.item3 -> {
                        // Handle selection
                        anchor.setText("User")
                        searchQuery.text.clear()
                        true
                    }
                    R.id.item4 -> {
                        // Handle selection
                        anchor.setText("Device")
                        searchQuery.text.clear()
                        true
                    }
                    // Add more items as needed
                    else -> false
                }
            }
        }
        else{
            popupMenu.menu.removeItem(R.id.item1)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.item2 -> {
                        // Handle selection
                        anchor.setText("Unit")
                        searchQuery.setText(merchantdata!!.unit_id.toString())
                        true
                    }
                    R.id.item3 -> {
                        // Handle selection
                        anchor.setText("User")
                        searchQuery.text.clear()
                        true
                    }
                    R.id.item4 -> {
                        // Handle selection
                        anchor.setText("Device")
                        searchQuery.text.clear()
                        true
                    }
                    // Add more items as needed
                    else -> false
                }
            }
        }

        popupMenu.show()
    }
    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        return true
    }
    private fun createTextView(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(this, R.color.black))
        textView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        textView.setPadding(8, 8, 8, 8)
        textView.gravity = android.view.Gravity.START
        textView.background = ContextCompat.getDrawable(this, R.drawable.box_background)
        return textView
    }
    private fun createAmountTextView(amount: Double): TextView {
        // Format the amount to ensure it always has two decimal places
        val formattedAmount = String.format("%.2f", amount)
        val textView = TextView(this).apply {
            text = formattedAmount
            setTextColor(ContextCompat.getColor(this@TransactionStatement, R.color.black))
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(8, 8, 10, 8)
            gravity = android.view.Gravity.RIGHT
            background = ContextCompat.getDrawable(this@TransactionStatement, R.drawable.box_background)
        }

        return textView
    }
    private fun updateFromDate(date: Calendar) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        editfromDate.setText(dateFormat.format(date.time))

        val dateFormat1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate1 = dateFormat1.format(date.time)
        FromDate = parsedDate1


    //    println(parsedDate1)

    }
    private fun updateToDate(date: Calendar) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        edittoDate.setText(dateFormat.format(date.time))

        val dateFormat1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate1 = dateFormat1.format(date.time)
        ToDate = parsedDate1
        accStatementDate.text = ("Account statement for the period from "+ editfromDate.text.toString()+" to " + edittoDate.text.toString())
      //  println(parsedDate1)
    }
    private fun showDatePickerDialog(isFromDate: Boolean) {
        val currentDate = if (isFromDate) fromDate else toDate
        val today = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                currentDate.set(year, monthOfYear, dayOfMonth)
                if (isFromDate) {
                    updateFromDate(currentDate)
                } else {
                    updateToDate(currentDate)
                }
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )

        // Restrict future dates
        datePickerDialog.datePicker.maxDate = today.timeInMillis

        if (!isFromDate) {
            // Set min date for toDate as one day after fromDate
            datePickerDialog.datePicker.minDate = fromDate.timeInMillis // 86400000 milliseconds = 1 day
        }

        datePickerDialog.show()
    }
    private fun showResponseDialog(response:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert!")
        builder.setMessage(response)
        builder.setNegativeButton("OK") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }
    private fun fetchMerchantStatement() {
        dateLay.visibility = View.GONE
        downloadbtn.visibility= View.VISIBLE
        filterButton.visibility = View.VISIBLE
        val currentDate = Date()
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val formattedDate = formatter.format(currentDate)
      //  println("Formatted Date: $formattedDate")
        val call: Call<List<List<Any>>> = when (searchBy.text.toString()) {
            "Unit" -> ApiClient.apiService.getStatementUnitWise(merchantdata?.merchant_user_id.toString(),editfromDate.text.toString(),edittoDate.text.toString(),searchQuery.text.toString())
            "All" ->  ApiClient.apiService.getStatementMerchantWise(merchantdata?.merchant_user_id.toString(), editfromDate.text.toString(), edittoDate.text.toString())
            "User" -> ApiClient.apiService.getStatementUserWise(merchantdata?.merchant_user_id.toString(),editfromDate.text.toString(),edittoDate.text.toString(),unitId.text.toString(),searchQuery.text.toString())
            "Device" -> ApiClient.apiService.getStatementDeviceWise(merchantdata?.merchant_user_id.toString(),editfromDate.text.toString(),edittoDate.text.toString(),unitId.text.toString(),searchQuery.text.toString())
            else -> {
                return
            }
        }
        call.enqueue(object : Callback<List<List<Any>>> {
            override fun onResponse(
                call: Call<List<List<Any>>>,
                response: Response<List<List<Any>>>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()!!
                  //  print(responseData)
                    val mappedData = responseData.map { data ->
                        StatemenResponse(
                            placeData = data.getOrNull(0)?.toString(),
                            stateData = data.getOrNull(1)?.toString(),
                            phNumData = data.getOrNull(2)?.toString(),
                            tranCurrencyData = data.getOrNull(3)?.toString(),
                            tranDate = data.getOrNull(4)?.toString(),
                            sequenceUniqueId = data.getOrNull(5)?.toString(),
                            merchantBillNumber = data.getOrNull(6)?.toString(),
                            unitId = data.getOrNull(7)?.toString(),
                            userId = data.getOrNull(8)?.toString(),
                            deviceId = data.getOrNull(9)?.toString(),
                            bankNameData = data.getOrNull(10)?.toString(),
                            tranAmount = data.getOrNull(11)?.toString(),
                            tranStatus = data.getOrNull(12)?.toString()
                        )
                    }
                    val sortedData = mappedData.sortedByDescending { it.tranDate ?: "" }
                    populateTable(sortedData)
                } else {
                //    println("Error: ${response.code()}")
                    Toast.makeText(this@TransactionStatement, "Invalid credentials", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<List<Any>>>, t: Throwable) {
                NetworkUtils.hideProgress(this@TransactionStatement)
               // t.printStackTrace()
                Toast.makeText(this@TransactionStatement, R.string.Failed, Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun populateTable(data: List<StatemenResponse>) {
        data.forEach { data ->
            val row = TableRow(this@TransactionStatement)
            val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            row.layoutParams = params
            val timestamp1 = data.tranDate
            val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            val dateTime1 = LocalDateTime.parse(timestamp1, formatter1).toLocalDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val tranDateText = createTextView(dateTime1)
            val msgRefText = createTextView(data.sequenceUniqueId ?: "N/A")
            val billText = createTextView(data.merchantBillNumber ?: "N/A")
            val unitText = createTextView(data.unitId ?: "N/A")
            val userText = createTextView(data.userId ?: "N/A")
            val deviceText = createTextView(data.deviceId ?: "N/A")
            val bankText = createTextView(data.bankNameData ?: "N/A")
            val currencyText = createTextView(data.tranCurrencyData ?: "N/A")
            val amountText = createAmountTextView(data.tranAmount!!.toDouble())
            val statusText = createTextView(data.tranStatus ?: "N/A")

            val dateParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            val msgrefParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.7f)
            val bankParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            val currencyParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            val amtParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            val statusParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            val useridParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            val unitParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.8f)
            val deviceParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f)
            val billParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            // Set layout parameters to TextViews
            tranDateText.layoutParams = dateParams
            msgRefText.layoutParams = msgrefParams
            bankText.layoutParams = bankParams
            currencyText.layoutParams = currencyParams
            amountText.layoutParams = amtParams
            statusText.layoutParams = statusParams
            userText.layoutParams = useridParams
            unitText.layoutParams = unitParams
            billText.layoutParams = billParams
            deviceText.layoutParams = deviceParams

            row.addView(tranDateText)
            row.addView(msgRefText)
            row.addView(billText)
            if(searchBy.text.toString()!="Unit")
            {
                row.addView(unitText)
            }
            if(searchBy.text.toString()!="User")
            {
                row.addView(userText)
            }
            row.addView(bankText)
            row.addView(currencyText)
            row.addView(amountText)
            row.addView(statusText)
            tableLayout.addView(row)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Write permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Write permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}