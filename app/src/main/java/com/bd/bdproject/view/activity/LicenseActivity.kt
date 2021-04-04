package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bd.bdproject.databinding.ActivityLicenseBinding
import com.bd.bdproject.view.adapter.LicenseAdapter

class LicenseActivity : AppCompatActivity() {

    lateinit var binding: ActivityLicenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvLicense.adapter = LicenseAdapter()
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}