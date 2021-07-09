package com.bd.bdproject.view.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bd.bdproject.databinding.ActivityAddOrEditHashBinding
import com.bd.bdproject.common.Constant.INFO_TAG
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.common.dpToPx
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.ACTION_ADD
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.ACTION_EDIT
import com.bd.bdproject.viewmodel.ManageHashViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Exception

class AddOrEditHashActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddOrEditHashBinding

    private val manageHashViewModel: ManageHashViewModel by viewModel()

    var type: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manageHashViewModel

        binding = ActivityAddOrEditHashBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        type = intent.getIntExtra("TYPE", -1)

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

        binding.apply {
            actionConfirm.setOnClickListener { view ->
                val newTag = inputTag.text.toString()

                if(newTag.isEmpty()) {
                    Toast.makeText(view.context, "태그명을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if(newTag.contains(" ")) {
                    Toast.makeText(view.context, "태그에는 공백이 들어갈 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else if(manageHashViewModel.isAlreadyExist(newTag)){
                    Toast.makeText(view.context, "이미 존재하는 태그명입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    KeyboardUtil.keyBoardHide(binding.inputTag)
                    CoroutineScope(Dispatchers.IO).launch {
                        when(type) {
                            ACTION_ADD -> {
                                launch {
                                    manageHashViewModel.insertTag(inputTag.text.toString())
                                }.join()
                                launch(Dispatchers.Main) {
                                    val resultIntent = Intent().apply {
                                        putExtra("TYPE", ACTION_ADD)
                                    }
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    finish()
                                }
                            }
                            ACTION_EDIT -> {
                                launch {
                                    manageHashViewModel.editTag(intent.getStringExtra(INFO_TAG)?:throw Exception(), inputTag.text.toString())
                                }.join()
                                launch(Dispatchers.Main) {
                                    val resultIntent = Intent().apply {
                                        putExtra("TYPE", ACTION_EDIT)
                                    }
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    finish()
                                }
                            }
                        }
                    }
                }
            }

            btnBack.setOnClickListener { finish() }
        }
    }

    override fun onResume() {
        super.onResume()

        manageHashViewModel.searchTag(null)
    }
}