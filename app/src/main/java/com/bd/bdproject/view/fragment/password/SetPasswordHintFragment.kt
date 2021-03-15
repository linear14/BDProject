package com.bd.bdproject.view.fragment.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bd.bdproject.databinding.FragmentSetPasswordBinding
import com.bd.bdproject.databinding.FragmentSetPasswordHintBinding

class SetPasswordHintFragment: Fragment() {

    var _binding: FragmentSetPasswordHintBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetPasswordHintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}