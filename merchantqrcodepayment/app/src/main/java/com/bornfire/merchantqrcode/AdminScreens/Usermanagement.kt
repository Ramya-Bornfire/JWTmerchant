package com.bornfire.merchantqrcode.AdminScreens

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.*
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.UserData
import com.bornfire.merchantqrcode.DetailScreen.UserDetailsActivity
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.TextforTable.createTextView
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class Usermanagement : BaseActivity() {
    private lateinit var edFilter: EditText
    lateinit var userList: List<UserData>
    private lateinit var tableLayout: TableLayout
    private lateinit var backBtn: ImageView
    private lateinit var nodata: TextView
    private var usid: String = "${SharedMerchantDataObj.merchantData?.merchant_user_id}U001"
    private lateinit var um_help_image: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usermanagement)
        tableLayout = findViewById(R.id.tableLayout)
        nodata = findViewById(R.id.emptyTextView)
        edFilter = findViewById(R.id.editFilter)
        backBtn = findViewById(R.id.backBtn)
        um_help_image = findViewById(R.id.um_help_image)
        setupUI()
        setupListeners()

        if (NetworkUtils.isNetworkAvailable(this)) {
            fetchUserData()
        } else {
            NetworkUtils.NoInternetAlert(this)
        }
    }
    private fun setupUI() {
        startService(Intent(this, AppMonitorService::class.java))
        supportActionBar?.hide()
        edFilter.visibility = View.VISIBLE
        nodata.visibility = View.GONE
    }
    private fun setupListeners() {
        backBtn.setOnClickListener { finish() }
        um_help_image.setOnClickListener { HelpInfo.getInfo(this, "12") }
        edFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val filterQuery = s.toString().trim()
                if (filterQuery.isNotEmpty()) {
                    filterData(filterQuery)
                } else {
                    fetchUserData()
                }
            }
        })
        findViewById<Button>(R.id.addBtn).setOnClickListener {
            startActivity(Intent(this, UserDetailsActivity::class.java).apply {
                putExtra("Button", "Submit")
                putExtra("UserId", usid)
            })
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@Usermanagement, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        })
    }
    private fun filterData(query: String) {
        val filteredList = userList?.filter {
            val status = getUserStatus(it)
            it.user_id.contains(query, ignoreCase = true) ||
                    it.user_name.contains(query, ignoreCase = true) ||
                    it.user_role.contains(query, ignoreCase = true) ||
                    it.unit_id_u.contains(query, ignoreCase = true) ||
                    it.unit_name_u.contains(query, ignoreCase = true) ||
                    status.contains(query, ignoreCase = true)
        }
        if (filteredList.isNullOrEmpty()) {
            AlertDialogBox().showDialog(this, "No matching data found")
        } else {
            updateTable(filteredList)
        }
    }
    private fun getUserStatus(user: UserData): String {
        return if ((user.entry_flag == "N" && user.modify_flag == "N") ||
            (user.entry_flag == "N" && user.modify_flag == "Y")) "Unverified" else "Verified"
    }
    private fun updateTable(filteredList: List<UserData>) {
        tableLayout.removeAllViews()
        tableLayout.addView(createHeaderRow())

        filteredList.forEach { userData ->
            tableLayout.addView(createTableRow(userData))
        }
    }
    private fun createHeaderRow(): TableRow {
        return TableRow(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@Usermanagement, R.color.liteOrange))
            addView(createTextView(this@Usermanagement, "USER ID").apply {
                setTypeface(null, android.graphics.Typeface.BOLD)
                textSize = 15f
            })
            addView(createTextView(this@Usermanagement, "USER NAME").apply {
                setTypeface(null, android.graphics.Typeface.BOLD)
                textSize = 15f
            })
            addView(createTextView(this@Usermanagement, "UNIT ID").apply {
                setTypeface(null, android.graphics.Typeface.BOLD)
                textSize = 15f
            })
            addView(createTextView(this@Usermanagement, "UNIT NAME").apply {
                setTypeface(null, android.graphics.Typeface.BOLD)
                textSize = 15f
            })
            addView(createTextView(this@Usermanagement, "ROLE").apply {
                setTypeface(null, android.graphics.Typeface.BOLD)
                textSize = 15f
            })
            addView(createTextView(this@Usermanagement, "STATUS").apply {
                setTypeface(null, android.graphics.Typeface.BOLD)
                textSize = 15f
            })
        }
    }
    private fun createTableRow(userData: UserData): TableRow {
        return TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            addView(createTextView(this@Usermanagement, userData.user_id))
            addView(createTextView(this@Usermanagement, userData.user_name))
            addView(createTextView(this@Usermanagement, userData.unit_id_u))
            addView(createTextView(this@Usermanagement, userData.unit_name_u))
            addView(createTextView(this@Usermanagement, userData.user_role))
            addView(createTextView(this@Usermanagement, getUserStatus(userData)))
            setOnClickListener { navigateToUserDetails(userData) }
        }
    }
    private fun navigateToUserDetails(user: UserData) {
        startActivity(Intent(applicationContext, UserDetailsActivity::class.java).apply {
            putExtra("userId", user.user_id)
            putExtra("userName", user.user_name)
            putExtra("defaultDeviceId", user.default_device_id)
            putExtra("deviceId1", user.alternative_device_id1)
            putExtra("deviceId2", user.alternative_device_id2)
            putExtra("password", user.password1)
            putExtra("userDisabledFromDate", user.user_disable_from_date1)
            putExtra("userDisabledToDate", user.user_disable_to_date1)
            putExtra("mobileNum", user.mobile_no1)
            putExtra("email", user.email_address1)
            putExtra("designation", user.user_designation)
            putExtra("role", user.user_role)
            putExtra("alterMobNo", user.alternate_mobile_no1)
            putExtra("alterEmail", user.alternate_email_id1)
            putExtra("makerChecker", user.make_or_checker)
            putExtra("unitId", user.unit_id_u)
            putExtra("unitName", user.unit_name_u)
            putExtra("unitType", user.unit_type_u)
            putExtra("verify", user.verify_user)
            putExtra("entryUser", user.entry_user)
            putExtra("mofFlag", user.modify_flag)
            putExtra("entryFlag", user.entry_flag)
            putExtra("modifyUser", user.modify_user)
            putExtra("countryCode", user.countrycode)
            putExtra("altcountryCode", user.alt_countrycode)
        })
    }
    private fun fetchUserData() {
        tableLayout.removeAllViews()
        tableLayout.addView(createHeaderRow())
        val merchantData = SharedMerchantDataObj.merchantData ?: return
        ApiClient.apiService.getUserList(merchantData.merchant_user_id!!, merchantData.unit_id!!).enqueue(object : Callback<List<UserData>> {
            override fun onResponse(call: Call<List<UserData>>, response: Response<List<UserData>>) {
                NetworkUtils.hideProgress(this@Usermanagement)
                if (response.isSuccessful) {
                     userList = response.body()?.sortedByDescending { it.user_id }.orEmpty()
                    if (userList.isEmpty()) {
                        nodata.visibility = View.VISIBLE
                    } else {
                        val nextRecSrl = getNextRecSrl(userList.first().user_id)
                        usid = nextRecSrl
                        updateTable(userList)
                    }
                } else {
                    Toast.makeText(this@Usermanagement, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                //    println("Error: ${response.code()}")
                    NetworkUtils.NoInternetAlert(this@Usermanagement)
                }
            }
            override fun onFailure(call: Call<List<UserData>>, t: Throwable) {
                NetworkUtils.hideProgress(this@Usermanagement)
                NetworkUtils.NoInternetAlert(this@Usermanagement)
            }
        })
    }
    fun getNextRecSrl(currentRecSrl: String): String {
        val numericPart = currentRecSrl.takeLastWhile { it.isDigit() }
        val prefix = currentRecSrl.dropLast(numericPart.length)
        val nextNumber = (numericPart.toIntOrNull() ?: 0) + 1
        val desiredLength = numericPart.length
        val newNumericPart = nextNumber.toString().padStart(desiredLength, '0')
        return prefix + newNumericPart
    }
}
