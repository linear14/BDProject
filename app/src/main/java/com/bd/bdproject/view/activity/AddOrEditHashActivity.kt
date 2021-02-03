package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bd.bdproject.databinding.ActivityAddOrEditHashBinding
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.dpToPx
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.ACTION_ADD
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.ACTION_EDIT
import com.bd.bdproject.viewmodel.ManageHashViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.lang.Exception

class AddOrEditHashActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddOrEditHashBinding

    private val manageHashViewModel: ManageHashViewModel by inject()

    var type: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                KeyboardUtil.keyBoardHide(inputTag)

                val newTag = inputTag.text.toString()

                if(newTag.isEmpty()) {
                    Toast.makeText(view.context, "태그명을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if(newTag.contains(" ")) {
                    Toast.makeText(view.context, "태그에는 공백이 들어갈 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else if(manageHashViewModel.isAlreadyExist(newTag)){
                    Toast.makeText(view.context, "이미 존재하는 태그명입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    when(type) {
                        ACTION_ADD -> {
                            runBlocking {
                                val job = GlobalScope.launch {
                                    manageHashViewModel.insertTag(inputTag.text.toString())
                                }
                                job.join()

                                if(job.isCancelled) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(view.context, "태그 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                } else if(job.isCompleted) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(view.context, "태그가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }

                                ""
                            }
                        }
                        ACTION_EDIT -> {
                            runBlocking {
                                val job = GlobalScope.launch {
                                    manageHashViewModel.editTag(intent.getStringExtra(INFO_TAG)?:throw Exception(), inputTag.text.toString())
                                }
                                job.join()

                                if(job.isCancelled) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(view.context, "태그 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                } else if(job.isCompleted) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(view.context, "태그가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }

                                ""
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