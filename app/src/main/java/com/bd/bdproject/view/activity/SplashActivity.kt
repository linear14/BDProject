package com.bd.bdproject.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivitySplashBinding
import com.bd.bdproject.util.Constant.INFO_DATE_CODE
import com.bd.bdproject.util.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.util.Constant.INFO_SHOULD_HAVE_DRAWER
import com.bd.bdproject.util.Constant.SPLASH
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.viewmodel.SplashViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private var binding: ActivitySplashBinding? = null
    private val splashViewModel: SplashViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater).apply {
            setContentView(root)

            GlobalScope.launch {
                val deferred = splashViewModel.isEnrolledTodayAsync(System.currentTimeMillis().timeToString())
                deferred.join()

                delay(1500)

                withContext(Dispatchers.Main) {
                    var intent: Intent? = null

                    // DB 작업 실패시 --> 빛 입력 후 화면으로 이동
                    if(deferred.isCancelled) {
                        intent = Intent(this@SplashActivity, DetailActivity::class.java)
                    } else {
                        val isEnrolledToday = deferred.getCompleted()

                        if(isEnrolledToday) {
                            intent = Intent(this@SplashActivity, DetailActivity::class.java).apply {
                                putExtra(INFO_DATE_CODE, System.currentTimeMillis().timeToString())
                                putExtra(INFO_SHOULD_HAVE_DRAWER, true)
                            }
                        } else {
                            intent = Intent(this@SplashActivity, BitdamEnrollActivity::class.java)
                        }
                    }
                    startActivity(intent.apply{ putExtra(INFO_PREVIOUS_ACTIVITY, SPLASH) })
                    finish()
                }
            }
        }

    }
}