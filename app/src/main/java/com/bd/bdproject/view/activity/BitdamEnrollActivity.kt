package com.bd.bdproject.view.activity

import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bd.bdproject.R
import com.bd.bdproject.databinding.ActivityBitdamEnrollBinding
import com.bd.bdproject.util.Constant
import com.bd.bdproject.util.Constant.ACTIVITY_NOT_RECOGNIZED
import com.bd.bdproject.util.Constant.COLLECTION_MAIN
import com.bd.bdproject.util.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class BitdamEnrollActivity : AppCompatActivity() {

    lateinit var binding: ActivityBitdamEnrollBinding
    val previousActivity by lazy {
        intent.getIntExtra(INFO_PREVIOUS_ACTIVITY, ACTIVITY_NOT_RECOGNIZED)
    }
    private val checkEnrollStateViewModel: CheckEnrollStateViewModel by inject()

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

            navigationDrawer.actionSetting.setOnClickListener {
                checkEnrollStateViewModel.isVisitedSetting = true
                startActivity(Intent(this@BitdamEnrollActivity, SettingActivity::class.java))
                drawer.closeDrawer(GravityCompat.START)
            }

            try {
                val pInfo: PackageInfo = applicationContext.packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName
                navigationDrawer.tvVersion.text = "ver. $version"
            } catch (e: Exception) {
                navigationDrawer.tvVersion.text = ""
            }

        }
    }

    override fun onResume() {
        super.onResume()

        if(checkEnrollStateViewModel.isVisitedSetting) {
            checkEnrollStateViewModel.isVisitedSetting = false
            CoroutineScope(Dispatchers.IO).launch {
                val deferred = checkEnrollStateViewModel.isEnrolledTodayAsync(System.currentTimeMillis().timeToString())
                val isEnrolledToday = deferred.await()

                if (isEnrolledToday) {
                    launch(Dispatchers.Main) {
                        val intent =
                            Intent(this@BitdamEnrollActivity, DetailActivity::class.java).apply {
                                putExtra(INFO_PREVIOUS_ACTIVITY, Constant.BITDAM_ENROLL)
                            }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        checkEnrollStateViewModel.isVisitedSetting = false
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