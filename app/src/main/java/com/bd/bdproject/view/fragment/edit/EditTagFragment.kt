package com.bd.bdproject.view.fragment.edit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.view.activity.BitdamEditActivity
import com.bd.bdproject.view.fragment.ControlTagFragment
import com.bd.bdproject.viewmodel.common.LightTagRelationViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class EditTagFragment: ControlTagFragment() {

    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()

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
                        Toast.makeText(BitDamApplication.applicationContext(), "태그 변경이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        parentActivity.returnToDetailActivity()
                    }
                }
            }

        }

        observeTagEnrolled()
        observeTagSearched()

    }

    override fun onResume() {
        super.onResume()

        makeBackground(
            brightness = args.light?.bright ?: 0,
            tags = args.tags?: mutableListOf()
        )
        showUiWithoutAnimation()

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }

    private fun showUiWithoutAnimation() {
        binding.apply {
            actionNext.visibility = View.GONE
            actionEnroll.visibility = View.VISIBLE
            rvTagEnrolled.alpha = 1.0f
            layoutInput.alpha = 1.0f
            layoutTagRecommend.alpha = 1.0f
        }
    }

    private fun observeTagEnrolled() {
        tagViewModel.candidateTags.observe(viewLifecycleOwner) { enrolled ->
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
        tagViewModel.searchedTagNames.observe(viewLifecycleOwner) { searchedResult ->
            val brightness = args.light?.bright ?: 0

            tagRecommendAdapter.submitList(
                searchedResult.map{ Tag(it.name) }.toMutableList(),
                brightness,
                true
            )
        }
    }

}
