package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bd.bdproject.databinding.ActivitySetPasswordBinding

class SetPasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivitySetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}