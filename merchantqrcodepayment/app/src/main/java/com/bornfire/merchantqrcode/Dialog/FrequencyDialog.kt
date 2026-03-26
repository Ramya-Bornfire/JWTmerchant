package com.bornfire.merchantqrcode.Dialog

import android.content.Context
import android.view.MenuItem
import android.widget.EditText
import android.widget.PopupMenu
import com.bornfire.merchantqrcode.R

object FrequencyDialog {
     fun showfrequencey(context: Context, anchor: EditText) {
        val popupMenu = PopupMenu(context, anchor)
        popupMenu.menuInflater.inflate(R.menu.select_time, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.item1 -> {
                    // Handle selection
                    anchor.setText("5 sec")
                    true
                }
                R.id.item2 -> {
                    // Handle selection
                    anchor.setText("10 sec")
                    true
                }
                R.id.item3 -> {
                    // Handle selection
                    anchor.setText("30sec")
                    true
                }
                R.id.item4 -> {
                    // Handle selection
                    anchor.setText("1 minute")
                    true
                }
                R.id.item5 -> {
                    // Handle selection
                    anchor.setText("5 minutes")
                    true
                }
                R.id.item6 -> {
                    // Handle selection
                    anchor.setText("30 minutes")
                    true
                }
                R.id.item7 -> {
                    // Handle selection
                    anchor.setText("1 hour")
                    true
                }
                R.id.item8 -> {
                    // Handle selection
                    anchor.setText("2 hours")
                    true
                }
                // Add more items as needed
                else -> false
            }
        }
        popupMenu.show()
    }
}