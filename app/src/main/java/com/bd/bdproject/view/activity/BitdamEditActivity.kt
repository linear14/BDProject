package com.bd.bdproject.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.bd.bdproject.EditNavigationDirections
import com.bd.bdproject.R
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.model.Tags
import com.bd.bdproject.databinding.ActivityBitdamEditBinding
import com.bd.bdproject.common.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.common.Constant.CONTROL_MEMO
import com.bd.bdproject.common.Constant.CONTROL_TAG
import com.bd.bdproject.common.Constant.DESTINATION_NOT_RECOGNIZED
import com.bd.bdproject.common.Constant.INFO_DESTINATION
import com.bd.bdproject.common.Constant.INFO_LIGHT
import com.bd.bdproject.common.Constant.INFO_TAG

class BitdamEditActivity : AppCompatActivity() {

    lateinit var binding: ActivityBitdamEditBinding

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

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
                val temp = intent.getParcelableArrayListExtra<Tag>(INFO_TAG)
                val tags = Tags()
                for(i in temp?: mutableListOf()) {
                    tags.add(i)
                }
                val navDirection: NavDirections =
                    EditNavigationDirections.actionGlobalEditTagFragment(
                        intent.getParcelableExtra(INFO_LIGHT),
                        tags
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

    fun returnToDetailActivity(type: Int) {
        val resultIntent = Intent().apply {
            putExtra("TYPE", type)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun updateBackgroundColor(gradientLights: IntArray) {
        gradientDrawable.colors = gradientLights
        binding.root.background = gradientDrawable
    }
}