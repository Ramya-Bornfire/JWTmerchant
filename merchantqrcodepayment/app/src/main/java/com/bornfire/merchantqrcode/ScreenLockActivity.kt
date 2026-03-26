package com.bornfire.merchantqrcode

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.ViewPager
import com.bornfire.merchantqrcode.DataModel.*
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.LogoutApi
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScreenLockActivity : AppCompatActivity() {
    private val prefsName = "AppPrefs"
    private val lastVisibilityKey = "lastVisibilityTime"
    private var recClose: Boolean = false
    private lateinit var unlockBtn1: Button
    private lateinit var lockView: CardView
    private lateinit var lockPassword: EditText
    private lateinit var visibleImg1: ImageView
    private lateinit var unlock: Button
    lateinit var viewPager: ViewPager
    private var visibility: Boolean = true
    private val userSharedCategory = SharedusercatDataObj.UserCategory
    val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var carouselItems: List<CarouselItem>
    val psuDeviceID = Encryption.generatePSUDeviceId()
    val objectMapper = jacksonObjectMapper()
    val userData = SharedUserDataObj.userData
    private val merchantSharedData = SharedMerchantDataObj.merchantData
    private val runnable = object : Runnable {
        override fun run() {
            viewPager.currentItem = (viewPager.currentItem + 1) % carouselItems.size
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_lock)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.title = getString(R.string.sc_lock)
        if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.sc_lock)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener {
                HelpInfo.getInfo(this, "11")
            }
        }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                alertLogout()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        visibleImg1 = findViewById(R.id.pswdvisble)
        requestedOrientation = if (isTablet()) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        unlockBtn1 = findViewById(R.id.unlockBtn1)
        lockView = findViewById(R.id.lockView)
        lockPassword = findViewById(R.id.lockPswd)
        unlock = findViewById(R.id.unlock)
        viewPager = findViewById(R.id.viewPager)
        lockView.visibility = View.GONE
        unlockBtn1.setOnClickListener {
            unlockBtn1.visibility = View.GONE
            lockView.visibility = View.VISIBLE
        }
        unlock.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(this)) {
                    if (lockPassword.text.toString().isEmpty()) {
                        AlertDialogBox().showDialog(this, "Please enter the password")
                    } else {
                        if (userSharedCategory?.user_category == "Representative") {
                            passwordTab(merchantSharedData!!.merchant_rep_id!!,
                                lockPassword.text.toString()
                            )
                        } else {
                            passwordMobile(userData!!.user_id, lockPassword.text.toString())
                        }
                    }

            } else {
                NetworkUtils.NoInternetAlert(this)
            }
        }
        carouselItems = listOf(
            CarouselItem(R.drawable.img1),
            CarouselItem(R.drawable.img2),
            CarouselItem(R.drawable.img3),
            CarouselItem(R.drawable.img4),
            CarouselItem(R.drawable.img5)
        )
        val adapter = CarouselAdapter(this, carouselItems) {
        }
        viewPager.adapter = adapter
        startAutoScroll()
    }

    fun onImageClick(view: View) {
        if (visibility) {
            lockPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            visibility = false
            visibleImg1.setImageResource(R.drawable.ic_lock_visibility)
        } else {
            lockPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            visibility = true
            visibleImg1.setImageResource(R.drawable.ic_lock_visibility_off)
        }
    }
    private fun startAutoScroll() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 3000)
    }

    override fun onSupportNavigateUp(): Boolean {
        alertLogout()
        return true
    }

    private fun passwordTab(empId: String, password: String) {
        val call = ApiClient.apiService.getPasswordForMerchant(empId, password, psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                  //      println("Decrypted response: $decryptedResponse")
                        val loginResponse = objectMapper.readValue(
                            decryptedResponse,
                            ApiResponse::class.java
                        )
                        if (loginResponse?.Message == "Success") {
                            unlockScreen()
                        } else {
                            Toast.makeText(this@ScreenLockActivity, "Wrong Password", Toast.LENGTH_LONG).show()
                        }
                    } else {
                    //    println("Error: ${response.code()}")
                        Toast.makeText(this@ScreenLockActivity, "Invalid credentials", Toast.LENGTH_LONG).show()
                    }
                } else {
                    //println("Error: ${response.code()}")
                    Toast.makeText(
                        this@ScreenLockActivity,
                        "Invalid credentials",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               // println("Network error: ${t.message}")
                Toast.makeText(
                    this@ScreenLockActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()

            }
        })
    }

    private fun passwordMobile(userId: String, password: String) {
        val call = ApiClient.apiService.getPasswordForUser(userId, password, psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                    //    println("Decrypted response: $decryptedResponse")
                        val loginResponse = objectMapper.readValue(
                            decryptedResponse,
                            ApiResponse::class.java
                        )
                        if (loginResponse?.Message == "Success") {
                            unlockScreen()
                        } else {
                            Toast.makeText(
                                this@ScreenLockActivity, "Wrong Password", Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                       // println("Error: ${response.code()}")
                        Toast.makeText(
                            this@ScreenLockActivity,
                            "Invalid credentials",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                  //  println("Error: ${response.code()}")
                    Toast.makeText(
                        this@ScreenLockActivity,
                        "Invalid credentials",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  println("Network error: ${t.message}")
                Toast.makeText(
                    this@ScreenLockActivity,
                    "Something Went Wrong at Server End",
                    Toast.LENGTH_LONG
                ).show()

            }
        })
    }

    private fun unlockScreen() {
        Toast.makeText(this@ScreenLockActivity, "Screen is unlocked", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun isTablet(): Boolean {
        return resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    override fun onPause() {
        super.onPause()
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        prefs.edit().putLong(lastVisibilityKey, System.currentTimeMillis()).apply()
        recClose = true
    }

    override fun onResume() {
        super.onResume()
        if (recClose) {
            val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
            val lastVisibilityTime = prefs.getLong(lastVisibilityKey, 0)
            val currentTime = System.currentTimeMillis()
            if (lastVisibilityTime > 0 && currentTime - lastVisibilityTime >= 30 * 60 * 1000) {
                logoutAndCloseApp()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                recClose = false
            }
        }
    }

    private fun logoutAndCloseApp() {
        val userSharedCategory = SharedusercatDataObj.UserCategory
        val merchantData = SharedMerchantDataObj.merchantData
        if (userSharedCategory?.user_category == "Representative") {
            LogoutApi.logoutTab(merchantData?.merchant_rep_id.toString(), this)
        } else {
            val userData = SharedUserDataObj.userData
            LogoutApi.logoutMobile(userData?.user_id.toString(), this)
        }
        finishAffinity()
    }

    fun alertLogout() {
        AlertDialog.Builder(this@ScreenLockActivity)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Ok") { _, _ ->
                logoutAndCloseApp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}