package com.bornfire.merchantqrcode


import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.Utils.HelpInfo
import java.io.ByteArrayOutputStream

class StaticQRActivity : BaseActivity() {
    private lateinit var shareBtn: Button
    private lateinit var downloadBtn: Button
    private lateinit var qrCodeImageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staticqractivity)
        val isTablet =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.stc_qr)
        if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.stc_qr)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener {
                HelpInfo.getInfo(this, "1")
            }
        }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val baseString = intent.getStringExtra("Base64String")
        shareBtn = findViewById(R.id.sharebtn)
        downloadBtn = findViewById(R.id.downloadbtn)
        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        shareBtn.setOnClickListener {
            shareQRCode()
        }
        downloadBtn.setOnClickListener {
            downloadQRCode()
        }
        val decodedBytes = Base64.decode(baseString, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        qrCodeImageView.setImageBitmap(bitmap)

    }

    private fun downloadQRCode() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                123
            )
        } else {
            downloadQR()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                downloadQR()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadQR() {
        try {
            val qrImage = (qrCodeImageView.drawable as BitmapDrawable).bitmap
            val bytes = ByteArrayOutputStream()
            qrImage.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val displayName = "MerchantQR_${sdf.format(Date())}.jpg"
            val mimeType = "image/jpeg"
            val relativePath = Environment.DIRECTORY_PICTURES + "/merchantQR"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            }
            val contentResolver = applicationContext.contentResolver
            val imageUri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            imageUri?.let { uri ->
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes.toByteArray())
                    outputStream.flush()
                }
                MediaScannerConnection.scanFile(
                    this,
                    arrayOf(uri.toString()),
                    arrayOf(mimeType)
                ) { _, _ -> }
                Toast.makeText(this, "QR code downloaded successfully", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Failed to create new MediaStore record.", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareQRCode() {
        if (qrCodeImageView.drawable == null) {
            Toast.makeText(
                this,
                "QR Code not found",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val qrImage = ((qrCodeImageView.drawable as BitmapDrawable).bitmap)
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "image/jpeg"

            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "Merchant QR Code Pay")
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            val uri = this.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            val outStream: OutputStream
            try {
                outStream = this.contentResolver?.openOutputStream(uri!!)!!
                qrImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                outStream.close()
            } catch (e: Exception) {
               // e.printStackTrace()
            }
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(intent, "Share PayConnect QR"))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}