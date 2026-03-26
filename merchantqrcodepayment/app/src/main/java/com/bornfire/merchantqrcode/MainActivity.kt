package com.bornfire.merchantqrcode

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.AdminScreens.AdminActivity
import com.bornfire.merchantqrcode.AdminScreens.Chargebacks
import com.bornfire.merchantqrcode.DataModel.*
import com.bornfire.merchantqrcode.Receiver.NetworkChangeReceiver
import com.bornfire.merchantqrcode.Utils.AuthToken
import com.bornfire.merchantqrcode.Utils.LogoutClass
import com.bornfire.merchantqrcode.Utils.NullCheck
import com.bornfire.merchantqrcode.Utils.TextforTable.formatNumberWithGroups
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.bornfire.merchantqrcode.retrofit.Encryption.getAndroidId
import com.bornfire.merchantqrcode.retrofit.Encryption.getRandom4Digit
import com.google.android.material.navigation.NavigationView
import com.scottyab.rootbeer.RootBeer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.*
class MainActivity : BaseActivity() {
    private lateinit var passwordExpiryDate :String
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var gridView: GridView
    private lateinit var currentDateTime: TextView
    private var pauseAt:Long=0
    private lateinit var merchantUserName:TextView
    private lateinit var merchantUserId:TextView
    private val callPermissionCode = 123
    lateinit var qr:String
    val userData = SharedUserDataObj.userData
    private val merchantSharedData = SharedMerchantDataObj.merchantData
    private val userSharedCategory= SharedusercatDataObj.UserCategory
    lateinit var merchantName : String
    lateinit var merchantId : String
    lateinit var merId : String
    lateinit var unitId : String
    lateinit var transList: List<TransData>
    lateinit var username : String
    lateinit var userid : String
    lateinit var date:TextView
    private lateinit var sdf :SimpleDateFormat
    private lateinit var profileImg:ImageView
    private lateinit var uId:TextView
    private lateinit var uname:TextView
    var totalAmount = 0.00
    private lateinit var psuChannel:String
    var countTrans = 0
    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        networkChangeReceiver = NetworkChangeReceiver()
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        gridView = findViewById(R.id.groupfingertips)
        profileImg = findViewById(R.id.profileimg)
        if (isDeviceRooted()||  isDeviceCompromised(this)||isUsingVPN() || isUsingProxy(this)||checkFridaFiles()) {
             Toast.makeText(this, "Rooted device detected! Exiting...", Toast.LENGTH_LONG).show()
            finishAffinity() // Close the app
        } else {
            // Toast.makeText(this, "Device is secure", Toast.LENGTH_SHORT).show()
        }

        if (isDeviceCompromised(this)) {
            Toast.makeText(this, "Rooted or Frida detected! Exiting...", Toast.LENGTH_LONG).show()
            finishAffinity() // Close app
        }

        gridView.numColumns = 3
        merchantUserName=findViewById(R.id.merchantname)
        merchantUserId = findViewById(R.id.merchantid)
        profileImg.setOnClickListener{
            startActivity(Intent(this, profileactivity::class.java))
        }
        navigationView = findViewById(R.id.nav_view)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        uId = findViewById(R.id.uId_dash)
        uname = findViewById(R.id.uname_dash)
        if(userSharedCategory?.user_category=="Representative") {
            uId.text = getString(R.string.repId)
            uname.text = getString(R.string.repName)
            passwordExpiryDate = merchantSharedData?.password_expiry_date.toString()
            username= NullCheck.getValidText(merchantSharedData?.mer_representative_name.toString())
            userid=NullCheck.getValidText(merchantSharedData?.merchant_rep_id.toString())
            unitId=NullCheck.getValidText(merchantSharedData?.unit_id)
            merId=NullCheck.getValidText(merchantSharedData?.merchant_user_id.toString())
            merchantName = getString(R.string.colan, username)
            merchantId = getString(R.string.colan, userid)
            merchantUserName.text = (merchantName)
            merchantUserId.text = (merchantId)
        }else{
            uId.text = getString(R.string.userId)
            uname.text = getString(R.string.userName)
            passwordExpiryDate = userData?.password_expiry_date1.toString()
            username= NullCheck.getValidText(userData!!.user_name.toString())
            userid=NullCheck.getValidText(userData.user_id)
            unitId=NullCheck.getValidText(userData.unit_id)
            merId=NullCheck.getValidText(userData.merchant_user_id.toString())
            merchantUserName.text =getString(R.string.colan,username )
            merchantUserId.text = getString(R.string.colan, userid)
        }
        if (isTablet) {
            psuChannel="TABLET"
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
             var chronometer = findViewById<Chronometer>(R.id.chronometer)
            chronometer.base = SystemClock.elapsedRealtime()-pauseAt
            chronometer.start()
            sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss",Locale.US)
            var transCountLayout = findViewById<LinearLayout>(R.id.transCountLayout)
            transCountLayout.setOnClickListener{
                startActivity(Intent(this, TransactionHistoryActivity::class.java))
            }
            fetchMerchantTransData(merId,userid)
        } else {
            psuChannel = "MOBILE"
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            sdf = SimpleDateFormat("dd-MM-yyyy",Locale.US)
        }
        passwordExpiry()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                else {
                    logoutAlert()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        setupGridView(userSharedCategory?.user_category)
        drawerLayout = findViewById(R.id.my_drawer_layout)
        currentDateTime = findViewById(R.id.lastvisitedtime)
        val displayDate = sdf.format(Date())
        currentDateTime.text = getString(R.string.colan,displayDate)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_transaction-> {
                    startActivity(Intent(this@MainActivity,TransactionHistoryActivity::class.java))
                    true
                }
                R.id.nav_userprofile->{
                    startActivity(Intent(this@MainActivity, profileactivity::class.java))
                    true
                }
                R.id.nav_FAQ->{
                    startActivity(Intent(this@MainActivity, faqactivity::class.java))
                    true
                }
                R.id.screenLock->{
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(this@MainActivity, ScreenLockActivity::class.java))
                    true
                }
                R.id.passwordChange->{
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this,NewPassword::class.java)
                    intent.putExtra("userId", userid)
                    startActivity(intent)
                    true
                }
                R.id.nav_logout->{
                    logoutAlert()
                    true
                }
                else -> false
            }
        }
    }
    fun isFridaRunning(): Boolean {
        val fridaProcesses = listOf("frida", "gum-js", "gadget")
        val mapsFile = File("/proc/self/maps")

        if (mapsFile.exists()) {
            val content = mapsFile.readText()
            fridaProcesses.forEach { process ->
                if (content.contains(process)) {
                    return true
                }
            }
        }
        return false
    }

    private fun setupGridView(userCategory: String?) {
        val gridModelList = mutableListOf<gridmodel>()
        if (userCategory == "Representative") {
            gridModelList.apply {
                add(gridmodel("Admin", R.drawable.admin_imgg))
                add(gridmodel("Static QR", R.drawable.static_qr_img))
                add(gridmodel("Dynamic QR", R.drawable.dynamic_qr_img))
                add(gridmodel("Scan QR", R.drawable.scan_qr_img))
                add(gridmodel("Inquiries and Reports", R.drawable.img8))
                add(gridmodel("Contact Us", R.drawable.contact_us_img2))
            }
        } else {
            gridModelList.apply {
                add(gridmodel("Static QR", R.drawable.static_qr_img))
                add(gridmodel("Dynamic QR", R.drawable.dynamic_qr_img))
                add(gridmodel("Scan QR", R.drawable.scan_qr_img))
                add(gridmodel("Transaction Details", R.drawable.img8))
                add(gridmodel("Charge Back", R.drawable.ic_chargeback))
                add(gridmodel("Contact Us", R.drawable.contact_us_img2))
            }
        }
        val courseAdapter = GridViewAdapter(courseList = gridModelList, context = this)
        gridView.adapter = courseAdapter
        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (gridModelList[position].Name) { // Get the name of the clicked item
                "Admin" -> startActivity(Intent(this, AdminActivity::class.java))
                "Static QR" -> handleStaticQr()
                "Dynamic QR" -> startActivity(Intent(this, DynamicQRActivity::class.java))
                "Scan QR" ->  startActivity(Intent(this, ScanQRActivity::class.java))
                "Inquiries and Reports" -> showInquiriesAndReportsMenu()
                "Transaction Details" -> startActivity(Intent(this, TransactionHistoryActivity::class.java))
                "Charge Back" -> startActivity(Intent(this, Chargebacks::class.java))
                "Contact Us" -> startActivity(Intent(this, ContactScreen::class.java))//showContactUsMenu()
            }
        }
    }
    private fun handleStaticQr() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            generateStaticMerQR()
        } else {
            NetworkUtils.NoInternetAlert(this)
        }
    }
    /**
     * Check if the device is connected to a VPN.
     */
    private fun isUsingVPN(): Boolean {
        try {
            val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in networkInterfaces) {
                if (networkInterface.isUp && (networkInterface.name.contains("tun") ||
                            networkInterface.name.contains("ppp") ||
                            networkInterface.name.contains("pptp"))
                ) {
                    return true
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Check if the device is using a proxy.
     */
    private fun isUsingProxy(context: Context): Boolean {
        return try {
            val proxyAddress = System.getProperty("http.proxyHost")
            val proxyPort = System.getProperty("http.proxyPort")?.toIntOrNull()
            if (proxyAddress != null && proxyPort != null && proxyPort != -1) {
                return true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (networkCapabilities != null && networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI)) {
                    val proxyInfo = connectivityManager.getDefaultProxy()
                    return proxyInfo != null
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    private fun isDeviceRooted(): Boolean {
        val rootBeer = RootBeer(this)
        return rootBeer.isRooted
    }
    fun isRootBinaryPresent(): Boolean {
        val rootBinaries = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/xbin/su"
        )

        return rootBinaries.any { File(it).exists() }
    }
    fun isRootManagementAppInstalled(context: Context): Boolean {
        val rootApps = listOf(
            "com.noshufou.android.su", "eu.chainfire.supersu",
            "com.koushikdutta.superuser", "com.zachspong.temprootremovejb"
        )

        return rootApps.any {
            try {
                context.packageManager.getPackageInfo(it, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
    fun isDangerousPropsSet(): Boolean {
        val props = listOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0"
        )

        return props.any { (key, expected) ->
            try {
                val process = Runtime.getRuntime().exec("getprop $key")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val value = reader.readLine()
                value == expected
            } catch (e: Exception) {
                false
            }
        }
    }
    fun hasTestKeys(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }
    fun canExecuteSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            process.inputStream.bufferedReader().readLine() != null
        } catch (e: Exception) {
            false
        }
    }
    fun isFridaPortOpen(): Boolean {
        val ports = listOf(27042, 27043)

        return ports.any {
            try {
                Socket("127.0.0.1", it).use { _ -> true }
            } catch (e: Exception) {
                false
            }
        }
    }
    fun isFridaInjected(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("cat /proc/net/tcp")
            process.inputStream.bufferedReader().useLines { lines ->
                lines.any { it.contains("27042") || it.contains("27043") }
            }
        } catch (e: Exception) {
            false
        }
    }
    fun isDeviceCompromised(context: Context): Boolean {
        return isRootBinaryPresent() ||
                isRootManagementAppInstalled(context) ||
                isDangerousPropsSet() ||
                hasTestKeys() ||
                canExecuteSuCommand() ||
                isFridaRunning() ||
                isFridaPortOpen() ||
                isFridaInjected()
    }
    private fun showInquiriesAndReportsMenu() {
        val popupMenu = PopupMenu(this, gridView[4])
        popupMenu.menuInflater.inflate(
            R.menu.inquries_reports,
            popupMenu.menu
        )
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.transInq -> {
                    val intent =
                        Intent(this, TransactionHistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.transStatement -> {
                    val intent =
                        Intent(this, TransactionStatement::class.java)
                    startActivity(intent)
                }
            }
            true
        }
        popupMenu.show()
    }
    private fun showContactUsMenu() {
        val popupMenu = PopupMenu(this, gridView[5])
        popupMenu.menuInflater.inflate(R.menu.contactus, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.cl -> {
                }
                R.id.em -> {
                }
            }
               true
            }
            popupMenu.show()
    }

    private fun passwordExpiry() {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy",Locale.US)
        val givenDate = dateFormat.parse(passwordExpiryDate)
        val today = Date()
        if (today < givenDate) {
            showYesNoDialog(userid)
        }
    }
    fun checkFridaFiles(): Boolean {
        val paths = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/frida-agent.so",
            "/data/local/tmp/libgadget.so",
            "/system/bin/frida-server",
            "/system/xbin/frida-server"
        )

        paths.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                return true
            }
        }
        return false
    }

    private fun generateStaticMerQR() {
        val pId =  Encryption.generatePID()
        val referenceNumber=pId+ getRandom4Digit()
        //val psuDeviceId=Encryption.generatePSUDeviceId()
        val psuDeviceId = AuthToken.getPSUDeviceId(this)!!
        val psuIpAddress=Encryption.getPSUIPADDRESS()
        val psuId=Encryption.generatePSUID(this)
        val code = ScanQRrequest("00020101021226670009mu01120215CIM2394871283740315CIM24032118660452045811530348054035505802MU5911MARINA MALL6010PORT LOUIS62170205234240704 242630405AF")
        val call = ApiClient.apiService.generateStaticQRCode(pId,psuDeviceId,psuIpAddress,psuId,psuChannel,merId,getAndroidId(this),referenceNumber,userid,unitId,code)
        call.enqueue(object : Callback<StaticResponse> {
            override fun onResponse(
                call: Call<StaticResponse>,
                response: Response<StaticResponse>
            ) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val loginEntityJsonString = loginResponse?.base64QR
                    gotoStaticScreen(loginEntityJsonString.toString())
                } else {
                    Toast.makeText(this@MainActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<StaticResponse>, t: Throwable) {
              //  println("Network error: ${t.message}")
                Toast.makeText(this@MainActivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun gotoStaticScreen(base64String:String) {
        val intent = Intent(this,StaticQRActivity::class.java)
        intent.putExtra("Base64String", base64String)
        startActivity(intent)
    }
    private fun logoutAndCloseApp() {
            if (userSharedCategory?.user_category == "Representative") {
                LogoutClass.LogoutforTab(userid)
            }
            else{
                LogoutClass.LogoutforMobile(userid)
            }
      finishAffinity()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            callPermissionCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall()
                } else {
                    // Permission denied, handle accordingly (e.g., show a message)
                }
            }
        }
    }
    private fun makePhoneCall() {
        val phoneNumber = "tel:9884298802" // replace with the actual phone number
        val dial = Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber))
        startActivity(dial)
    }
    private fun logoutAlert()
    {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Ok") { _, _ ->
                logoutAndCloseApp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun fetchMerchantTransData(merId: String,userid:String) {
        var transCount = findViewById<TextView>(R.id.transCount)
        var todayTransAmount= findViewById<TextView>(R.id.todayTransAmount)
        val calendar = Calendar.getInstance()
        val currentDate: Date = calendar.time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateString: String = dateFormat.format(currentDate)
        val call: Call<List<TransData>> = ApiClient.apiService.getTransactionDetails(merId,userid,dateString,dateString,"pdf")
        call.enqueue(object : Callback<List<TransData>> {
            override fun onResponse(
                call: Call<List<TransData>>,
                response: Response<List<TransData>>
            ) {
                if (response.isSuccessful) {
                    transList = response.body()!!
                  //  println(transList)
                    val filteredUserList = transList.filter { it.tran_status == "SUCCESS" }
                    filteredUserList.forEach{ transList->
                        val timestamp =  transList.tran_date
                        val tranDateFormat = SimpleDateFormat("dd-MM-yyyy",Locale.US)
                        val correctDate=tranDateFormat.format(Date())
                        if (correctDate.toString()==timestamp){
                            totalAmount += transList.tran_amount.toDouble()
                            countTrans+=1
                        }
                    }
                    transCount.text = countTrans.toString()
                    todayTransAmount.text =getString(R.string.displayAmount,formatNumberWithGroups(totalAmount))
                } else {
                    Toast.makeText(this@MainActivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<TransData>>, t: Throwable) {
               // t.printStackTrace()
                Toast.makeText(this@MainActivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
    }
    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }
    private fun showYesNoDialog(empId:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Password Expired")
        builder.setMessage("Your password is due to expire soon; it is recommended to change it promptly for security reasons")
        builder.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
           val intent = Intent(this,NewPassword::class.java)
            intent.putExtra("userId", empId)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }
}