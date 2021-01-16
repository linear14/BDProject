package com.bd.bdproject.view.fragment

import androidx.fragment.app.Fragment
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.view.activity.MainActivity

open class BaseFragment: Fragment() {

    val mainActivity by lazy {
        activity as MainActivity
    }

    var onBackPressedListener: OnBackPressedInFragment? = null
}