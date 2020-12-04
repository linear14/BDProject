package com.bd.bdproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bd.bdproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            sbLight.thumbPlaceholderDrawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.deco_seekbar_thumb)
            sbLight.setOnProgressChangeListener {
                tvBrightness.text = (it * 5).toString()
            }
        }


    }
}