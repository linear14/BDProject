package com.bd.bdproject.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.R
import com.bd.bdproject.databinding.ActivitySplashBinding
import com.bd.bdproject.util.AlarmUtil
import com.bd.bdproject.common.BitDamApplication
import com.bd.bdproject.common.Constant.INFO_BRIGHTNESS
import com.bd.bdproject.common.Constant.INFO_DATE_CODE
import com.bd.bdproject.common.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.common.Constant.INFO_SHOULD_HAVE_DRAWER
import com.bd.bdproject.common.Constant.SPLASH
import com.bd.bdproject.common.timeToString
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import com.bd.bdproject.viewmodel.common.LightViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {

    private var binding: ActivitySplashBinding? = null
    private val splashViewModel: CheckEnrollStateViewModel by viewModel()

    var splashJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        BitDamApplication.removeLifecycleObserver()
        BitdamLog.contentLogger("SplashActivity: onCreate")

        super.onCreate(savedInstanceState)
        splashViewModel
        binding = ActivitySplashBinding.inflate(layoutInflater).apply {
            setContentView(root)

            val scaleUpAnimation = AnimationUtils.loadAnimation(root.context, R.anim.scale_up).apply {
                interpolator = AccelerateInterpolator()
                setAnimationListener(object: Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {}
                    override fun onAnimationRepeat(p0: Animation?) {}

                    override fun onAnimationEnd(p0: Animation?) {
                        splashJob = CoroutineScope(Dispatchers.IO).launch {
                            val enterTime = System.currentTimeMillis()
                            val isEnrolledToday = splashViewModel.isEnrolledTodayAsync(enterTime.timeToString()).await()

                            BitDamApplication.pref.lastVisitedTime = enterTime
                            if(BitDamApplication.pref.useAppPush) {
                                AlarmUtil.setThreeDayAlarm(this@SplashActivity, use = true)
                            }
                            delay(1000)

                            launch(Dispatchers.Main) {
                                val intent: Intent = if(isEnrolledToday) {
                                    Intent(this@SplashActivity, DetailActivity::class.java).apply {
                                        putExtra(INFO_DATE_CODE, enterTime.timeToString())
                                        putExtra(INFO_SHOULD_HAVE_DRAWER, true)
                                        putExtra(INFO_BRIGHTNESS, splashViewModel.getTodayBrightness(enterTime.timeToString()).await())
                                    }
                                } else {
                                    Intent(this@SplashActivity, BitdamEnrollActivity::class.java)
                                }

                                startActivity(intent.apply { putExtra(INFO_PREVIOUS_ACTIVITY, SPLASH) } )
                                finish()
                                BitDamApplication.enrollLifecycleObserver()
                            }
                        }
                    }

                })
            }

            ivLogo.animation = scaleUpAnimation
            ivLogo.animate()

        }
    }

    override fun onResume() {
        super.onResume()
        BitdamLog.contentLogger("SplashActivity: onResume")

        splashJob?.let { job ->
            if(!job.isActive) {
                job.start()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        CoroutineScope(Dispatchers.IO).launch {
            splashJob?.cancelAndJoin()
        }
    }


    // LIFECYCLE TEST
    override fun onStart() {
        super.onStart()
        BitdamLog.contentLogger("SplashActivity: onStart")
    }

    override fun onPause() {
        super.onPause()
        BitdamLog.contentLogger("SplashActivity: onPause")
    }

    override fun onStop() {
        super.onStop()
        BitdamLog.contentLogger("SplashActivity: onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        BitdamLog.contentLogger("SplashActivity: onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        BitdamLog.contentLogger("SplashActivity: onRestart")
    }
    // LIFECYCLE TEST END
}