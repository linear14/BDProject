package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.bd.bdproject.data.raw.descriptions
import com.bd.bdproject.databinding.ActivityHowToUseBinding
import com.bd.bdproject.view.adapter.HowToUseAdapter

class HowToUseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHowToUseBinding

    private val pageChangeCallback: ViewPager2.OnPageChangeCallback by lazy {
        object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tvDescription.text = descriptions[position].description
                binding.indicator.selectedPos = position
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHowToUseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            vpHowToUse.adapter = HowToUseAdapter()
            vpHowToUse.registerOnPageChangeCallback(pageChangeCallback)
            pageChangeCallback.onPageSelected(0)
            btnClose.setOnClickListener { finish() }
        }
    }
}