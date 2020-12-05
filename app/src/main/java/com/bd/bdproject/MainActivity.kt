package com.bd.bdproject

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bd.bdproject.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            sbLight.thumbPlaceholderDrawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.deco_seekbar_thumb)
            sbLight.setOnProgressChangeListener {
                tvBrightness.text = getBrightness(it).toString()
            }

            sbLight.setOnPressListener {
                tvAsk.visibility = View.GONE
                tvBrightness.visibility = View.VISIBLE
                tvBrightness.text = getBrightness(it).toString()
                sbLight.barWidth = 4
            }
        }

        showMessageWithDelay()
    }

    private fun showMessageWithDelay() {
        GlobalScope.launch {
            binding.apply {
                tvAsk.clearAnimation()
                tvAsk.visibility = View.GONE
                sbLight.clearAnimation()
                delay(1000)

                withContext(Dispatchers.Main) {
                    tvAsk.animate()
                        .alpha(1.0f)
                        .setDuration(2000)
                        .setListener(object: AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator?) {
                                super.onAnimationStart(animation)
                                tvAsk.visibility = View.VISIBLE

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                sbLight.animate()
                                    .alpha(1.0f)
                                    .setDuration(2000)
                            }
                        })
                }
            }
        }
    }

    private fun getBrightness(step: Int): Int {
        val convertedStep = step / 10
        return (convertedStep * 5)
    }
}