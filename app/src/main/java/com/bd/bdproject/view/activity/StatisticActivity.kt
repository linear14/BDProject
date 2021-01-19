package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bd.bdproject.R
import com.bd.bdproject.databinding.ActivityStatisticBinding

class StatisticActivity : AppCompatActivity() {

    lateinit var binding: ActivityStatisticBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatisticBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }


    }
}