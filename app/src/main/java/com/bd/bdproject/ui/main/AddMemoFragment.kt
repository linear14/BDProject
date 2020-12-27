package com.bd.bdproject.ui.main

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.bd.bdproject.BitDamApplication
import com.bd.bdproject.`interface`.JobFinishedListener
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.FragmentAddMemoBinding
import com.bd.bdproject.ui.BaseFragment
import com.bd.bdproject.ui.MainActivity
import com.bd.bdproject.ui.main.adapter.TagAdapter
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.viewmodel.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.LightViewModel
import com.bd.bdproject.viewmodel.TagViewModel
import com.bd.bdproject.viewmodel.main.AddViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class AddMemoFragment: BaseFragment() {

    private var _binding: FragmentAddMemoBinding? = null

    private val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()
    private val tagViewModel: TagViewModel by inject()
    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()
    private val sharedViewModel: AddViewModel by activityViewModels()

    private var tagEnrolledAdapter = TagAdapter()

    private var jobFinishedListener: JobFinishedListener? = null

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAddMemoBinding.inflate(inflater, container, false).apply {
            inputMemo.addTextChangedListener(InputMemoWatcher())
        }

        initBackground()
        showUi()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun insertLightWithTag() {
        runBlocking {
            binding.apply {
                val dateCode = GlobalScope.async { insertLight() }
                val tagList = GlobalScope.async { insertTag() }
                val job = GlobalScope.launch { insertRelation(dateCode.await(), tagList.await()) }

                job.join()

                // 왜 withContext로는 안되지? --> 완료된 함수 안에서만 사용?
                if(job.isCancelled) {
                    // TODO 예외 처리를 어떻게 할까? __ 이렇게 예외 처리하는건 맞는지 알아보기
                    Toast.makeText(BitDamApplication.applicationContext(), "등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(BitDamApplication.applicationContext(), "빛 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        jobFinishedListener?.onSuccess()
                    }
                }

            }
        }

    }

    private fun insertLight(): String {
        binding.apply {
            val currentTime = System.currentTimeMillis().timeToString()
            val light = Light(
                currentTime,
                tvBrightness.text.toString().toInt(),
                null
            )
            lightViewModel.asyncInsertLight(light)
            return currentTime
        }
    }

    private fun insertTag(): MutableList<Tag>? {
        binding.apply {
            tagViewModel.asyncInsertTag(tagViewModel.candidateTags.value)
        }
        return tagViewModel.candidateTags.value
    }

    private fun insertRelation(dateCode: String, tagList: MutableList<Tag>?) {
        lightTagRelationViewModel.insertRelation(dateCode, tagList)
    }

    private fun initBackground() {
        (activity as MainActivity).binding.btnDrawer.visibility = View.GONE
        (activity as MainActivity).binding.btnBack.visibility = View.VISIBLE

        val brightness = sharedViewModel.brightness.value?:0
        val tags = sharedViewModel.tags.value?: mutableListOf()

        gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
        binding.layoutAddMemo.background = gradientDrawable
        binding.tvBrightness.text = brightness.toString()
    }

    private fun showUi() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.layoutMemo.animateTransparency(1.0f, 2000)
        }
    }

    inner class InputMemoWatcher: TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.apply {
                val length = s?.length?:0

                if(length > MAX_MEMO_LENGTH) {
                    inputMemo.setText(s?.substring(0, MAX_MEMO_LENGTH))
                    inputMemo.setSelection(MAX_MEMO_LENGTH)
                    Toast.makeText(requireActivity(), "메모는 80자를 넘을 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    tvTextCount.text = "${s?.length?:0}/80자"
                }
            }
        }

    }

    companion object {
        const val MAX_MEMO_LENGTH = 80
    }

}