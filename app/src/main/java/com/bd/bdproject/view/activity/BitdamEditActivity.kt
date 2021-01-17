package com.bd.bdproject.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.bd.bdproject.EditNavigationDirections
import com.bd.bdproject.R
import com.bd.bdproject.databinding.ActivityBitdamEditBinding
import com.bd.bdproject.util.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.util.Constant.CONTROL_MEMO
import com.bd.bdproject.util.Constant.CONTROL_TAG
import com.bd.bdproject.util.Constant.DESTINATION_NOT_RECOGNIZED
import com.bd.bdproject.util.Constant.INFO_DESTINATION
import com.bd.bdproject.util.Constant.INFO_LIGHT
import com.bd.bdproject.util.Constant.INFO_TAG

class BitdamEditActivity : AppCompatActivity() {

    lateinit var binding: ActivityBitdamEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBitdamEditBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
    }

    override fun onResume() {
        super.onResume()
        setFragments()
    }

    private fun setFragments() {
        when(intent.getIntExtra(INFO_DESTINATION, DESTINATION_NOT_RECOGNIZED)) {
            CONTROL_BRIGHTNESS -> {
                val navDirection: NavDirections =
                EditNavigationDirections.actionGlobalEditBrightnessFragment(intent.getParcelableExtra(INFO_LIGHT))
                findNavController(R.id.layout_fragment).navigate(navDirection)
            }
            CONTROL_TAG -> {
                val navDirection: NavDirections =
                    EditNavigationDirections.actionGlobalEditTagFragment(
                        intent.getParcelableExtra(INFO_LIGHT),
                        intent.getParcelableExtra(INFO_TAG)
                    )
                findNavController(R.id.layout_fragment).navigate(navDirection)
            }
            CONTROL_MEMO -> {
                val navDirection: NavDirections =
                    EditNavigationDirections.actionGlobalEditMemoFragment(intent.getParcelableExtra(INFO_LIGHT))
                findNavController(R.id.layout_fragment).navigate(navDirection)
            }
        }
    }
}