package com.bd.bdproject.view.fragment.edit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.util.Constant.CONTROL_TAG
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.view.activity.BitdamEditActivity
import com.bd.bdproject.view.fragment.ControlTagFragment
import com.bd.bdproject.viewmodel.AddViewModel
import com.bd.bdproject.viewmodel.common.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.common.TagViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class EditTagFragment: ControlTagFragment() {

    private val tagViewModel: TagViewModel by inject()
    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()
    private val sharedViewModel: AddViewModel by activityViewModels()

    private val args: EditTagFragmentArgs by navArgs()

    private val parentActivity by lazy {
        activity as BitdamEditActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.actionEnroll.setOnClickListener {
            KeyboardUtil.keyBoardHide(binding.inputTag)
            runBlocking {
                val tagList = GlobalScope.async { tagViewModel.asyncInsertTag(tagViewModel.candidateTags.value) }
                val job = GlobalScope.launch {
                    args.light?.let {
                        lightTagRelationViewModel.updateRelationsAll(it.dateCode, tagList.await())
                    } }

                job.join()

                if(job.isCancelled) {
                    Toast.makeText(BitDamApplication.applicationContext(), "태그 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        sharedViewModel.previousPage.value = CONTROL_TAG
                        Toast.makeText(BitDamApplication.applicationContext(), "태그 변경이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                        // 디테일 화면으로 다시 이동
                    }
                }
            }

        }

    }

    override fun onResume() {
        super.onResume()

        initBackground()
        showUi()
        observeTagEnrolled()
        observeTagSearched()

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }

    private fun initBackground() {
        val brightness: Int = args.light?.bright ?: 0
        val tags: List<Tag> = args.tags ?: mutableListOf()

        setEntireTagFragmentColor(brightness)
        gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)

        binding.layoutAddTag.background = gradientDrawable
        tagViewModel.candidateTags.value = tags.toMutableList()
        binding.tvBrightness.text = brightness.toString()
    }

    private fun showUi() {
        binding.apply {
            actionNext.visibility = View.GONE
            actionEnroll.visibility = View.VISIBLE
            rvTagEnrolled.alpha = 1.0f
            layoutInput.alpha = 1.0f
            layoutTagRecommend.alpha = 1.0f
        }
    }

    private fun observeTagEnrolled() {
        tagViewModel.candidateTags.observe(requireActivity()) { enrolled ->
            tagEnrolledAdapter.apply {
                if(isEditMode) {
                    binding.rvTagEnrolled.itemAnimator = null
                    isEditMode = false
                    editModeTag = null
                } else {
                    binding.rvTagEnrolled.itemAnimator = DefaultItemAnimator()
                }

                val brightness = args.light?.bright ?: 0
                submitList(enrolled.toMutableList(), brightness)
            }
        }
    }

    private fun observeTagSearched() {
        tagViewModel.searchedTagNames.observe(requireActivity()) { searchedResult ->
            val brightness = args.light?.bright ?: 0

            tagRecommendAdapter.submitList(
                searchedResult.map{ Tag(it) }.toMutableList(),
                brightness,
                true
            )
        }
    }

}
