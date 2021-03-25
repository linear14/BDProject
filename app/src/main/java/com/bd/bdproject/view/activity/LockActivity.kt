package com.bd.bdproject.view.activity

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.PasswordType
import com.bd.bdproject.R
import com.bd.bdproject.databinding.FragmentSetPasswordBinding
import com.bd.bdproject.viewmodel.PasswordViewModel
import org.koin.android.ext.android.inject

class LockActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }
    }

    lateinit var binding: FragmentSetPasswordBinding
    val viewModel: PasswordViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentSetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            tvSetPasswordTitle.text = "암호 입력"
            tvSetPasswordDescription.text = "암호를 입력해주세요"
            tvHint.visibility = View.VISIBLE
            tvHint.paintFlags = tvHint.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            viewModel.type = PasswordType.TYPE_VERIFY

            btn0.setOnClickListener { viewModel.clickButton("0") }
            btn1.setOnClickListener { viewModel.clickButton("1") }
            btn2.setOnClickListener { viewModel.clickButton("2") }
            btn3.setOnClickListener { viewModel.clickButton("3") }
            btn4.setOnClickListener { viewModel.clickButton("4") }
            btn5.setOnClickListener { viewModel.clickButton("5") }
            btn6.setOnClickListener { viewModel.clickButton("6") }
            btn7.setOnClickListener { viewModel.clickButton("7") }
            btn8.setOnClickListener { viewModel.clickButton("8") }
            btn9.setOnClickListener { viewModel.clickButton("9") }
            btnDelete.setOnClickListener { viewModel.clickButton("BACK") }
        }
        observeVerifyPassword()
    }

    private fun observeVerifyPassword() {
        viewModel.verifyPassword.observe(this) { verifyPassword ->
            binding.apply {
                val length = verifyPassword.length

                for(i in 0 until 4) {
                    when(i) {
                        in 0 until length -> {
                            (layoutDotPassword.getChildAt(i) as View).setBackgroundResource(R.drawable.deco_marker_circle_yellow)
                        }
                        else -> {
                            (layoutDotPassword.getChildAt(i) as View).setBackgroundResource(R.drawable.deco_marker_circle_gray)
                        }
                    }

                }

                if(length == 4) {
                    if(viewModel.verifyPassword()) {
                        finish()
                    } else {
                        for(i in 0 until 4) {
                            (layoutDotPassword.getChildAt(i) as View).setBackgroundResource(R.drawable.deco_marker_circle_gray)
                        }
                        Toast.makeText(this@LockActivity, "잘못된 암호입니다.", Toast.LENGTH_SHORT).show()
                        viewModel.verifyPassword.value = ""
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}