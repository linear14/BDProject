package com.bd.bdproject.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivityBitdamEditBinding

class BitdamEditActivity : AppCompatActivity() {

    lateinit var binding: ActivityBitdamEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBitdamEditBinding.inflate(layoutInflater).apply {
            setContentView(root)

            btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }
}