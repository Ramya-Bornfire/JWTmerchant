package com.bornfire.merchantqrcode

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.core.app.ActivityCompat
import com.bornfire.merchantqrcode.DataModel.ScanQRResponse
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanQRActivity : BaseActivity() {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 2
    private lateinit var barcodeScannerView: CompoundBarcodeView
    var qr = ""
    private lateinit var uploadQR: Button
    var isScanning = false
    private var lastScannedQR: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qractivity)
        uploadQR = findViewById(R.id.uploadqr)

        barcodeScannerView = findViewById(R.id.barcode_scanner)

        if (isCameraPermissionGranted()) {
            setupBarcodeScanner()
        } else {
            requestCameraPermission()
        }
        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.sc_qr)
       if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            // Set the title
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.sc_qr)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener{
                HelpInfo.getInfo(this,"3")
            }
        }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        uploadQR.setOnClickListener {

                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
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
                    openGallery()
                }

        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"  // Set the type to image
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        } catch (e: Exception) {
        //    e.printStackTrace()
            showToast("Failed to open gallery.")
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                try {
                    // Convert the selected image URI to a Bitmap
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    // Call the QR code scanning function
                    scanQRCodeFromBitmap(bitmap)
                } catch (e: Exception) {
                 //   e.printStackTrace()
                    showToast("Failed to load image.")
                }
            } else {
                showToast("No image selected.")
            }
        }
    }


    private fun scanQRCodeFromBitmap(bitmap: Bitmap) {
        // Convert Bitmap to IntArray
        val width = bitmap.width
        val height = bitmap.height
        val intArray = IntArray(width * height)
        bitmap.getPixels(intArray, 0, width, 0, 0, width, height)

        // Create RGBLuminanceSource
        val source = RGBLuminanceSource(width, height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        // Decode the QR code
        try {
            val reader = MultiFormatReader()
            val hints = mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE))
            val result = reader.decode(binaryBitmap, hints)
            handleQRCodeContent(result.text)
        } catch (e: Exception) {
          //  e.printStackTrace()
            showToast("Failed to decode QR code.")
        }
    }

    private fun handleQRCodeContent(qrCodeContent: String) {
        if (isScanning) return  // Prevent multiple scans
        isScanning = true  // Set flag to true to prevent further scanning
        qr = qrCodeContent
        getQRContent(qrCodeContent)
      //  println(qrCodeContent)
    }


    private fun getQRContent(qrCodeContent: String) {
        val pID = Encryption.generatePID()
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val psuIpAddress = Encryption.getPSUIPADDRESS()
        val psuId = Encryption.generatePSUID(this)
        val isTablet = isTablet(this)
        val psuChannel = if (isTablet) {
            "Tablet"
        } else {
            "Mobile"
        }

        val call = ApiClient.apiService.scanQR(pID, psuDeviceID, psuIpAddress, psuId, psuChannel, qrCodeContent)
        call.enqueue(object : Callback<ScanQRResponse> {
            override fun onResponse(call: Call<ScanQRResponse>, response: Response<ScanQRResponse>) {
                isScanning = false  // Allow scanning again
                if (response.isSuccessful) {
                    val merchantResponse = response.body()
                    val customerID = merchantResponse?.payeeAccountInformation?.customerID ?: ""
                    val customerName = merchantResponse?.customerName ?: ""
                    val deviceId = merchantResponse?.additionalDataInformation?.deviceID ?: ""
                    val referenceNumber = merchantResponse?.additionalDataInformation?.referenceNumber ?: ""
                    val purpose = merchantResponse?.additionalDataInformation?.purposeOfTransaction ?: ""
                    val city = merchantResponse?.city ?: ""
                    gotoInitiateScreen(customerID, customerName, deviceId, referenceNumber, purpose, city)
                } else {
                    showToast("Error: ${response.message()}")
                   // println("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ScanQRResponse>, t: Throwable) {
                isScanning = false  // Allow scanning again
              //  println("Network error: ${t.message}")
                showToast("Something Went Wrong at Server End")
            }
        })
    }

    private fun gotoInitiateScreen(
        customerId: String,
        customerName: String,
        customerDeviceId:String,
        customerReferenceNumber: String,
        purpose:String,
        city:String,
    ) {
        val intent = Intent(this,InitiatePayment()::class.java)
        val message = qr
        intent.putExtra(InitiatePayment.EXTRA_MESSAGE, message)
        intent.putExtra("customerId",customerId)
        intent.putExtra("customerName",customerName)
        intent.putExtra("customerDeviceId",customerDeviceId)
        intent.putExtra("customerReferenceNumber",customerReferenceNumber)
        intent.putExtra("Purpose",purpose)
        intent.putExtra("City",city)

        startActivity(intent)
    }

    private fun isCameraPermissionGranted(): Boolean {
        return checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupBarcodeScanner()
            } else {
                showToast("Camera permission is required to use this feature")
            }
        }
    }
    private fun setupBarcodeScanner() {
        val formats = listOf(BarcodeFormat.QR_CODE)
        val hints = mapOf(DecodeHintType.POSSIBLE_FORMATS to formats)
        barcodeScannerView.barcodeView.decoderFactory = DefaultDecoderFactory(formats, hints, null, 0)

        barcodeScannerView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                val qrContent = result.text

                // If the content is the same as the last scanned content, ignore it
                if (qrContent == lastScannedQR) {
                    return
                }

                // Process the QR code and store the content to avoid rescanning the same code
                handleQRCodeContent(qrContent)
                lastScannedQR = qrContent

                // Optional: Reset the last scanned QR after some time (e.g., 5 seconds) to allow rescanning
                barcodeScannerView.postDelayed({
                    lastScannedQR = null
                }, 5000) // Adjust delay as needed
            }

            override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>) {
                // Handle possible result points if needed
            }
        })
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun isTablet(context: Context): Boolean {
        return (context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    override fun onResume() {
        super.onResume()
        barcodeScannerView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeScannerView.pause()
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}