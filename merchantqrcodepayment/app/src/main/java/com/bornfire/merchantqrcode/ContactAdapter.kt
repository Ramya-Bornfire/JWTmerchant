package com.bornfire.merchantqrcode

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bornfire.merchantqrcode.DataModel.Contact

class ContactAdapter(private val context: Context, private val contactList: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleTextView)
        val phone: TextView = view.findViewById(R.id.phoneTextView)
        val fax: TextView = view.findViewById(R.id.faxTextView)
        val email: TextView = view.findViewById(R.id.emailTextView)
        val callIcon: ImageView = view.findViewById(R.id.callIcon)
        val callFaxIcon:ImageView=view.findViewById(R.id.callFaxIcon)
        val emailIcon: ImageView = view.findViewById(R.id.emailIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]

        holder.title.text = contact.title
        holder.phone.text = "Phone: ${contact.phoneNumber}"
        holder.fax.text = "Fax: ${contact.faxNumber}"
        holder.email.text = "Email: ${contact.email}"

        // Open Dialer on Call Icon Click
        holder.callIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))
            context.startActivity(intent)
        }
        holder.callFaxIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.faxNumber}"))
            context.startActivity(intent)
        }


        // Open Email App on Email Icon Click
        holder.emailIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${contact.email}")
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = contactList.size
}
