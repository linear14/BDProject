package com.bd.bdproject.view.fragment

import android.app.DatePickerDialog.OnDateSetListener
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.bd.bdproject.databinding.FragmentControlDateBinding
import com.bd.bdproject.dialog.SlideDatePicker


class ControlDateFragment: BaseFragment() {

    private var _binding: FragmentControlDateBinding? = null
    val binding get() = _binding!!

    val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    /*** @flag
     *  - isFirstPressed :
     *      seekbar의 thumb를 클릭했는지 여부에 따라 값이 바뀝니다. (클릭하자마자 값이 바뀌는것을 방지)
     *
     *  - isChangingFragment :
     *      다음 화면으로 전환 애니메이션이 동작하면 true로 변합니다.
     *      true 상태에서는 추가적인 값의 조작이 불가능합니다.
     * ***/
    var isFirstPressed = true
    var isChangingFragment = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlDateBinding.inflate(inflater, container, false).apply {

        }
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        isFirstPressed = true
        isChangingFragment = false

        binding.apply {
            val params = viewThumb.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(0, 0, 0, sbLight.marginBottom)

            viewThumb.layoutParams = params
        }

        binding.viewThumb.setOnClickListener {
            val navDirection: NavDirections =
                ControlDateFragmentDirections.actionControlDateFragmentToEnrollBrightnessFragment()
            Navigation.findNavController(binding.viewThumb).navigate(navDirection)
        }

        binding.actionDatePick.setOnClickListener {
            val d: OnDateSetListener = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            }

            val picker = SlideDatePicker()
            picker.setListener(d)
            picker.show(requireActivity().supportFragmentManager, "SlideDatePicker")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}