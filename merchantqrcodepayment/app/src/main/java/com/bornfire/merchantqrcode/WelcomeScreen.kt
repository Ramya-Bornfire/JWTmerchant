package com.bornfire.merchantqrcode

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.Utils.LogoutApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WelcomeScreen : BaseActivity() {
    lateinit var userId:TextView
    lateinit var deviceId:TextView
    lateinit var date:TextView
    private lateinit var welcomeText:TextView
    private lateinit var appNameText:TextView
    lateinit var userName:TextView
    private lateinit var headerAnime:LinearLayout
    val userData = SharedUserDataObj.userData
    private val merchantSharedData = SharedMerchantDataObj.merchantData
    private val userSharedCategory= SharedusercatDataObj.UserCategory
    private lateinit var uIdWelcome:TextView
    private lateinit var uNameWelcome:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)
        supportActionBar?.hide()
        welcomeText = findViewById(R.id.welcomeText)
        appNameText = findViewById(R.id.appNameText)
        headerAnime = findViewById(R.id.headerAnime)
        userName = findViewById(R.id.userName)

        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val animationZoom = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        headerAnime.startAnimation(animationZoom)
        Handler(Looper.getMainLooper()).postDelayed({
        }, 10000)

        appNameText.setText(R.string.merchantMobile)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                logoutAndCloseApp()
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        userId = findViewById(R.id.userid)
        deviceId = findViewById(R.id.devcieid)
        date = findViewById(R.id.date)

        uIdWelcome = findViewById(R.id.uid_welcome)
        uNameWelcome = findViewById(R.id.uname_welcome)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            welcomeText.text = getString(R.string.welcome)
            if(userSharedCategory?.user_category=="Representative"){
                uIdWelcome.text = getString(R.string.repId)
                uNameWelcome.text = getString(R.string.repName)
                userId.text = merchantSharedData?.merchant_rep_id.toString().uppercase()
                userName.text = merchantSharedData?.mer_representative_name.toString().uppercase()
            }
            else {
                uIdWelcome.text = getString(R.string.userId)
                uNameWelcome.text = getString(R.string.userName)
                userId.text = userData?.user_id.toString().uppercase()
                userName.text = userData?.user_name.toString().uppercase()
            }
        }
        else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            welcomeText.text=getString(R.string.welcomeMobile)
            userId.text = userData?.user_id.toString().uppercase()
            userName.text = userData?.user_name.toString().uppercase()
        }
        val currentDate = LocalDate.now()
        val inputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val date1: LocalDate = LocalDate.parse(currentDate.toString(), inputFormat1) // Parse input date string
        val formattedDate2 = date1.format(outputFormat1)
        date.text = formattedDate2
        val startBtn = findViewById<Button>(R.id.startscreenbtn)
        startBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun logoutAndCloseApp() {
            if (userSharedCategory?.user_category == "Representative") {
                LogoutApi.logoutTab(merchantSharedData?.merchant_rep_id.toString(),this)
            }
            else{
                LogoutApi.logoutMobile(userData?.user_id.toString(),this)
            }
    }
    override fun onResume() {
        super.onResume()
        val intent = Intent("com.bornfire.ACTION_RESUME")
        sendBroadcast(intent)
    }
}