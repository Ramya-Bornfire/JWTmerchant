package com.bornfire.merchantqrcode.AdminScreens

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.GridView
import androidx.activity.OnBackPressedCallback
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.*
import com.bornfire.merchantqrcode.DataModel.gridmodel
import com.bornfire.merchantqrcode.GridViewAdapter

class AdminActivity : BaseActivity() {

    private lateinit var gridView: GridView
    private lateinit var allMenu: List<gridmodel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adminactivity)
        startService(Intent(this, AppMonitorService::class.java))
        setupActionBar()
        initializeGridView()
        configureGridItems()
        setupGridViewClickListener()
        handleBackPress()
    }
    private fun setupActionBar() {
        supportActionBar?.apply {
            title = getString(R.string.admin)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun initializeGridView() {
        gridView = findViewById(R.id.adminMenu)
        gridView.numColumns = 3
    }

    private fun configureGridItems() {
        allMenu = listOf(
            gridmodel(getString(R.string.user_list), R.drawable.ic_usermanage),
            gridmodel(getString(R.string.device_list), R.drawable.ic_devicemanage),
            gridmodel("RATE MAINTENANCE", R.drawable.ic_rate),
            gridmodel("CUSTOMER TRANSACTION", R.drawable.ic_custrans),
            gridmodel("FEES AND CHARGES", R.drawable.fees_and_charges_img),
            gridmodel("CHARGE BACKS", R.drawable.ic_chargeback),
            gridmodel(getString(R.string.alert_list), R.drawable.alert),
            gridmodel(getString(R.string.notification_list), R.drawable.ic_alert),
            gridmodel(getString(R.string.service_list), R.drawable.ic_serv_req)
        )
        gridView.adapter = GridViewAdapter(courseList = allMenu, context = this)
    }

    private fun setupGridViewClickListener() {
        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val intent = when (position) {
                0 -> Intent(this, Usermanagement::class.java)
                1 -> Intent(this, DeviceManagement::class.java)
                2 -> {
                  //  Intent(this, RateMaintance::class.java)
                  showModuleNotApplicableDialog()
                    return@OnItemClickListener
                }
                3 -> Intent(this, CustomerTransaction::class.java)
                4 -> Intent(this, FeesAndCharge::class.java)
                5 -> Intent(this, Chargebacks::class.java)
                6 -> Intent(this, AlertScreen::class.java)
                7 -> Intent(this, NotificationScreen::class.java)
                8 -> Intent(this, ServiceRequest::class.java)
                else -> null
            }

            intent?.let { startActivity(it) }
        }
    }

    private fun showModuleNotApplicableDialog() {
        AlertDialog.Builder(this)
            .setTitle("Alert")
            .setCancelable(false)
            .setMessage("This Module is currently not Applicable")
            .setNegativeButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMainActivity()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateToMainActivity()
        return true
    }
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }
}
