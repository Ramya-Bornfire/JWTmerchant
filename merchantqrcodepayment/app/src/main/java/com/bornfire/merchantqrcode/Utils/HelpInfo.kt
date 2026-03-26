package com.bornfire.merchantqrcode.Utils

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bornfire.merchantqrcode.DataModel.HelpInfoData
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object HelpInfo {
    fun getInfo(context: Context, screenId: String) {
        val call = ApiClient.apiService.getHelpInfoLimit(screenId)
        call.enqueue(object : Callback<HelpInfoData> {
            override fun onResponse(call: Call<HelpInfoData>, response: Response<HelpInfoData>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val builder = AlertDialog.Builder(context)
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.activity_help_screen, null)
                    builder.setView(view)
                    val alertDialog = builder.create()
                    alertDialog.setCancelable(true)
                    alertDialog.show()
                    val window = alertDialog.window
                    val layoutParams = window?.attributes
                    layoutParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
                    layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
                    window?.attributes = layoutParams
                    val decorView = window?.decorView
                    decorView?.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent_background))
                    val cls_img = view.findViewById<ImageView>(R.id.cls_img)
                    cls_img.setOnClickListener(){
                        alertDialog.dismiss()
                    }

                    val okButton = view.findViewById<Button>(R.id.ok_button)  // Ensure this ID matches your layout
                    okButton.setOnClickListener {
                        alertDialog.dismiss()
                    }
                    // Find and populate TextViews with data
                    val textViews = listOf(
                        view.findViewById<TextView>(R.id.desc1),
                        view.findViewById<TextView>(R.id.desc2),
                        view.findViewById<TextView>(R.id.desc3),
                        view.findViewById<TextView>(R.id.desc4),
                        view.findViewById<TextView>(R.id.desc5),
                        view.findViewById<TextView>(R.id.desc6),
                        view.findViewById<TextView>(R.id.desc7),
                        view.findViewById<TextView>(R.id.desc8),
                        view.findViewById<TextView>(R.id.desc9),
                        view.findViewById<TextView>(R.id.desc10)
                    )
                    val descData = listOf(
                        loginResponse?.desc_data1,
                        loginResponse?.desc_data2,
                        loginResponse?.desc_data3,
                        loginResponse?.desc_data4,
                        loginResponse?.desc_data5,
                        loginResponse?.desc_data6,
                        loginResponse?.desc_data7,
                        loginResponse?.desc_data8,
                        loginResponse?.desc_data9,
                        loginResponse?.desc_data10
                    )
                    textViews.zip(descData).forEach { (textView, data) ->
                        if (data != null) {
                            textView.text = data
                            textView.visibility = View.VISIBLE
                        } else {
                            textView.visibility = View.GONE
                        }
                    }
                } else {
                    Toast.makeText(context, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    Log.d("HelpInfo", "Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<HelpInfoData>, t: Throwable) {
                Log.d("HelpInfo", "Network error: ${t.message}")
            }
        })
    }
}
