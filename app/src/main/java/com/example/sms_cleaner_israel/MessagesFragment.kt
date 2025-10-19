package com.example.sms_cleaner_israel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MessagesFragment : Fragment() {
    
    private lateinit var smsRecyclerView: RecyclerView
    private lateinit var smsAdapter: SmsAdapter
    
    // This will be set by MainActivity
    var smsList: MutableList<SmsMessage> = mutableListOf()
    var onDeleteClick: ((SmsMessage) -> Unit)? = null
    var onHideClick: ((SmsMessage) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messages_screen, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        smsRecyclerView = view.findViewById(R.id.recyclerViewSms)
        setupRecyclerView()
    }
    
    private fun setupRecyclerView() {
        smsAdapter = SmsAdapter(
            smsList,
            onDeleteClick = { smsMessage ->
                onDeleteClick?.invoke(smsMessage)
            },
            onHideClick = { smsMessage ->
                onHideClick?.invoke(smsMessage)
            }
        )
        smsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        smsRecyclerView.adapter = smsAdapter
    }
    
    fun updateMessages(newSmsList: MutableList<SmsMessage>) {
        smsList.clear()
        smsList.addAll(newSmsList)
        smsAdapter.notifyDataSetChanged()
    }
    
    fun setClickListeners(
        onDelete: (SmsMessage) -> Unit,
        onHide: (SmsMessage) -> Unit
    ) {
        onDeleteClick = onDelete
        onHideClick = onHide
    }
}
