package com.bd.bdproject.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bd.bdproject.R
import com.bd.bdproject.databinding.ActivityBitdamEnrollBinding
import com.bd.bdproject.util.Constant.ACTIVITY_NOT_RECOGNIZED
import com.bd.bdproject.util.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.view.fragment.BaseFragment

class BitdamEnrollActivity : AppCompatActivity() {

    lateinit var binding: ActivityBitdamEnrollBinding
    val previousActivity by lazy {
        intent.getIntExtra(INFO_PREVIOUS_ACTIVITY, ACTIVITY_NOT_RECOGNIZED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBitdamEnrollBinding.inflate(layoutInflater).apply {
            setContentView(root)

            navigationDrawer.actionMyLight.setOnClickListener {
                startActivity(Intent(this@BitdamEnrollActivity, CollectionMainActivity::class.java))
                drawer.closeDrawer(GravityCompat.START)
            }

            navigationDrawer.actionStatistic.setOnClickListener {
                startActivity(Intent(this@BitdamEnrollActivity, StatisticActivity::class.java))
                drawer.closeDrawer(GravityCompat.START)
            }

            navigationDrawer.actionHash.setOnClickListener {
                startActivity(Intent(this@BitdamEnrollActivity, ManageHashActivity::class.java))
                drawer.closeDrawer(GravityCompat.START)
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
}