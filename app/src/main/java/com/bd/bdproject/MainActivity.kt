package com.bd.bdproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bd.bdproject.databinding.ActivityMainBinding
import com.bd.bdproject.light.AddLightFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            supportFragmentManager.beginTransaction().add(R.id.layout_frame, AddLightFragment()).commit()
        }


    }
}