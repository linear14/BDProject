package com.bd.bdproject.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivitySplashBinding
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
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)

                    // DB 작업 실패시 --> 빛 입력 후 화면으로 이동
                    if(deferred.isCancelled) {
                        intent.putExtra("IS_ENROLLED_TODAY", true)
                    } else {
                        val isEnrolledToday = deferred.getCompleted()

                        if(isEnrolledToday) {
                            intent.putExtra("IS_ENROLLED_TODAY", true)
                        } else {
                            intent.putExtra("IS_ENROLLED_TODAY", false)
                        }
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }

    }
}