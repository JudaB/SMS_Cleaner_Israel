package com.example.sms_cleaner_israel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class MainScreenFragment : Fragment() {
    
    private lateinit var readSmsButton: Button
    private lateinit var readSmsButton2: Button
    private lateinit var readAllButton: Button
    private lateinit var openSettingsButton: Button
    private lateinit var openSmsSettingsButton: Button
    private lateinit var autoDeleteLoansButton: Button
    private lateinit var autoDeleteMedicalButton: Button
    private lateinit var autoDeletePromotionsButton: Button
    private lateinit var statusText: TextView
    
    // Statistics counters
    private lateinit var spamCountText: TextView
    private lateinit var businessCountText: TextView
    private lateinit var totalCountText: TextView
    
    // Auto delete counters
    private lateinit var loansCountText: TextView
    private lateinit var medicalCountText: TextView
    private lateinit var promotionsCountText: TextView
    
    // Callbacks to MainActivity
    var onReadSmsClick: (() -> Unit)? = null
    var onReadSms2Click: (() -> Unit)? = null
    var onReadAllClick: (() -> Unit)? = null
    var onOpenSettingsClick: (() -> Unit)? = null
    var onOpenSmsSettingsClick: (() -> Unit)? = null
    var onAutoDeleteLoansClick: (() -> Unit)? = null
    var onAutoDeleteMedicalClick: (() -> Unit)? = null
    var onAutoDeletePromotionsClick: (() -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_screen, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupClickListeners()
    }
    
    private fun initializeViews(view: View) {
        readSmsButton = view.findViewById(R.id.btnReadSms)
        readSmsButton2 = view.findViewById(R.id.btnReadSms2)
        readAllButton = view.findViewById(R.id.btnReadAll)
        openSettingsButton = view.findViewById(R.id.btnOpenSettings)
        openSmsSettingsButton = view.findViewById(R.id.btnOpenSmsSettings)
        autoDeleteLoansButton = view.findViewById(R.id.btnAutoDeleteLoans)
        autoDeleteMedicalButton = view.findViewById(R.id.btnAutoDeleteMedical)
        autoDeletePromotionsButton = view.findViewById(R.id.btnAutoDeletePromotions)
        statusText = view.findViewById(R.id.tvStatus)
        
        // Initialize statistics counters
        spamCountText = view.findViewById(R.id.tvSpamCount)
        businessCountText = view.findViewById(R.id.tvBusinessCount)
        totalCountText = view.findViewById(R.id.tvTotalCount)
        
        // Initialize auto delete counters
        loansCountText = view.findViewById(R.id.tvLoansCount)
        medicalCountText = view.findViewById(R.id.tvMedicalCount)
        promotionsCountText = view.findViewById(R.id.tvPromotionsCount)
    }
    
    private fun setupClickListeners() {
        readSmsButton.setOnClickListener { onReadSmsClick?.invoke() }
        readSmsButton2.setOnClickListener { onReadSms2Click?.invoke() }
        readAllButton.setOnClickListener { onReadAllClick?.invoke() }
        openSettingsButton.setOnClickListener { onOpenSettingsClick?.invoke() }
        openSmsSettingsButton.setOnClickListener { onOpenSmsSettingsClick?.invoke() }
        autoDeleteLoansButton.setOnClickListener { onAutoDeleteLoansClick?.invoke() }
        autoDeleteMedicalButton.setOnClickListener { onAutoDeleteMedicalClick?.invoke() }
        autoDeletePromotionsButton.setOnClickListener { onAutoDeletePromotionsClick?.invoke() }
    }
    
    fun updateStatusText(text: String) {
        statusText.text = text
    }
    
    fun updateSpamCount(count: Int) {
        try {
            spamCountText.text = if (count == -1) "Spam: Error" else "Spam: $count"
        } catch (e: Exception) {
            // Fragment not ready yet
        }
    }
    
    fun updateBusinessCount(count: Int) {
        try {
            businessCountText.text = if (count == -1) "Business: Error" else "Business: $count"
        } catch (e: Exception) {
            // Fragment not ready yet
        }
    }
    
    fun updateTotalCount(count: Int) {
        try {
            totalCountText.text = if (count == -1) "Total: Error" else "Total: $count"
        } catch (e: Exception) {
            // Fragment not ready yet
        }
    }
    
    fun updateLoansCount(count: Int) {
        try {
            loansCountText.text = if (count == -1) "Loans: Error" else "Loans: $count"
        } catch (e: Exception) {
            // Fragment not ready yet
        }
    }
    
    fun updateMedicalCount(count: Int) {
        try {
            medicalCountText.text = if (count == -1) "Medical: Error" else "Medical: $count"
        } catch (e: Exception) {
            // Fragment not ready yet
        }
    }
    
    fun updatePromotionsCount(count: Int) {
        try {
            promotionsCountText.text = if (count == -1) "Promotions: Error" else "Promotions: $count"
        } catch (e: Exception) {
            // Fragment not ready yet
        }
    }
}
