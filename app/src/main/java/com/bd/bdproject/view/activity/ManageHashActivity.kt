package com.bd.bdproject.view.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivityManageHashBinding
import com.bd.bdproject.view.adapter.ManageHashAdapter
import com.bd.bdproject.viewmodel.ManageHashViewModel
import org.koin.android.ext.android.inject

class ManageHashActivity : AppCompatActivity() {

    lateinit var binding: ActivityManageHashBinding

    private val manageHashViewModel: ManageHashViewModel by inject()
    val adapter = ManageHashAdapter(mutableListOf()) { resultTag ->
        binding.apply {
            manageHashViewModel.addOrRemoveCheckedTag(resultTag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageHashBinding.inflate(layoutInflater).apply {
            setContentView(root)

            rvResult.adapter = adapter
        }

        observeTag()
        observeCheckedState()

        binding.apply {
            actionCancel.setOnClickListener {
                manageHashViewModel.removeAllCheckedTags()
                adapter.removeAllCheckedPosition()
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun observeTag() {
        manageHashViewModel.tags.observe(this) { result ->
            binding.rvResult.adapter?.let {
                (it as ManageHashAdapter).tags = result
                it.notifyDataSetChanged()
            }
        }
    }

    private fun observeCheckedState() {
        manageHashViewModel.checkedTags.observe(this) { checkedStates ->
            binding.apply {
                if (checkedStates.isNullOrEmpty()) {
                    tvSelectedCnt.text = "0개 선택"
                    toolbarSub.visibility = View.GONE
                    layoutManageDetail.visibility = View.GONE
                } else {
                    tvSelectedCnt.text = "${checkedStates.size}개 선택"
                    toolbarSub.visibility = View.VISIBLE
                    layoutManageDetail.visibility = View.VISIBLE
                }
            }
        }
    }
}