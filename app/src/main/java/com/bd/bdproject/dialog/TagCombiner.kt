package com.bd.bdproject.dialog

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.bd.bdproject.R
import com.bd.bdproject.databinding.DialogTagCombinerBinding
import com.bd.bdproject.common.Constant.INFO_TAG
import com.bd.bdproject.view.adapter.CombineTagAdapter

class TagCombiner(val doCombined: (String) -> Unit) : DialogFragment() {

    companion object {
        const val TAG_COMBINER = "tag_combiner"
    }

    private var _binding: DialogTagCombinerBinding? = null
    val binding get() = _binding!!

    private val combineTagAdapter by lazy {
        CombineTagAdapter(tagList?.toMutableList()?: mutableListOf())
    }

    private val tagList by lazy {
        val list = arguments?.getStringArray(INFO_TAG)
        list?.sort()
        list
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTagCombinerBinding.inflate(inflater, container, false)
        isCancelable = false

        binding.apply {
            btnClose.setOnClickListener { dismiss() }
            btnConfirm.setOnClickListener {
                combineTagAdapter.newPos.value?.let { checkedPos ->
                    val tagName = combineTagAdapter.list[checkedPos]
                    doCombined(tagName)
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
            rvTags.itemAnimator = null
            rvTags.adapter = combineTagAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeNewPosition() {
        combineTagAdapter.newPos.observe(this) { newPosition ->
            if(newPosition == null) {
                binding.btnConfirm.setBackgroundResource(R.drawable.deco_confirm_button_bottom_unchecked)
            } else {
                binding.btnConfirm.setBackgroundResource(R.drawable.deco_confirm_button_bottom_checked)
            }
        }
    }


}