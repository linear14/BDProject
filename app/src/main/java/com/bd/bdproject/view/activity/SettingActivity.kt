package com.bd.bdproject.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivitySettingBinding
import com.bd.bdproject.util.BitDamApplication

class SettingActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnBack.setOnClickListener { onBackPressed() }
        }

        setSwitchAnimation()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            switchAnimation.isChecked = BitDamApplication.pref.isAnimationActivate
        }
    }

    private fun setSwitchAnimation() {
        binding.switchAnimation.setOnCheckedChangeListener { _, isChecked ->
            BitDamApplication.pref.isAnimationActivate = isChecked
        }
    }
}