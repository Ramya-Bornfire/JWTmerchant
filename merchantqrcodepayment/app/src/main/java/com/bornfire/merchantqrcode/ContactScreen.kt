package com.bornfire.merchantqrcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bornfire.merchantqrcode.DataModel.Contact

class ContactScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_screen)
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title="Contact Details"
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.custom_divider)!!)
        recyclerView.addItemDecoration(divider)
        val contacts = listOf(
            Contact("Gaborone Main Mall", "+267-3188878", "+267-3188879", "gaborone@bankofbaroda.com"),
            Contact("G-West Branch Gaborone", "+267-3992705", "+267-3992721", "gwesbo@bankofbaroda.com"),
            Contact("Francis Town Branch", "+267-2413440", "+267-2413437", "francistown@bankofbaroda.com"),
            Contact("Palapye Branch", "+267-4920033", "+267-4920037", "palapye@bankofbaroda.com")
        )
        recyclerView.adapter = ContactAdapter(this, contacts)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}