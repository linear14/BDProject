package com.bd.bdproject.view.fragment.edit

import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.view.activity.BitdamEditActivity
import com.bd.bdproject.view.fragment.ControlMemoFragment
import com.bd.bdproject.viewmodel.common.LightViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class EditMemoFragment: ControlMemoFragment() {

    private val lightViewModel: LightViewModel by inject()

    private val args: EditMemoFragmentArgs by navArgs()

    private val parentActivity by lazy {
        activity as BitdamEditActivity
    }

    override fun onResume() {
        super.onResume()


        binding.actionEnroll.setOnClickListener {
            editMemo()
        }

        initBackground()
        showUi()

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }

    private fun editMemo() {
        KeyboardUtil.keyBoardHide(binding.inputMemo)
        runBlocking {
            val job = GlobalScope.launch {
                args.light?.let {
                    lightViewModel.editMemo(binding.inputMemo.text.toString(), it.dateCode)
                }
            }

            job.join()

            if(job.isCancelled) {
                Toast.makeText(BitDamApplication.applicationContext(), "메모 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(BitDamApplication.applicationContext(), "메모 변경이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    parentActivity.returnToDetailActivity()
                }
            }
        }


    }

    private fun initBackground() {
        binding.apply {
            val brightness: Int = args.light?.bright ?: 0
            val memo: String? = args.light?.memo

            setEntireMemoFragmentColor(brightness)

            gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
            layoutAddMemo.background = gradientDrawable
            inputMemo.setText(memo)
        }
    }

    private fun showUi() {
        binding.layoutMemo.alpha = 1.0f
        binding.tvBrightness.visibility = View.GONE
    }
}