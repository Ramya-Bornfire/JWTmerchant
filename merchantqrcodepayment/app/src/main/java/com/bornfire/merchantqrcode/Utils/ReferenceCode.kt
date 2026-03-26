package com.bornfire.merchantqrcode.Utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ReferenceCode {
    fun getInfo(context: Context, refId: String, callback: ReferenceCodeCallback) {
        val call = ApiClient.apiService.getReferenceCode(refId)
        call.enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        callback.onSuccess(loginResponse)
                    } else {
                        callback.onError("Response body is null")
                    }
                } else {
                    Toast.makeText(context, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    callback.onError("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                callback.onError("Network error: ${t.message}")
            }
        })
    }
    fun showReferenceCodePopup(context: Context,anchorView: EditText, dataList: List<String>) {
        // Inflate the layout for the popup window
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_device_list, null)
        val editTextWidth = anchorView.width
        val popupWindow = PopupWindow(popupView, editTextWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        // Set up the ListView in the popup
        val listView = popupView.findViewById<ListView>(R.id.deviceListView)
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, dataList)
        listView.adapter = adapter
        // Handle item click in the ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = dataList[position]
            anchorView.setText(selectedItem)
            anchorView.error = null
            anchorView.clearFocus()
            popupWindow.dismiss()
        }
        // Show the popup window below the EditText
        popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.NO_GRAVITY)
    }
}
interface ReferenceCodeCallback {
    fun onSuccess(data: List<String>)
    fun onError(errorMessage: String)
}
