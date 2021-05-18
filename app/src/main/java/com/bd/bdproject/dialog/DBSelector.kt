package com.bd.bdproject.dialog

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.bd.bdproject.R
import com.bd.bdproject.data.model.DBInfo
import com.bd.bdproject.databinding.DialogDbSelectorBinding
import com.bd.bdproject.databinding.DialogTagCombinerBinding
import com.bd.bdproject.util.Constant.INFO_DB
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.view.adapter.CombineTagAdapter
import com.bd.bdproject.view.adapter.DBAdapter

class DBSelector(val onIdSelected: (String, DialogFragment) -> Unit) : DialogFragment() {

    companion object {
        const val DB_SELECTOR = "db_selector"
    }

    private var _binding: DialogDbSelectorBinding? = null
    val binding get() = _binding!!

    private val dbAdapter by lazy {
        DBAdapter(dbList?.toList()?: mutableListOf())
    }

    private val dbList by lazy {
        arguments?.getParcelableArrayList<DBInfo>(INFO_DB)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDbSelectorBinding.inflate(inflater, container, false)
        isCancelable = false

        binding.apply {
            btnClose.setOnClickListener { dismiss() }
            btnConfirm.setOnClickListener {
                dbAdapter.newPos.value?.let { checkedPos ->
                    val dbId = dbAdapter.list[checkedPos].id
                    onIdSelected(dbId, this@DBSelector)
                    dismiss()
                }
            }
        }

        observeNewPosition()

        dialog?.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // window의 width/height 얻어내기
        val windowWidth = Resources.getSystem().displayMetrics.widthPixels
        val windowHeight = Resources.getSystem().displayMetrics.heightPixels

        /*val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val windowWidth = displayMetrics.widthPixels*/

        // dialog의 높이 고정시키기
        val params = dialog?.window?.attributes
        params?.let {
            it.width = (windowWidth * 0.666).toInt()
            it.height = (windowHeight * 0.574).toInt()
        }
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        // end

        binding.apply {
            rvDbs.itemAnimator = null
            rvDbs.adapter = dbAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeNewPosition() {
        dbAdapter.newPos.observe(this) { newPosition ->
            if(newPosition == null) {
                binding.btnConfirm.setBackgroundResource(R.drawable.deco_confirm_button_bottom_unchecked)
            } else {
                binding.btnConfirm.setBackgroundResource(R.drawable.deco_confirm_button_bottom_checked)
            }
        }
    }


}