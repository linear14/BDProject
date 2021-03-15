package com.bd.bdproject.view.fragment.password

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bd.bdproject.databinding.FragmentSetPasswordBinding
import com.bd.bdproject.databinding.FragmentSetPasswordHintBinding
import com.bd.bdproject.view.activity.SetPasswordActivity
import com.bd.bdproject.viewmodel.SetPasswordViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SetPasswordHintFragment: Fragment() {

    var _binding: FragmentSetPasswordHintBinding? = null
    val binding get() = _binding!!

    val viewModel: SetPasswordViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetPasswordHintBinding.inflate(inflater, container, false).apply {
            inputPasswordHint.addTextChangedListener(HintWatcher())
        }

        (activity as SetPasswordActivity).binding.apply {
            actionConfirm.visibility = View.VISIBLE
            switchActivatePassword.visibility = View.GONE
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class HintWatcher: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun afterTextChanged(s: Editable?) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val hint = s?.toString() ?: ""
            viewModel.passwordHint = hint
        }

    }
}