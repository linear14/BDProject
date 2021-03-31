package com.bd.bdproject.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.databinding.ActivitySplashBinding
import com.bd.bdproject.util.AlarmUtil
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.util.Constant.INFO_DATE_CODE
import com.bd.bdproject.util.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.util.Constant.INFO_SHOULD_HAVE_DRAWER
import com.bd.bdproject.util.Constant.SPLASH
import com.bd.bdproject.util.SharedUtil
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private var binding: ActivitySplashBinding? = null
    private val splashViewModel: CheckEnrollStateViewModel by inject()

    var splashJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        BitDamApplication.removeLifecycleObserver()
        BitdamLog.contentLogger("SplashActivity: onCreate")

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater).apply {
            setContentView(root)

            splashJob = CoroutineScope(Dispatchers.IO).launch {
                val deferred = splashViewModel.isEnrolledTodayAsync(System.currentTimeMillis().timeToString())
                val isEnrolledToday = deferred.await()

                AlarmUtil.setThreeDayAlarm(this@SplashActivity)
                delay(1500)

                // 데이터 불러오기 실패했을 경우
                if(deferred.isCancelled) {
                    val failedIntent = Intent().apply {
                        putExtra(INFO_PREVIOUS_ACTIVITY, SPLASH)
                    }

                    startActivity(failedIntent)
                    finish()
                }

                launch(Dispatchers.Main) {
                    val intent: Intent = if(isEnrolledToday) {
                        Intent(this@SplashActivity, DetailActivity::class.java).apply {
                            putExtra(INFO_DATE_CODE, System.currentTimeMillis().timeToString())
                            putExtra(INFO_SHOULD_HAVE_DRAWER, true)
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