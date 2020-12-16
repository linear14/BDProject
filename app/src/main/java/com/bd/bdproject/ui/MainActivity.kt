package com.bd.bdproject.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bd.bdproject.R
import com.bd.bdproject.databinding.ActivityMainBinding
import com.bd.bdproject.ui.main.AddLightFragment
import com.bd.bdproject.ui.main.LightDetailFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
            true -> supportFragmentManager.beginTransaction().add(R.id.layout_frame, LightDetailFragment()).commit()
            false -> supportFragmentManager.beginTransaction().add(R.id.layout_frame, AddLightFragment()).commit()
        }
    }
}