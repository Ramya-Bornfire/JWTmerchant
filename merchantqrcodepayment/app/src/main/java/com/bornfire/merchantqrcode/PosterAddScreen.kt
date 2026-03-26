package com.bornfire.merchantqrcode

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.retrofit.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PosterAddScreen : BaseActivity() {
    lateinit var uploadImg:LinearLayout
    private lateinit var frameLayout: FrameLayout
    private lateinit var imageView: ImageView
    private lateinit var videoView: VideoView
    val merchantdata = SharedMerchantDataObj.merchantData
    lateinit var merId:EditText
    lateinit var merRepId:EditText
    lateinit var unitId:EditText
    lateinit var unitName:EditText
    lateinit var posterId:EditText
    lateinit var posterDate:EditText
    lateinit var uploadPoster:Button
    private var mediaPlayer: MediaPlayer? = null
    lateinit var disFromDate:EditText
    lateinit var disToDate:EditText
    lateinit var select:EditText
    private var fromDate: Calendar = Calendar.getInstance()
    private var toDate: Calendar = Calendar.getInstance()
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2
    private val REQUEST_PICK_VIDEO = 3
    var isImage :Boolean = true
    lateinit var videoURI: Uri


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poster_add_screen)
        select = findViewById(R.id.select)

        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        disFromDate= findViewById(R.id.fromdate)
        disToDate= findViewById(R.id.toDate)
        disFromDate.setOnClickListener(){
            showDatePickerDialog(true)
        }
        disToDate.setOnClickListener(){
            showDatePickerDialog(false)
        }
        supportActionBar?.title = "Poster Add Screen"
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        val selectEditText: EditText = findViewById(R.id.select)

        selectEditText.setOnClickListener {
            showPopupMenu(selectEditText)
        }

        uploadImg = findViewById(R.id.uploadImg)
        frameLayout = findViewById(R.id.frameLayout)
        imageView = findViewById(R.id.imageView)
        videoView = findViewById(R.id.videoView)
        merId = findViewById(R.id.merId)
        merRepId = findViewById(R.id.merRepId)
        unitId = findViewById(R.id.unitId)
        posterId = findViewById(R.id.posterId)
        posterDate = findViewById(R.id.posterDate)
        uploadPoster = findViewById(R.id.uploadPoster)

        mediaPlayer = MediaPlayer.create(this, R.raw.my_sound)
        merId.setText(merchantdata!!.merchant_user_id)
        merRepId.setText(merchantdata!!.merchant_rep_id)
        unitId.setText(merchantdata!!.unit_id)

        val buttonName = intent.getStringExtra("Button")
        if(buttonName=="Submit") {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val formattedDate = currentDate.format(formatter)
            posterDate.setText(formattedDate.toString())
            uploadPoster.setText(buttonName)
            uploadPoster.setOnClickListener() {
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        if(isImage==true){
                            uploadImage(this,imageView,"Salem","101","MER0101","MER0101R01","5 sec")
                        }
                        else{
                            uploadVideo(this,videoView,"Salem","101","MER0101","MER0101R01","10")
                        }

                    } else {
                        NoInternetAlert()
                    }
            }
        }
        else {
            val base64String = intent.getStringExtra("imageData")
            val bitmap = decodeBase64ToBitmap(base64String!!)
            if (bitmap != null) {
                setBitmapToImageView(bitmap, imageView)
            } else {
                // Handle the case where decoding failed
            }

            //posterImg.setImageResource(R.drawable.img4)
            uploadPoster.visibility = View.GONE
            val merchant_id = intent.getStringExtra("MerchantId")
            val mer_rep_id = intent.getStringExtra("MerchantRepid")
            val poster_id = intent.getStringExtra("PosterId")
            val unit_id = intent.getStringExtra("UnitId")
            val poster_date = intent.getStringExtra("PosterDate")
            val media = intent.getStringExtra("Media")

            if (media!!.endsWith(".jpg") || media.endsWith(".png")) {
                imageView.setImageResource(media.toInt())
            }
            else if (media.endsWith(".mp4") ||media.endsWith(".avi")) {
                videoView.setVideoURI(media.toUri())
            }
            val timestamp1 = poster_date
            val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            val dateTime1 = LocalDateTime.parse(timestamp1, formatter1).toLocalDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy"))

            merId.setText(merchant_id)
            merRepId.setText(mer_rep_id)
            unitId.setText(unit_id)
            posterId.setText(poster_id)
            posterDate.setText(dateTime1)
            select.setText("5 sec")
            disFromDate.setText("20-04-2024")
            disToDate.setText("15-05-2024")

        }
        uploadImg.setOnClickListener(){
            onChooseImageButtonClicked()

        }

    }
    private fun showDatePickerDialog(isFromDate: Boolean) {
        val currentDate = if (isFromDate) fromDate else toDate
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                currentDate.set(year, monthOfYear, dayOfMonth)
                if (isFromDate) {
                    updateFromDate()
                } else {
                    updateToDate()
                }
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )
        if(isFromDate){
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        }
        else  {
            // Set min date for toDate as one day after fromDate
            datePickerDialog.datePicker.minDate = fromDate.timeInMillis + 86400000 // 86400000 milliseconds = 1 day
        }
        datePickerDialog.show()
    }
    private fun updateFromDate() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        disFromDate.setText( dateFormat.format(fromDate.time).toString())
    }
    private fun updateToDate() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        disToDate.setText(dateFormat.format(toDate.time).toString())
    }
    private fun showPopupMenu(anchor: EditText) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.select_time, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.item1 -> {
                    // Handle selection
                    anchor.setText("5 sec")
                    true
                }
                R.id.item2 -> {
                    // Handle selection
                    anchor.setText("10 sec")
                    true
                }
                R.id.item3 -> {
                    // Handle selection
                    anchor.setText("30sec")
                    true
                }
                R.id.item4 -> {
                    // Handle selection
                    anchor.setText("1 minute")
                    true
                }
                R.id.item5 -> {
                    // Handle selection
                    anchor.setText("5 minutes")
                    true
                }
                R.id.item6 -> {
                    // Handle selection
                    anchor.setText("30 minutes")
                    true
                }
                R.id.item7 -> {
                    // Handle selection
                    anchor.setText("1 hour")
                    true
                }
                R.id.item8 -> {
                    // Handle selection
                    anchor.setText("2 hours")
                    true
                }
                // Add more items as needed
                else -> false
            }
        }
        popupMenu.show()
    }
    private fun NoInternetAlert() {
        val builder = android.app.AlertDialog.Builder(this,R.style.CustomAlertDialogStyle)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.activity_alert_dialog_box, null)
        // Set the custom view to the builder
        builder.setView(view)
        builder.setCancelable(false)
        // Set other properties of the AlertDialog (e.g., title, buttons, etc.)
        builder.setPositiveButton("OK") { dialog, which ->
            // Handle positive button click
            dialog.dismiss()
        }
        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun showImageAlertDialog(updateText:String) {
        val builder = android.app.AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.activity_success_dialog, null)
        val textView = view.findViewById<TextView>(R.id.textview)
        // Set the updateText to the TextView
        textView.text = updateText
        // Set the custom view to the builder
        builder.setView(view)
        playSound()
        // Set other properties of the AlertDialog (e.g., title, buttons, etc.)
        builder.setPositiveButton("OK") { dialog, which ->
            // Handle positive button click
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            dialog.dismiss()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun playSound() {
        mediaPlayer?.start()
    }
    private fun onChooseImageButtonClicked() {
        val options = arrayOf("Camera", "Gallery","Video")

        AlertDialog.Builder(this)
            .setTitle("Choose Image From")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePictureFromCamera()
                    1 -> choosePictureFromGallery()
                    2-> chooseVideoFromGallery()
                }
            }
            .show()
    }
    private fun chooseVideoFromGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_PICK_VIDEO)
    }
    private fun takePictureFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }
    private fun choosePictureFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    displayCameraImage(imageBitmap)
                }
                REQUEST_PICK_IMAGE -> {
                    val selectedImage = data?.data
                    selectedImage?.let { displayImage(it) }
                }
                REQUEST_PICK_VIDEO -> {
                    val selectedVideo = data?.data
                    selectedVideo?.let { displayVideo(it) }
                }
            }
        }
    }

    private fun displayImage(imageUri: Uri) {
        imageView.setImageURI(imageUri)
        imageView.visibility = View.VISIBLE
        videoView.visibility = View.GONE
    }
    private fun displayCameraImage(imageUri: Bitmap) {
        imageView.setImageBitmap(imageUri)
        imageView.visibility = View.VISIBLE
        videoView.visibility = View.GONE
    }
    private fun displayVideo(videoUri: Uri) {
        videoURI = videoUri
        videoView.setVideoURI(videoUri)
        videoView.start()
        imageView.visibility = View.GONE
        videoView.visibility = View.VISIBLE
        isImage = false
    }
    fun imageViewToFile(context: Context, imageView: ImageView): File? {
        // Enable drawing cache
        imageView.isDrawingCacheEnabled = true
        // Create a bitmap from the ImageView's drawing cache
        val bitmap = Bitmap.createBitmap(imageView.drawingCache)
        // Disable drawing cache to release resources
        imageView.isDrawingCacheEnabled = false

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image_${System.currentTimeMillis()}.jpg")

        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            return file
        } catch (e: IOException) {
         //   e.printStackTrace()
        }

        return null
    }
    fun uriToFile(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.let {
            val file = File(context.cacheDir, "${System.currentTimeMillis()}_${uri.lastPathSegment}")
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024) // Adjust buffer size as needed
                var read: Int
                while (it.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                return file
            }
        }
        return null
    }
    // Function to convert Bitmap to File
    fun bitmapToFile(context: Context, bitmap: Bitmap): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "IMG_${timeStamp}.jpg"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(fileName, ".jpg", storageDir)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
          //  e.printStackTrace()
        }
        return file
    }
    private fun getVideoThumbnail(videoUri: Uri): Bitmap? {
        return try {
            val resolver = contentResolver
            val bitmap = ThumbnailUtils.createVideoThumbnail(getRealPathFromURI(videoUri), MediaStore.Video.Thumbnails.MINI_KIND)
            bitmap
        } catch (e: Exception) {
          //  e.printStackTrace()
            null
        }
    }
    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val idx = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val path = cursor?.getString(idx!!)
        cursor?.close()
        return path ?: ""
    }
    override fun onSupportNavigateUp(): Boolean {
       finish()
        return true
    }
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_PICK_IMAGE = 2
    }
    fun uploadImage(context: Context, imageView: ImageView, unitName: String, unitId: String, merchantId: String, merchantRepId: String, frequency: String) {
        val imageFile = imageViewToFile(context, imageView)
        imageFile?.let { file ->
            val currentDate = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val currentDateFormatted = dateFormat.format(currentDate)

            val fromDate = currentDate
            val toDate = currentDate

            val unitNameRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), unitName)
            val unitIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), unitId)
            val merchantIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), merchantId)
            val merchantRepIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), merchantRepId)
            val frequencyRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), frequency)
            val dateRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), currentDateFormatted)

            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val call = ApiClient.apiService.uploadImage(body, unitNameRequestBody, dateRequestBody, unitIdRequestBody, merchantIdRequestBody, merchantRepIdRequestBody, frequencyRequestBody, dateRequestBody, dateRequestBody)

            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Toast.makeText(applicationContext,loginResponse.toString(),Toast.LENGTH_SHORT).show()
                        // Handle success response
                    } else {
                        // Handle failure response
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@PosterAddScreen,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()

                    // Handle network failure
                }
            })
        }
    }
    fun uploadVideo(context: Context, videoView: VideoView, unitName: String, unitId: String, merchantId: String, merchantRepId: String, frequency: String) {
        val videoUri = videoURI
        val videoFile = uriToFile(context, videoUri)
        videoFile?.let { file ->
            val currentDate = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val currentDateFormatted = dateFormat.format(currentDate)

            val fromDate = currentDate
            val toDate = currentDate

            val unitNameRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), unitName)
            val unitIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), unitId)
            val merchantIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), merchantId)
            val merchantRepIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), merchantRepId)
            val frequencyRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), frequency)
            val dateRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), currentDateFormatted)

            val requestFile = RequestBody.create("video/*".toMediaTypeOrNull(), file)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val call = ApiClient.apiService.uploadImage(body, unitNameRequestBody, dateRequestBody, unitIdRequestBody, merchantIdRequestBody, merchantRepIdRequestBody, frequencyRequestBody, dateRequestBody, dateRequestBody)

            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Toast.makeText(context, loginResponse.toString(), Toast.LENGTH_SHORT).show()
                        // Handle success response
                    } else {
                        // Handle failure response
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(context, "Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
                    // Handle network failure
                }
            })
        }
    }

    fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        // Remove the data type prefix from the Base64 string, if present
        val base64Image = base64String.substring(base64String.indexOf(",") + 1)

        // Decode the Base64 string into a byte array
        val imageByteArray = Base64.decode(base64Image, Base64.DEFAULT)

        // Convert the byte array to a Bitmap
        return BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
    }
    fun setBitmapToImageView(bitmap: Bitmap, imageView: ImageView) {
        imageView.setImageBitmap(bitmap)
    }
}

