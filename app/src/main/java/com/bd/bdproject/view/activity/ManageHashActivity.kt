package com.bd.bdproject.view.activity

import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivityManageHashBinding
import com.bd.bdproject.util.dpToPx
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.FILTER_ASC
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

            actionSearch.setOnClickListener {
                manageHashViewModel.searchTag(inputSearch.text.toString())
            }

            actionReset.setOnClickListener {
                manageHashViewModel.getAllTags(FILTER_ASC)
            }

            filter.setOnClickListener {
                registerForContextMenu(it)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    it.showContextMenu(0f, 32.dpToPx().toFloat())
                } else {
                    openContextMenu(it)
                }
                unregisterForContextMenu(it)
            }

        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {

        val filterAsc = menu?.add(Menu.NONE, FILTER_ASC, 0, "오름차순")
        val filterDesc = menu?.add(Menu.NONE, FILTER_DESC, 1, "내림차순")

        filterAsc?.setOnMenuItemClickListener(filterItems)
        filterDesc?.setOnMenuItemClickListener(filterItems)
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

    private val filterItems = object: MenuItem.OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem): Boolean {
            when(item.itemId) {
                FILTER_ASC -> {
                    binding.filter.text = "오름차순"
                    manageHashViewModel.getAllTags(FILTER_ASC)
                }
                FILTER_DESC -> {
                    binding.filter.text = "내림차순"
                    manageHashViewModel.getAllTags(FILTER_DESC)
                }
            }
            return true
        }
    }

    companion object {
        const val FILTER_ASC = 2000
        const val FILTER_DESC = 2100
    }
}