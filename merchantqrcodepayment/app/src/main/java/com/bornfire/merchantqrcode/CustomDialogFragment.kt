package com.bornfire.merchantqrcode

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.print.PrintHelper
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Utils.NullCheck
import com.bornfire.merchantqrcode.retrofit.Encryption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class CustomDialogFragment : DialogFragment() {
    lateinit var merchantid: String
    lateinit var deviceid: String
    lateinit var transtatus: String
    lateinit var amount: String
    lateinit var referencelabel: String
    lateinit var tranId: String
    lateinit var tranDATE: String
    lateinit var merchantname: String
    lateinit var merchantaddress: String
    lateinit var merchantCity: String
    lateinit var terminalid: String
    lateinit var billid: String
    lateinit var emailId: String
    lateinit var contactNo: String
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_BLUETOOTH_PERMISSIONS = 3
    private  val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 4
    private val LOCATION_PERMISSION_REQUEST_CODE = 5
    private val REQUEST_LOCATION_PERMISSION = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var printButton:Button
    lateinit var cancelbtn:Button


    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Common SPP UUID
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private val pairedDevices = mutableListOf<BluetoothDevice>()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setOnKeyListener { _, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: throw IllegalStateException("Bluetooth not supported")
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Make background transparent
        return dialog
    }
    companion object {
        private const val ArnmerchantId = "merchantId"
        private const val ArndeviceId = "deviceId"
        private const val ArntranStatus = "tranStatus"
        private const val Arnamount = "amount"
        private const val Arnreferencelabel = "referencelabel"
        private const val ArntranId = "tranId"
        private const val ArntranDate = "tranDate"
        private const val ArnmerchantName = "merchantName"
        private const val ArnmerchantAddr = "merchantAddr"
        private const val ArnmerchantCity = "merchantCity"
        private const val ArnmerchantTerminal = "merchantTerminal"
        private const val Arnbillnumber = "billnumber"
        private const val Arnemail = "email"
        private const val Arncontact = "contact"

        fun newInstance(
            merchantId: String,
            deviceId: String,
            tranStatus: String,
            amount: String,
            referencelabel: String,
            tranId: String,
            tranDate: String,
            merchantName: String,
            merchantAddr: String,
            merchantCity: String,
            merchantTerminal: String,
            billnumber: String,
            emailid: String,
            contactno: String
        ): CustomDialogFragment {
            return CustomDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ArnmerchantId, merchantId)
                    putString(ArndeviceId, deviceId)
                    putString(ArntranStatus, tranStatus)
                    putString(Arnamount, amount)
                    putString(Arnreferencelabel, referencelabel)
                    putString(ArntranId, tranId)
                    putString(ArntranDate, tranDate)
                    putString(ArnmerchantName, merchantName)
                    putString(ArnmerchantAddr, merchantAddr)
                    putString(ArnmerchantCity, merchantCity)
                    putString(ArnmerchantTerminal, merchantTerminal)
                    putString(Arnbillnumber, billnumber)
                    putString(Arnemail, emailid)
                    putString(Arncontact, contactno)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Retrieve arguments
        merchantid = arguments?.getString(ArnmerchantId) ?: ""
        deviceid = arguments?.getString(ArndeviceId) ?: ""
        transtatus = arguments?.getString(ArntranStatus) ?: ""
        amount = arguments?.getString(Arnamount) ?: ""
        referencelabel = arguments?.getString(Arnreferencelabel) ?: ""
        tranId = arguments?.getString(ArntranId) ?: ""
        tranDATE = arguments?.getString(ArntranDate) ?: ""
        merchantname = arguments?.getString(ArnmerchantName) ?: ""
        merchantaddress = arguments?.getString(ArnmerchantAddr) ?: ""
        merchantCity = arguments?.getString(ArnmerchantCity) ?: ""
        terminalid = arguments?.getString(ArnmerchantTerminal) ?: ""
        billid = arguments?.getString(Arnbillnumber) ?: ""
        emailId = arguments?.getString(Arnemail) ?: ""
        contactNo = arguments?.getString(Arncontact) ?: ""

        return inflater.inflate(R.layout.activity_billtransaction, container, false)
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         printButton = view.findViewById<Button>(R.id.printScreenButton)
         cancelbtn = view.findViewById<Button>(R.id.cancelScreenButton)
        // Initialize TextViews
        initializeTextViews(view)
        printButton.setOnClickListener {
           /* printButton.visibility = View.GONE
            cancelbtn.visibility = View.GONE*/
            showYesNoDialog()
        }
        cancelbtn.setOnClickListener {
            startActivity(Intent(requireActivity(), MainActivity::class.java))
        }
    }
    private fun initializeTextViews(view: View) {
        val merchantidtext = view.findViewById<TextView>(R.id.merchantidtext)
        val merchantNametext = view.findViewById<TextView>(R.id.merchantNametext)
        val counterNametext = view.findViewById<TextView>(R.id.counterNametext)
        val addresstext = view.findViewById<TextView>(R.id.addresstext)
        val contactnumbertext = view.findViewById<TextView>(R.id.contactnumbertext)
        val emailidtext = view.findViewById<TextView>(R.id.emailidtext)
        val receiptnotext = view.findViewById<TextView>(R.id.receipttext)
        val transactiondatetext = view.findViewById<TextView>(R.id.transactiondatetext)
        val transactionamounttext = view.findViewById<TextView>(R.id.tranamounttext)
        val transtatustext = view.findViewById<TextView>(R.id.transtatustext)
        val referencenumbertext = view.findViewById<TextView>(R.id.referencetext)
        val billnotext = view.findViewById<TextView>(R.id.billtext)

        billnotext.text = "Bill No : $billid"
        merchantidtext.text = "Merchant ID : $merchantid"
        merchantNametext.text = "Merchant Name : $merchantname"
        counterNametext.text = "Counter Number: $terminalid"
        addresstext.text = "Address: $merchantaddress $merchantCity"
        contactnumbertext.text = "Contact Number: ${NullCheck.getValidText(contactNo)}"
        emailidtext.text = "Email: ${NullCheck.getValidText(emailId)}"
        receiptnotext.text = "Receipt No: ${Encryption.generatereceipt()}"
        transactiondatetext.text = "Transaction Date: $tranDATE"
        transactionamounttext.text = "Transaction Amount: BWP $amount"
        transtatustext.text = "Transaction Status: $transtatus"
        referencenumbertext.text = "Reference ID: $tranId"
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun showYesNoDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Connect Device")
            .setMessage("Please choose which device to connect")
            // Positive button: Bluetooth
            .setPositiveButton("Bluetooth") { _: DialogInterface, _: Int ->
                if(checkBluetoothPermissions()){
                    enableBluetooth()
                }
            }
            // Neutral button: Wi-Fi
            .setNeutralButton("Cancel") { dialog: DialogInterface, _: Int ->
                // Implement Wi-Fi connection logic here
                dialog.dismiss()
            }
            // Negative button: Cancel
            .setNegativeButton("Wi-fi") { dialog: DialogInterface, _: Int ->
                wifiPrint()
            }
            .create()
            .show()
    }
    private fun checkBluetoothPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 and above (API level 31+)
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION // Location is still required for scanning.
            )
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request Bluetooth permissions
                ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_BLUETOOTH_PERMISSIONS)
                return false
            } else {
                // Bluetooth is enabled and permissions are granted
                showToast("Bluetooth permissions granted")
                return true
            }
        } else {
            // For Android versions below 12 (API level < 31)
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request Bluetooth permissions for older versions
                ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_BLUETOOTH_PERMISSIONS)
                return false
            } else {
                // Bluetooth is enabled and permissions are granted
                showToast("Bluetooth permissions granted")
                return true
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == AppCompatActivity.RESULT_OK) {
            // Bluetooth is now enabled
            showToast("Bluetooth is enabled")
            checkBluetoothPermissions()
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == AppCompatActivity.RESULT_CANCELED) {
            // User denied enabling Bluetooth
            showToast("Bluetooth needs to be enabled")
        }
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                showToast("Bluetooth permissions granted")
            } else {
                // Permission denied
                showToast("Bluetooth permissions are required")
            }
        }
        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, show dialog again
                showDevices()
            } else {
                showToast("Bluetooth permission denied")
            }
        }
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start discovering devices

            } else {
                // Permission denied, show a message to the user
                showToast("Location permission is required to discover devices")
            }
        }
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showAvailableDevices()
            } else {
                showToast("Location permission is required for Bluetooth discovery")
            }
        }

    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun enableBluetooth() {
        if (bluetoothAdapter == null) {
            showToast("Bluetooth not supported")
        } else {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            } else {
                checkBluetoothPermissions()
                showDevices()
            }
        }
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun showDevices() {
        // Clear the list of paired devices
        pairedDevices.clear()
        // Retrieve the paired devices
        val pairedDeviceSet: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDeviceSet?.let {
            pairedDevices.addAll(it)
        }
        // Show the progress dialog
        NetworkUtils.showProgress(requireContext())

            NetworkUtils.hideProgress(requireContext())
            if (!isAdded) return // Ensure the fragment is still added
            val deviceNames = mutableListOf<String>()
            val allDevices = mutableListOf<BluetoothDevice>()
            // Add paired devices to the list
            pairedDevices.forEach { device ->
                deviceNames.add("${device.name} (Paired)")
                allDevices.add(device)
            }
            // Show a dialog with the paired devices
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Paired Devices")
                .setItems(deviceNames.toTypedArray()) { _, which ->
                    // Get the selected device
                    val selectedDevice = allDevices[which]
                    // Get the BluetoothClass of the selected device
                    val deviceClass = selectedDevice.bluetoothClass
                    val deviceMajorClass = deviceClass?.majorDeviceClass
                    // Check if the selected device is a printer
                    if (deviceMajorClass == BluetoothClass.Device.Major.IMAGING) {
                        if (selectedDevice.bondState == BluetoothDevice.BOND_BONDED) {
                            connectAndPrintData(selectedDevice, uuid)
                        }
                        else{
                            showToast("Device not paired")
                        }
                    }
                    else {
                        showToast("Selected device is not a printer")
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create()
            dialog.setOnShowListener {
                // Optional: Add any additional setup when the dialog is shown
            }
            dialog.show()
    }
    private fun connectAndPrintData(selectedDevice: BluetoothDevice, uuid: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            var bluetoothSocket: BluetoothSocket? = null
            try {
                // Check permissions only on Android 12 and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        checkBluetoothPermissions()  // Your method to request permissions
                        return@launch
                    }
                }

                // Attempt to create and connect Bluetooth socket
                bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket.connect()

                // Send data to the printer in a background thread
                printData(bluetoothSocket)

                // Notify UI about successful printing
                withContext(Dispatchers.Main) {
                    showToast("Printing completed")
                }
            } catch (e: SecurityException) {
                // Handle permissions-related issues
           //     e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToast("Bluetooth permission required")
                }
            } catch (e: IOException) {
                // Handle connection failure and ensure the socket is closed if needed
        //        e.printStackTrace()
                withContext(Dispatchers.Main) {
                    bluetoothSocket?.close()
                    showToast("Failed to connect to printer")
                }
            } finally {
                // Ensure Bluetooth socket is closed in all cases
                try {
                    bluetoothSocket?.close()
                } catch (e: IOException) {
               //     e.printStackTrace()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerPairingReceiver()
    }
    private fun startDiscovery() {
        if (bluetoothAdapter.isDiscovering) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
               checkBluetoothPermissions()
                return
            }
            bluetoothAdapter.cancelDiscovery() // Cancel any ongoing discovery
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter.startDiscovery()
        showDiscoveryDialog()
    }
    // Show AlertDialog for discovered (unpaired) devices
    private fun showDiscoveryDialog() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        NetworkUtils.showProgress(requireContext()) // Optional: Show progress dialog during discovery
        // Register the BroadcastReceiver for discovery results
        Handler().postDelayed({
            NetworkUtils.hideProgress(requireContext()) // Hide progress dialog
            if (discoveredDevices.isEmpty()) {
                showToast("No devices found")
                return@postDelayed
            }
            // Convert device names to a list for the dialog
            val deviceNames = discoveredDevices.map { it.name ?: "Unknown Device" }.toTypedArray()
            // Show the dialog with the discovered devices
            AlertDialog.Builder(requireContext())
                .setTitle("Available Devices")
                .setItems(deviceNames) { _, which ->
                    // User selected a device to pair
                    val selectedDevice = discoveredDevices[which]
                    val deviceClass = selectedDevice.bluetoothClass
                    val deviceMajorClass = deviceClass?.majorDeviceClass

                    // Check if the selected device is a printer
                    if (deviceMajorClass == BluetoothClass.Device.Major.IMAGING) {
                        pairWithDevice(selectedDevice)
                    } else {
                        showToast("Selected device is not a printer")
                    }
                }
                .setPositiveButton("Scan Again") { dialog, _ ->
                    startDiscovery() // Start discovery again on click
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        checkBluetoothPermissions()
                        return@setNegativeButton
                    }
                    bluetoothAdapter.cancelDiscovery() // Stop discovery on cancel
                }
                .create()
                .show()

        }, 5000) // Delay to allow discovery
    }
    private fun checkAndRequestLocationPermission() {

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Permission granted, proceed with checking location services
            checkLocationServicesEnabled()
        }
    }
    private fun checkLocationServicesEnabled() {
        // Ensure location services are enabled on the device
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showToast("Please enable Location Services")
            // Optional: redirect user to location settings
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            // Location services enabled, continue your Bluetooth discovery
            showAvailableDevices()
        }
    }
    private fun showAvailableDevices() {
        // Ensure Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled) {
            showToast("Please enable Bluetooth")
            return
        }
        discoveredDevices.clear()
        // Start discovery and check if it begins
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
        checkBluetoothPermissions()
            return
        }
        if (bluetoothAdapter.startDiscovery()) {
            showToast("Discovery started")
        } else {
            showToast("Failed to start discovery")
            return
        }
        // Display discovered devices after 10 seconds
        Handler().postDelayed({
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                checkBluetoothPermissions()
                return@postDelayed
            }
            bluetoothAdapter.cancelDiscovery()
            if (!isAdded) return@postDelayed
            // Hide progress and show devices in a dialog
            val deviceNames = discoveredDevices.map { it.name ?: "Unknown Device" }
            if (deviceNames.isEmpty()) {
                showToast("No devices found")
                return@postDelayed
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Available Devices")
                .setItems(deviceNames.toTypedArray()) { _, which ->
                    val selectedDevice = discoveredDevices[which]
                    pairWithDevice(selectedDevice)
                }
                .setPositiveButton("OK", null)
                .show()

            // Unregister the receiver

        }, 10000)
    }
    private fun pairWithDevice(device: BluetoothDevice) {
        registerPairingReceiver()
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        try {
            device.createBond()
            showToast("Pairing with ${device.name}")
        } catch (e: Exception) {
         //   e.printStackTrace()
            showToast("Failed to pair with device")
        }
    }
    private fun printData(bluetoothSocket: BluetoothSocket?) {
        requireActivity().runOnUiThread {
            printButton.visibility = View.GONE
            cancelbtn.visibility = View.GONE
        }
        // Check if the socket is null
        if (bluetoothSocket == null) {
            showToast("Bluetooth socket is null")
            return
        }

        // Check if the socket is connected
        if (!bluetoothSocket.isConnected) {
            showToast("Printer not connected")
            return
        }
        try {
            // Capture the screen content
            val rootView = view?.rootView ?: return
            // Create a bitmap of the view
            val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            rootView.draw(canvas)
            // Resize the bitmap to the printer's width (384 pixels for most thermal printers)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 384, (bitmap.height * (384.0 / bitmap.width)).toInt(), true)
            // Convert the bitmap to monochrome (black and white)
            val monochromeBitmap = convertToMonochrome(scaledBitmap)
            // Convert the monochrome bitmap to ESC/POS format
            val escPosData = convertBitmapToEscPos(monochromeBitmap)
            // Send the ESC/POS data to the printer
            val outputStream: OutputStream = bluetoothSocket.outputStream
            outputStream.write(escPosData)
            outputStream.flush()
            // Redirect to MainActivity after printing
            requireActivity().runOnUiThread {
                printButton.visibility = View.VISIBLE
                cancelbtn.visibility = View.VISIBLE
            }
        } catch (e: IOException) {
            // Handle IO exception
            //e.printStackTrace()
            requireActivity().runOnUiThread {
                showToast("Error occurred while printing: ${e.message}")
                printButton.visibility = View.VISIBLE
                cancelbtn.visibility = View.VISIBLE
            }
        }
        catch (e: Exception) {
         //   e.printStackTrace()
            requireActivity().runOnUiThread {
                showToast("Unexpected error occurred: ${e.message}")
                printButton.visibility = View.VISIBLE
                cancelbtn.visibility = View.VISIBLE
            }
        }
    }
    // Converts a Bitmap to monochrome (black and white)
    private fun convertToMonochrome(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val monochromeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val avg = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                if (avg < 128) {
                    monochromeBitmap.setPixel(x, y, Color.BLACK)
                } else {
                    monochromeBitmap.setPixel(x, y, Color.WHITE)
                }
            }
        }
        return monochromeBitmap
    }
    // Converts the monochrome bitmap into ESC/POS byte array
    private fun convertBitmapToEscPos(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val bytes = ByteArrayOutputStream()
        // ESC/POS command to start printing an image
        bytes.write(byteArrayOf(0x1B, 0x33, 0x00)) // Set line spacing
        for (y in 0 until height step 24) {
            bytes.write(byteArrayOf(0x1B, 0x2A, 33, (width % 256).toByte(), (width / 256).toByte())) // Image print command
            for (x in 0 until width) {
                // For each byte, send 3 bytes representing 24 pixels
                for (k in 0 until 3) {
                    var byte = 0
                    for (b in 0 until 8) {
                        val pixelY = y + k * 8 + b
                        if (pixelY < height) {
                            val pixel = bitmap.getPixel(x, pixelY)
                            if (pixel == Color.BLACK) {
                                byte = byte or (1 shl (7 - b))
                            }
                        }
                    }
                    bytes.write(byte)
                }
            }
            bytes.write(byteArrayOf(0x0A)) // Line feed
        }
        bytes.write(byteArrayOf(0x1B, 0x64, 0x02)) // Feed paper by 2 lines
        bytes.write(byteArrayOf(0x1B, 0x69)) // Cut paper command
        return bytes.toByteArray()
    }
    private fun wifiPrint() {
        val rootView = view?.rootView ?: return // Get the Fragment's root view
        // Create a bitmap of the view
        val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        rootView.draw(canvas)

        // Use PrintHelper to print the bitmap
        val printHelper = PrintHelper(requireContext())
        printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
        printHelper.printBitmap("Printed View", bitmap)
    }
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult() ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            showToast("Bluetooth enabled")
            showDevices()
        } else {
            AlertDialogBox().showDialog(requireContext(),"Please turn on bluetooth to print")
           // Toast.makeText(requireContext(), "Please turn on bluetooth to print", Toast.LENGTH_SHORT).show()
        }
    }
    private val pairingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
                when (bondState) {
                    BluetoothDevice.BOND_BONDING -> {
                        // Pairing is in progress
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                          checkBluetoothPermissions()
                            return
                        }
                        showToast("Pairing with ${device?.name}...")
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        showToast("Successfully paired with ${device?.name}")
                        showDevices()
                    }
                    BluetoothDevice.BOND_NONE -> {
                        // Pairing failed or device was unpaired
                        showToast("Pairing failed or unpaired with ${device?.name}")
                    }
                }
            }
        }
    }
    private fun registerPairingReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        requireContext().registerReceiver(pairingReceiver, filter)
    }
    private fun unregisterPairingReceiver() {
        requireContext().unregisterReceiver(pairingReceiver)
    }
    override fun onResume() {
        super.onResume()
        registerPairingReceiver()
    }
    override fun onPause() {
        super.onPause()
        registerPairingReceiver()
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterPairingReceiver()
    }
    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}