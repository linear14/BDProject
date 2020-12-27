package com.bd.bdproject.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.bd.bdproject.MainNavigationDirections
import com.bd.bdproject.R
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.databinding.ActivityMainBinding
import com.bd.bdproject.ui.main.AddTagFragment

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setFragmentByEnrolledState(intent.getBooleanExtra("IS_ENROLLED_TODAY", true))

            btnDrawer.setOnClickListener {
                drawer.openDrawer(GravityCompat.START)
            }

            btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun setFragmentByEnrolledState(isEnrolled: Boolean) {
        when(isEnrolled) {
            false -> {
                val navDirection: NavDirections = MainNavigationDirections.actionGlobalAddLightFragment()
                findNavController(R.id.layout_fragment).navigate(navDirection)
            }
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.layout_fragment)
        val currentFragment = fragment?.let { it.childFragmentManager.fragments[0] }

        if(currentFragment is BaseFragment) {
            currentFragment.onBackPressedListener?.let {
                val popInFragment = it.onBackPressed()

                if (!popInFragment) {
                    super.onBackPressed()
                }
            }
                ?:super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    fun onBackPressed(isAnimationEnd: Boolean) {
        if(isAnimationEnd) super.onBackPressed()
        else onBackPressed()
    }

    companion object {
        const val ADD_LIGHT = 8000
        const val ADD_TAG = 8100
        const val ADD_MEMO = 8200
        const val LIGHT_DETAIL = 8300
    }
}