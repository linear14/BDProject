package com.bd.bdproject.view.fragment.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.bd.bdproject.PasswordType
import com.bd.bdproject.R
import com.bd.bdproject.databinding.FragmentSetPasswordBinding
import com.bd.bdproject.common.BitDamApplication
import com.bd.bdproject.viewmodel.PasswordViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SetPasswordFragment: Fragment() {

    var _binding: FragmentSetPasswordBinding? = null
    val binding get() = _binding!!

    val viewModel: PasswordViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetPasswordBinding.inflate(inflater, container, false).apply {
            btn0.setOnClickListener { viewModel.clickButton("0") }
            btn1.setOnClickListener { viewModel.clickButton("1") }
            btn2.setOnClickListener { viewModel.clickButton("2") }
            btn3.setOnClickListener { viewModel.clickButton("3") }
            btn4.setOnClickListener { viewModel.clickButton("4") }
            btn5.setOnClickListener { viewModel.clickButton("5") }
            btn6.setOnClickListener { viewModel.clickButton("6") }
            btn7.setOnClickListener { viewModel.clickButton("7") }
            btn8.setOnClickListener { viewModel.clickButton("8") }
            btn9.setOnClickListener { viewModel.clickButton("9") }
            btnDelete.setOnClickListener { viewModel.clickButton("BACK") }
        }
        setScreenState()
        observeActivateSwitch()
        observeTempPassword()
        observeConfirmPassword()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun screenLock() {
        binding.apply {
            layoutInputPassword.visibility = View.INVISIBLE
            tvSetPasswordTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_gray))
            tvSetPasswordDescription.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_gray))
        }
    }

    private fun screenUnLock() {
        binding.apply {
            layoutInputPassword.visibility = View.VISIBLE
            tvSetPasswordTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvSetPasswordDescription.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun setScreenState() {
        val isCurrentHavePassword = BitDamApplication.pref.bitdamPassword != null

        if(isCurrentHavePassword) {
            setScreenUpdatePassword()
        } else {
            setScreenInsertPassword()
        }
    }

    private fun setScreenInsertPassword() {
        binding.apply {
            tvSetPasswordTitle.text = "암호 입력"
            tvSetPasswordDescription.text = "설정할 암호를 입력해주세요"
        }
    }

    private fun setScreenConfirmPassword() {
        binding.apply {
            tvSetPasswordTitle.text = "암호 확인"
            tvSetPasswordDescription.text = "한번 더 입력해주세요"
        }
    }

    private fun setScreenUpdatePassword() {
        binding.apply {
            tvSetPasswordTitle.text = "암호 변경"
            tvSetPasswordDescription.text = "변경할 새로운 암호를 입력해 주세요"
        }
    }

    private fun observeActivateSwitch() {
        viewModel.isActivate.observe(viewLifecycleOwner) {
            when(it) {
                true -> {
                    screenUnLock()
                }
                false -> {
                    screenLock()
                }
            }
        }
    }

    private fun observeTempPassword() {
        viewModel.tempPassword.observe(viewLifecycleOwner) { tempPassword ->
            binding.apply {
                val length = tempPassword.length

                for(i in 0 until 4) {
                    when(i) {
                        in 0 until length -> {
                            (layoutDotPassword.getChildAt(i) as View).setBackgroundResource(R.drawable.deco_marker_circle_yellow)
                        }
                        else -> {
                            (layoutDotPassword.getChildAt(i) as View).setBackgroundResource(R.drawable.deco_marker_circle_gray)
                        }
                    }

                }

                if(length == 4) {
                    viewModel.type = PasswordType.TYPE_CONFIRM
                    setScreenConfirmPassword()
                    for(i in 0 until 4) {
                        (layoutDotPassword.getChildAt(i) as View).setBackgroundResource(R.drawable.deco_marker_circle_gray)
                    }
                }
            }
        }
    }

    private fun observeConfirmPassword() {
        viewModel.confirmPassword.observe(viewLifecycleOwner) { confirmPassword ->
            binding.apply {
                val length = confirmPassword.length

                for(i in 0 until 4) {
                    when(i) {
                        in 0 until length -> {
                            (layoutDotPassword.getChildAt(i) as View).setBackgroundResource(R.drawable.deco_marker_circle_yellow)
                        }
                        else -> {
                            (layoutDotPassword.getChildAt(i) as View).setBackgroundResource(R.drawable.deco_marker_circle_gray)
                        }
                    }

                }

                if(length == 4) {
                    // 비밀번호 확인 로직 필요
                    val confirmPasswordEntry = viewModel.confirmPassword()
                    if(confirmPasswordEntry.isSame) {
                        confirmPasswordEntry.password?.let {
                            val directions: NavDirections = SetPasswordFragmentDirections.actionSetPasswordFragmentToSetPasswordHintFragment(it)
                            Navigation.findNavController(binding.root).navigate(directions)
                        }
                    } else {
                        Snackbar.make(binding.root, "비밀번호가 일치하지 않습니다.", Snackbar.LENGTH_SHORT).show()
                        viewModel.confirmPassword.value = ""
                    }
                }
            }
        }
    }
}
