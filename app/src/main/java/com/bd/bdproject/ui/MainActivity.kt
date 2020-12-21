package com.bd.bdproject.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bd.bdproject.R
import com.bd.bdproject.`interface`.JobFinishedListener
import com.bd.bdproject.databinding.ActivityMainBinding
import com.bd.bdproject.ui.main.AddLightFragment
import com.bd.bdproject.ui.main.LightDetailFragment

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
        }
    }

    private fun setFragmentByEnrolledState(isEnrolled: Boolean) {
        when(isEnrolled) {
            true -> supportFragmentManager.beginTransaction().replace(R.id.layout_frame, LightDetailFragment()).commit()
            false -> {
                // TODO lambda로 어떻게 못바꾸나?
                val fragment = AddLightFragment().apply {
                    setOnJobFinishedListener(object: JobFinishedListener {
                        override fun onSuccess() {
                            setFragmentByEnrolledState(true)
                        }
                    })
                }
                supportFragmentManager.beginTransaction().replace(R.id.layout_frame, fragment).commit()
            }
        }
    }
}