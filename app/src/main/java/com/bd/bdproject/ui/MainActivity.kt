package com.bd.bdproject.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.bd.bdproject.MainNavigationDirections
import com.bd.bdproject.R
import com.bd.bdproject.databinding.ActivityMainBinding

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
        /*** TEST MEMO ***/
        /*supportFragmentManager.beginTransaction().replace(R.id.layout_frame, AddMemoFragment()).commit()
        return*/

        when(isEnrolled) {
            false -> {
                val navDirection: NavDirections = MainNavigationDirections.actionGlobalAddLightFragment()
                findNavController(R.id.layout_fragment).navigate(navDirection)
            }
        }
    }
}