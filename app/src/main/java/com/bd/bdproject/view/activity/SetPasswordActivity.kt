package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bd.bdproject.databinding.ActivitySetPasswordBinding
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.viewmodel.SetPasswordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetPasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivitySetPasswordBinding
    private val viewModel: SetPasswordViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchActivatePassword.isChecked = viewModel.isActivate.value!!
        setSwitchAnimation()
    }

    private fun setSwitchAnimation() {
        binding.switchActivatePassword.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isActivate.value = isChecked
        }
    }
}