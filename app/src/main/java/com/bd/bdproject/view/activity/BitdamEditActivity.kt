package com.bd.bdproject.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.bd.bdproject.EditNavigationDirections
import com.bd.bdproject.R
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.model.Tags
import com.bd.bdproject.databinding.ActivityBitdamEditBinding
import com.bd.bdproject.common.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.common.Constant.CONTROL_MEMO
import com.bd.bdproject.common.Constant.CONTROL_TAG
import com.bd.bdproject.common.Constant.DESTINATION_NOT_RECOGNIZED
import com.bd.bdproject.common.Constant.INFO_BRIGHTNESS
import com.bd.bdproject.common.Constant.INFO_DESTINATION
import com.bd.bdproject.common.Constant.INFO_LIGHT
import com.bd.bdproject.common.Constant.INFO_TAG
import com.bd.bdproject.viewmodel.EditViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BitdamEditActivity : AppCompatActivity() {

    lateinit var binding: ActivityBitdamEditBinding
    private val editViewModel: EditViewModel by viewModel()

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBitdamEditBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        setFragments()
    }

    private fun setFragments() {
        when(intent.getIntExtra(INFO_DESTINATION, DESTINATION_NOT_RECOGNIZED)) {
            CONTROL_BRIGHTNESS -> {
                if(editViewModel.light == null) {
                    editViewModel.light = intent.getParcelableExtra(INFO_LIGHT)
                }
                val navDirection: NavDirections = EditNavigationDirections.actionGlobalEditBrightnessFragment()
                (supportFragmentManager.findFragmentById(R.id.layout_fragment) as NavHostFragment).navController.navigate(navDirection)
            }
            CONTROL_TAG -> {
                if(editViewModel.light == null && editViewModel.tags == null) {
                    val temp = intent.getParcelableArrayListExtra<Tag>(INFO_TAG)
                    val tags = Tags()
                    for(i in temp?: mutableListOf()) {
                        tags.add(i)
                    }

                    editViewModel.light = intent.getParcelableExtra(INFO_LIGHT)
                    editViewModel.tags = tags
                }
                val navDirection: NavDirections = EditNavigationDirections.actionGlobalEditTagFragment()
                (supportFragmentManager.findFragmentById(R.id.layout_fragment) as NavHostFragment).navController.navigate(navDirection)
            }
            CONTROL_MEMO -> {
                if(editViewModel.light == null) {
                    editViewModel.light = intent.getParcelableExtra(INFO_LIGHT)
                }
                val navDirection: NavDirections = EditNavigationDirections.actionGlobalEditMemoFragment()
                (supportFragmentManager.findFragmentById(R.id.layout_fragment) as NavHostFragment).navController.navigate(navDirection)
            }
        }
    }

    fun returnToDetailActivity(type: Int) {
        val resultIntent = Intent().apply {
            putExtra("TYPE", type)
            putExtra(INFO_BRIGHTNESS, editViewModel.light?.bright)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun updateBackgroundColor(gradientLights: IntArray) {
        gradientDrawable.colors = gradientLights
        binding.root.background = gradientDrawable
    }
}