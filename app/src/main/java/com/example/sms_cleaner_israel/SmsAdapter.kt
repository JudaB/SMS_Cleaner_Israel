package com.example.sms_cleaner_israel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SmsAdapter(
    private val smsList: List<SmsMessage>,
    private val onDeleteClick: (SmsMessage) -> Unit,
    private val onHideClick: (SmsMessage) -> Unit
) : RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addressText: TextView = itemView.findViewById(R.id.tvAddress)
        val bodyText: TextView = itemView.findViewById(R.id.tvBody)
        val dateText: TextView = itemView.findViewById(R.id.tvDate)
        val typeText: TextView = itemView.findViewById(R.id.tvType)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
        val hideButton: Button = itemView.findViewById(R.id.btnHide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sms, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val sms = smsList[position]
        
        holder.addressText.text = "From: ${sms.address}"
        holder.bodyText.text = sms.body
        holder.dateText.text = sms.getFormattedDate()
        holder.typeText.text = sms.getTypeString()
        
        holder.deleteButton.setOnClickListener {
            onDeleteClick(sms)
        }
        
        holder.hideButton.setOnClickListener {
            onHideClick(sms)
        }
    }

    override fun getItemCount(): Int = smsList.size
}
