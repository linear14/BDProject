package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bd.bdproject.databinding.ActivitySetPasswordBinding
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.viewmodel.PasswordViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetPasswordActivity : AppCompatActivity() {

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
            Toast.makeText(this, "설정 오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        } else {
            BitDamApplication.pref.bitdamPassword = viewModel.confirmPassword.value
            BitDamApplication.pref.passwordHint = viewModel.passwordHint
            Toast.makeText(this, "암호 설정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}