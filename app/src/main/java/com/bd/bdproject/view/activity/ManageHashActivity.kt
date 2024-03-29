package com.bd.bdproject.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bd.bdproject.R
import com.bd.bdproject.`interface`.OnAsyncWorkFinished
import com.bd.bdproject.`interface`.OnBottomOptionSelectedListener
import com.bd.bdproject.common.BitDamApplication
import com.bd.bdproject.common.Constant.INFO_TAG
import com.bd.bdproject.common.dpToPx
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.ActivityManageHashBinding
import com.bd.bdproject.dialog.BitdamDialog
import com.bd.bdproject.dialog.BottomSelector
import com.bd.bdproject.dialog.TagCombiner
import com.bd.bdproject.view.adapter.ManageHashAdapter
import com.bd.bdproject.viewmodel.ManageHashViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

// TODO Database 작업 완료 후 리스너 설정을 다른 방식으로 진행하는 방안 고려
class ManageHashActivity : AppCompatActivity() {

    lateinit var binding: ActivityManageHashBinding

    private val manageHashViewModel: ManageHashViewModel by viewModel()
    var searchJob: Job? = null
    private var bottomSelector: BottomSelector? = null
    private var deleteDialog: BitdamDialog? = null

    val adapter = ManageHashAdapter(
        mutableListOf(),
        checkBoxClickedListener = { resultTag ->
            binding.apply {
                manageHashViewModel.addOrRemoveCheckedTag(resultTag)
            }},
        bottomSelectorClickedListener = { tag ->
            binding.actionCancel.performClick()
            bottomSelector = BottomSelector(tag).apply {
                setOnSelectedListener(object: OnBottomOptionSelectedListener {
                    override fun onEdit(tag: Tag) {
                        dismiss()

                        val intent = Intent(this@ManageHashActivity, AddOrEditHashActivity::class.java).apply {
                            putExtra("TYPE", ACTION_EDIT)
                            putExtra(INFO_TAG, tag.name)
                        }
                        showSnackBarForResult.launch(intent)
                    }

                    override fun onDelete(tag: Tag) {
                        dismiss()

                        manageHashViewModel.setOnAsyncWorkFinishedListener(object: OnAsyncWorkFinished {
                            override fun onSuccess() {
                                Snackbar.make(binding.root, "태그 삭제가 완료되었습니다.", Snackbar.LENGTH_SHORT).show()
                            }

                            override fun onFailure() {
                                Snackbar.make(binding.root, "태그 삭제에 실패하였습니다.", Snackbar.LENGTH_SHORT).show()
                            }

                        })

                        deleteDialog = BitdamDialog("정말 [${tag.name}]태그를 삭제하실 건가요?") {
                            manageHashViewModel.deleteTags(listOf(tag))
                        }
                        deleteDialog?.show(supportFragmentManager, "delete_one_tag")
                    }

                })
            }
            bottomSelector?.show(supportFragmentManager, "selector")
        })

    private val showSnackBarForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getIntExtra("TYPE", -1)
            data?.let {
                when(data) {
                    ACTION_ADD -> {
                        Snackbar.make(binding.root, "태그가 추가되었습니다.", Snackbar.LENGTH_SHORT).show()
                    }
                    ACTION_EDIT -> {
                        Snackbar.make(binding.root, "태그가 수정되었습니다.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val textWatcher = object: TextWatcher {
        private var text: String? = null
        private var beforeCursorPosition = 0

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchJob?.cancel()
            if(s.isNullOrEmpty() || s.isBlank()) {
                binding.actionReset.visibility = View.INVISIBLE
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    manageHashViewModel.searchedText.value = null
                }
            } else {
                binding.actionReset.visibility = View.VISIBLE
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    manageHashViewModel.searchedText.value = s.toString()
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            text = s.toString()
            beforeCursorPosition = start
        }

        override fun afterTextChanged(s: Editable?) {
            binding.inputSearch.also {
                it.removeTextChangedListener(this)
                if (it.lineCount > 1) {
                    it.setText(text)
                    it.setSelection(beforeCursorPosition)
                }
                it.addTextChangedListener(this)
            }
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
        observeSearchedText()
        showHelper()

        binding.apply {
            actionCancel.setOnClickListener {
                manageHashViewModel.removeAllCheckedTags()
                adapter.removeAllCheckedPosition()
            }

            actionReset.setOnClickListener {
                searchJob?.cancel()
                binding.inputSearch.text.clear()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    manageHashViewModel.searchedText.value = null
                }
            }

            inputSearch.addTextChangedListener(textWatcher)

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
                        Snackbar.make(binding.root, "선택된 태그 삭제가 완료되었습니다.", Snackbar.LENGTH_SHORT).show()
                    }

                    override fun onFailure() {
                        Snackbar.make(binding.root, "선택된 태그 삭제에 실패하였습니다.", Snackbar.LENGTH_SHORT).show()
                    }

                })
                manageHashViewModel.checkedTags.value?.let {
                    deleteDialog = BitdamDialog("정말 ${it.size}개의 해시태그를 삭제하실 건가요?") {
                        manageHashViewModel.deleteTags(it.toList().map { it.tag })
                        manageHashViewModel.removeAllCheckedTags()
                    }
                    deleteDialog?.show(supportFragmentManager, "delete_hash")
                }
            }

            actionAdd.setOnClickListener {
                val intent = Intent(this@ManageHashActivity, AddOrEditHashActivity::class.java).apply {
                    putExtra("TYPE", ACTION_ADD)
                }
                showSnackBarForResult.launch(intent)
            }

            actionCombine.setOnClickListener {
                val tagNameBundle = Bundle().apply {
                    putStringArray(INFO_TAG, manageHashViewModel.checkedTags.value?.map { it.tag.name }?.toTypedArray())
                }
                val combiner = TagCombiner { combinedTo ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val job = launch {
                            manageHashViewModel.combineTags(combinedTo)
                        }
                        job.join()

                        if(job.isCancelled) {
                            launch(Dispatchers.Main) {
                                Snackbar.make(binding.root, "태그 합치기에 실패하였습니다.", Snackbar.LENGTH_SHORT).show()
                            }
                        } else if(job.isCompleted) {
                            searchJob?.cancel()
                            searchJob = launch {
                                launch(Dispatchers.Main) {
                                    Snackbar.make(binding.root, "태그 합치기가 완료되었습니다.", Snackbar.LENGTH_SHORT).show()
                                }
                                manageHashViewModel.searchTag(manageHashViewModel.searchedText.value)
                            }
                        }

                        launch(Dispatchers.Main) {
                            manageHashViewModel.removeAllCheckedTags()
                            adapter.removeAllCheckedPosition()
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
        Log.d("LIFECYCLE_TEST", "onResumed Clicked")
        if(binding.inputSearch.text.isNullOrEmpty() || binding.inputSearch.text.isBlank()) {
            binding.actionReset.visibility = View.INVISIBLE
        } else {
            binding.actionReset.visibility = View.VISIBLE
        }
        searchJob?.cancel()
        searchJob = CoroutineScope(Dispatchers.IO).launch {
            manageHashViewModel.searchTag(manageHashViewModel.searchedText.value)
        }
    }

    override fun onStop() {
        super.onStop()
        bottomSelector?.dismissAllowingStateLoss()
        deleteDialog?.dismissAllowingStateLoss()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun showHelper() {
        if(BitDamApplication.pref.firstInManageHash) {
            binding.helper.tvHelper.setText(R.string.helper_manage_hash)
            binding.helper.root.setOnTouchListener() { _, _ -> true }
            binding.helper.root.visibility = View.VISIBLE
            BitDamApplication.pref.firstInManageHash = false
        }
    }

    private fun observeTag() {
        manageHashViewModel.tags.observe(this) { result ->
            binding.tvTotalCount.text = "총 ${result.size}개"
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

    private fun observeSearchedText() {
        manageHashViewModel.searchedText.observe(this) { txt ->
            manageHashViewModel.searchTag(txt)
        }
    }

    private val filterItems = object: MenuItem.OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem): Boolean {
            when(item.itemId) {
                FILTER_ASC -> {
                    binding.filter.text = "오름차순"
                    manageHashViewModel.filter.value = FILTER_ASC
                    searchJob?.cancel()
                    searchJob = CoroutineScope(Dispatchers.IO).launch {
                        manageHashViewModel.searchTag(manageHashViewModel.searchedText.value)
                    }
                }
                FILTER_DESC -> {
                    binding.filter.text = "내림차순"
                    manageHashViewModel.filter.value = FILTER_DESC
                    searchJob?.cancel()
                    searchJob = CoroutineScope(Dispatchers.IO).launch {
                        manageHashViewModel.searchTag(manageHashViewModel.searchedText.value)
                    }
                }
            }
            return true
        }
    }

    override fun onBackPressed() {
        if(binding.helper.root.isVisible) {
            binding.helper.root.visibility = View.GONE
            return
        }
        super.onBackPressed()
    }

    companion object {
        const val FILTER_ASC = 2000
        const val FILTER_DESC = 2100

        const val ACTION_ADD = 3000
        const val ACTION_EDIT = 3100
        const val ACTION_DELETE = 3200
    }
}