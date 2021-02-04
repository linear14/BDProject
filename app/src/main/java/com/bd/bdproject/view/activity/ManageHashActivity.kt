package com.bd.bdproject.view.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.`interface`.OnAsyncWorkFinished
import com.bd.bdproject.`interface`.OnBottomOptionSelectedListener
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.ActivityManageHashBinding
import com.bd.bdproject.dialog.BottomSelector
import com.bd.bdproject.dialog.TagCombiner
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.dpToPx
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.FILTER_ASC
import com.bd.bdproject.view.adapter.ManageHashAdapter
import com.bd.bdproject.viewmodel.ManageHashViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.lang.Exception
import java.security.Key

// TODO Database 작업 완료 후 리스너 설정을 다른 방식으로 진행하는 방안 고려
class ManageHashActivity : AppCompatActivity() {

    lateinit var binding: ActivityManageHashBinding

    private val manageHashViewModel: ManageHashViewModel by inject()

    val adapter = ManageHashAdapter(
        mutableListOf(),
        checkBoxClickedListener = { resultTag ->
            binding.apply {
                manageHashViewModel.addOrRemoveCheckedTag(resultTag)
            }},
        bottomSelectorClickedListener = { tag ->
            val bottomSelector = BottomSelector(tag).apply {
                setOnSelectedListener(object: OnBottomOptionSelectedListener {
                    override fun onEdit(tag: Tag) {
                        dismiss()

                        startActivity(Intent(this@ManageHashActivity, AddOrEditHashActivity::class.java).apply {
                            putExtra("TYPE", ACTION_EDIT)
                            putExtra(INFO_TAG, tag.name)
                        })
                    }

                    override fun onDelete(tag: Tag) {
                        dismiss()

                        manageHashViewModel.setOnAsyncWorkFinishedListener(object: OnAsyncWorkFinished {
                            override fun onSuccess() {
                                Toast.makeText(this@ManageHashActivity, "태그 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            }

                            override fun onFailure() {
                                Toast.makeText(this@ManageHashActivity, "태그 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            }

                        })
                        manageHashViewModel.deleteTags(listOf(tag))
                    }

                })
            }
            bottomSelector.show(supportFragmentManager, "selector")
        })

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
                manageHashViewModel.searchedText.value = inputSearch.text.toString()
                manageHashViewModel.searchTag(manageHashViewModel.searchedText.value)
                KeyboardUtil.keyBoardHide(it)
                it.clearFocus()
            }

            actionReset.setOnClickListener {
                binding.inputSearch.text.clear()
                manageHashViewModel.searchedText.value = null
                manageHashViewModel.searchTag(manageHashViewModel.searchedText.value)
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

            actionDelete.setOnClickListener {
                manageHashViewModel.setOnAsyncWorkFinishedListener(object: OnAsyncWorkFinished {
                    override fun onSuccess() {
                        Toast.makeText(this@ManageHashActivity, "선택된 태그 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure() {
                        Toast.makeText(this@ManageHashActivity, "선택된 태그 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    }

                })
                manageHashViewModel.checkedTags.value?.let {
                    manageHashViewModel.deleteTags(it.toList().map { it.tag })
                    manageHashViewModel.removeAllCheckedTags()
                }
            }

            actionAdd.setOnClickListener {
                startActivity(Intent(this@ManageHashActivity, AddOrEditHashActivity::class.java).apply {
                    putExtra("TYPE", ACTION_ADD)
                })
            }

            actionCombine.setOnClickListener {

                val tagNameBundle = Bundle().apply {
                    putStringArray(INFO_TAG, manageHashViewModel.checkedTags.value?.map { it.name }?.toTypedArray())
                }

                val combiner = TagCombiner { combinedTo ->
                    runBlocking {
                        val job = GlobalScope.launch {
                            manageHashViewModel.combineTags(combinedTo)
                        }
                        job.join()

                        if(job.isCancelled) {
                            GlobalScope.launch(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "태그 합치기에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else if(job.isCompleted) {
                            GlobalScope.launch(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "태그 합치기가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                combiner.arguments = tagNameBundle
                combiner.show(supportFragmentManager, TagCombiner.TAG_COMBINER)
            }

            btnBack.setOnClickListener { finish() }
        }
    }

    override fun onResume() {
        super.onResume()

        manageHashViewModel.searchTag(manageHashViewModel.searchedText.value)
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
                    manageHashViewModel.filter.value = FILTER_ASC
                    manageHashViewModel.searchTag(binding.inputSearch.text.toString())
                }
                FILTER_DESC -> {
                    binding.filter.text = "내림차순"
                    manageHashViewModel.filter.value = FILTER_DESC
                    manageHashViewModel.searchTag(binding.inputSearch.text.toString())
                }
            }
            return true
        }
    }

    companion object {
        const val FILTER_ASC = 2000
        const val FILTER_DESC = 2100

        const val ACTION_ADD = 3000
        const val ACTION_EDIT = 3100
        const val ACTION_DELETE = 3200
    }
}