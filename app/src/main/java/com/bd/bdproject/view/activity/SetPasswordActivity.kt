package com.bd.bdproject.view.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bd.bdproject.databinding.ActivitySetPasswordBinding
import com.bd.bdproject.common.BitDamApplication
import com.bd.bdproject.viewmodel.PasswordViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetPasswordActivity : AppCompatActivity() {

    companion object {
        const val SET_PASSWORD_SUCCESS = 4000
        const val SET_PASSWORD_FAILED = 4100
    }

    lateinit var binding: ActivitySetPasswordBinding
    private val viewModel: PasswordViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            switchActivatePassword.isChecked = viewModel.isActivate.value!!
            actionConfirm.setOnClickListener { savePassword() }
            btnBack.setOnClickListener { finish() }
        }

        setSwitchAnimation()
    }

    private fun setSwitchAnimation() {
        binding.switchActivatePassword.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isActivate.value = isChecked

            if(!isChecked) {
                Snackbar.make(binding.root, "암호가 해제되었습니다.", Snackbar.LENGTH_SHORT).show()
                BitDamApplication.pref.bitdamPassword = null
                BitDamApplication.pref.passwordHint = null
            }
        }
    }

    private fun savePassword() {
        if(viewModel.confirmPassword.value?.length != 4 || viewModel.confirmPassword.value == null) {
            val resultIntent = Intent().apply {
                putExtra("TYPE", SET_PASSWORD_FAILED)
            }
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            BitDamApplication.pref.bitdamPassword = viewModel.confirmPassword.value
            BitDamApplication.pref.passwordHint = viewModel.passwordHint
            val resultIntent = Intent().apply {
                putExtra("TYPE", SET_PASSWORD_SUCCESS)
            }
            setResult(Activity.RESULT_OK, resultIntent)
        }
        finish()
    }
}