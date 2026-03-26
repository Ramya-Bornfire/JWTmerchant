package com.bornfire.merchantqrcode

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.PosterList
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PosterScreen : BaseActivity() {
    lateinit var posterList: List<PosterList>

    lateinit var backBtn: ImageView
    val merchantdata = SharedMerchantDataObj.merchantData
    lateinit var nodata:TextView

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poster_screen)
        nodata=findViewById(R.id.emptyTextView)
        getSupportActionBar()?.hide()
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(resources.getColor(R.color.liteOrange)) // Replace with the actual color resource
        val tableLayout: TableLayout = findViewById(R.id.posterTableLayout)
        // Create TextViews for each header column
        val headers =
            arrayOf("Poster Id", "Merchant Id", "Merchant Rep Id", "Unit Id", "Poster Date")
        for (header in headers) {
            val textView = createTextView(header)
            textView.setTypeface(null, android.graphics.Typeface.BOLD)
            textView.textSize = 15f
            headerRow.addView(textView)
        }
        // Add the header row to the table layout
        tableLayout.addView(headerRow)
        fetchData()
        val addBtn = findViewById<Button>(R.id.addBtn)
        addBtn.setOnClickListener() {
            val intent = Intent(this, PosterAddScreen::class.java)
            intent.putExtra("Button", "Submit")
            startActivity(intent)
        }

    }
    private fun fetchData() {
        //NetworkUtils.showProgress(this)
        val call: Call<List<PosterList>> =
            ApiClient.apiService.getPosterlist(merchantId = "MER0101")
        // Enqueue the call
        call.enqueue(object : Callback<List<PosterList>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<PosterList>>,
                response: Response<List<PosterList>>
            ) {
                if (response.isSuccessful) {
                    // Handle the successful response
                    posterList = response.body()!!
                    if(posterList.isNotEmpty()){
                        nodata.visibility= View.INVISIBLE
                    }
                    posterList?.forEach { posterList ->
                        val tableLayout: TableLayout = findViewById(R.id.posterTableLayout)
                        val row = TableRow(this@PosterScreen)
                        val params = TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                        )
                        row.layoutParams = params

                        val posterId = createTextView(posterList.poster_id)
                        val merId = createTextView(posterList.merchant_id)
                        val merRepId = createTextView(posterList.merchant_rep_id)
                        val unitId = createTextView(posterList.unit_id)

                        val timestamp1 = posterList.poster_date
                        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        val dateTime1 =
                            LocalDateTime.parse(timestamp1, formatter1).toLocalDate().format(
                                DateTimeFormatter.ofPattern("dd-MM-yyyy")
                            )
                        val posterdata = createTextView(dateTime1)

                        row.addView(posterId)
                        row.addView(merId)
                        row.addView(merRepId)
                        row.addView(unitId)
                        row.addView(posterdata)

                        tableLayout.addView(row)
                        NetworkUtils.hideProgress(this@PosterScreen)

                        row.setOnClickListener() {
                            val intent = Intent(applicationContext, PosterAddScreen::class.java)
                            intent.putExtra("Button", "Y")
                            intent.putExtra("MerchantId", posterList.merchant_id)
                            intent.putExtra("MerchantRepid", posterList.merchant_rep_id)
                            intent.putExtra("PosterId", posterList.poster_id)
                            intent.putExtra("UnitId", posterList.unit_id)
                            intent.putExtra("PosterDate", posterList.poster_date)
                            intent.putExtra("Button", "Update")
                            intent.putExtra("Media",posterList.file_name)
                            startActivity(intent)
                        }
                    }
                    // Update UI or log the data
                } else {
                    NetworkUtils.hideProgress(this@PosterScreen)
                }
            }

            override fun onFailure(call: Call<List<PosterList>>, t: Throwable) {
              //  t.printStackTrace()
                NetworkUtils.hideProgress(this@PosterScreen)
                Toast.makeText(this@PosterScreen,"Something Went Wrong at Server End", Toast.LENGTH_LONG).show()

                // Handle network errors or failures
                // You may want to log the error or display a message to the user
            }
        })
    }
    private fun createTextView(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        textView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        textView.setPadding(8, 8, 8, 8)
        textView.gravity = android.view.Gravity.START
        return textView
    }
    fun fileToImageView(context: Context, file: File): ImageView {
        val imageView = ImageView(context)
        // Decode the file into a Bitmap
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        // Set the Bitmap to the ImageView
        imageView.setImageBitmap(bitmap)
        return imageView
    }

}