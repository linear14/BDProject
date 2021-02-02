package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.bd.bdproject.databinding.ActivityAddOrEditHashBinding
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.util.dpToPx
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.ACTION_ADD
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.ACTION_EDIT
import java.lang.Exception

class AddOrEditHashActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddOrEditHashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddOrEditHashBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        val type = intent.getIntExtra("TYPE", -1)
        when(type) {
            ACTION_ADD -> {
                binding.apply {
                    val params = tvDescription.layoutParams as ViewGroup.MarginLayoutParams
                    params.bottomMargin = 26.dpToPx()
                    tvDescription.requestLayout()

                    toolbarTitle.text = "새로운 해시태그 추가"
                    tvDescription.text = "새로운 해시태그를 입력해주세요 :)"
                    layoutPreviousTag.visibility = View.GONE
                }
            }

            ACTION_EDIT -> {
                binding.apply {
                    toolbarTitle.text = "해시태그 수정"
                    tvDescription.text = "해시태그를 수정해주세요 :)"
                    layoutPreviousTag.visibility = View.VISIBLE
                    tvPreviousTag.text = "# ${intent.getStringExtra(INFO_TAG)}"
                }
            }

            else -> {
                throw Exception()
            }
        }
    }
}