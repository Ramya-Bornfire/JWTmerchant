package com.bornfire.merchantqrcode

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.Utils.HelpInfo

class faqactivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faqactivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            // Set the title
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.faq)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener(){
                HelpInfo.getInfo(this,"9")
            } }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}