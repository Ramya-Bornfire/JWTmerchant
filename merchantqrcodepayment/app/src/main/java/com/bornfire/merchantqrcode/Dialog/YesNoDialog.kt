package com.bornfire.merchantqrcode.Dialog

import android.content.Context
import android.view.MenuItem
import android.widget.EditText
import android.widget.PopupMenu
import com.bornfire.merchantqrcode.R

object YesNoDialog {
    fun showYesOrNO(context: Context, anchor: EditText) {
        val popupMenu = PopupMenu(context, anchor)
        popupMenu.menuInflater.inflate(R.menu.yes_no_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.yes -> {
                    anchor.setText("Y")
                    true
                }
                R.id.no -> {
                    anchor.setText("N")
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }


}