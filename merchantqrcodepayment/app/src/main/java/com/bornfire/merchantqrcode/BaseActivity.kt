package com.bornfire.merchantqrcode

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.Utils.LogoutApi

open class BaseActivity : AppCompatActivity() {
    private val PREFS_NAME = "AppPrefs"
    private val LAST_VISIBILITY_KEY = "lastVisibilityTime"
    private var recClose:Boolean = false
    private val mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable {
        val intent = Intent(this, ScreenLockActivity::class.java)
        startActivity(intent)
        onStop()
    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        resetTimer()
        return super.dispatchTouchEvent(ev)
    }
    private fun resetTimer() {
        mHandler.removeCallbacks(mRunnable)
        startTimer()
    }
    private fun startTimer() {
        mHandler.postDelayed(mRunnable, 180000) // 30 seconds delay
    }

    override fun onStop() {
        super.onStop()
        mHandler.removeCallbacks(mRunnable)
    }
    override fun onResume() {
        super.onResume()
        startTimer()
        if(recClose){
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val lastVisibilityTime = prefs.getLong(LAST_VISIBILITY_KEY, 0)
            val currentTime = System.currentTimeMillis()
            if (lastVisibilityTime > 0 && currentTime - lastVisibilityTime >= 3 * 60 *1000) {
                logoutAndCloseApp()
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
                recClose = false
            }
        }
    }
    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(mRunnable)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putLong(LAST_VISIBILITY_KEY, System.currentTimeMillis()).apply()
        recClose = true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTimer()
    }
    private fun logoutAndCloseApp() {
        val userCategory = SharedusercatDataObj.UserCategory
        val merchantData = SharedMerchantDataObj.merchantData
        if (userCategory?.user_category == "Representative") {
            LogoutApi.logoutTab(merchantData?.merchant_rep_id.toString(),this)
        } else {
            val userData = SharedUserDataObj.userData
            LogoutApi.logoutMobile(userData?.user_id.toString(),this)
        }
    }

}
