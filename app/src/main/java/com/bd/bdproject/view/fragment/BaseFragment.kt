package com.bd.bdproject.view.fragment

import androidx.fragment.app.Fragment
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.view.activity.BitdamEnrollActivity

open class BaseFragment: Fragment() {

    val mainActivity by lazy {
        activity as BitdamEnrollActivity
    }

    var onBackPressedListener: OnBackPressedInFragment? = null
}