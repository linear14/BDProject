package com.bd.bdproject.view.fragment

import androidx.fragment.app.Fragment
import com.bd.bdproject.`interface`.OnBackPressedInFragment

open class BaseFragment: Fragment() {

    var onBackPressedListener: OnBackPressedInFragment? = null
}