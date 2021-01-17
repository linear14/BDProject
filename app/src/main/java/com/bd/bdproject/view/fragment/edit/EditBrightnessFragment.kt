package com.bd.bdproject.view.fragment.edit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.util.LightUtil.getDiagonalLight
import com.bd.bdproject.view.activity.BitdamEditActivity
import com.bd.bdproject.view.fragment.ControlBrightnessFragment
import com.bd.bdproject.viewmodel.common.LightViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class EditBrightnessFragment: ControlBrightnessFragment() {

    private val lightViewModel: LightViewModel by inject()

    private val args: EditBrightnessFragmentArgs by navArgs()

    private val parentActivity by lazy {
        activity as BitdamEditActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnDrawer.visibility = View.GONE
            btnBack.visibility = View.VISIBLE

            actionEnroll.setOnClickListener { editBrightness() }
        }
    }

    override fun onResume() {
        super.onResume()
        showUi()

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }

    private fun showUi() {
        binding.apply {
            val brightness = args.light?.bright?:0

            setEntireLightFragmentColor(brightness)
            gradientDrawable.colors = getDiagonalLight(brightness * 2)
            layoutAddLight.background = gradientDrawable
            tvBrightness.text = brightness.toString()
            tvAskCondition.visibility = View.GONE
            tvBrightness.visibility = View.VISIBLE
            sbLight.barWidth = 4
            sbLight.progress = brightness * 2

            sbLight.alpha = 1.0f
            actionEnroll.visibility = View.VISIBLE
        }
    }

    private fun editBrightness() {
        runBlocking {
            val job = GlobalScope.launch {
                args.light?.let {
                    lightViewModel.editBrightness(binding.tvBrightness.text.toString().toInt(), it.dateCode)
                }
            }

            job.join()

            if(job.isCancelled) {
                Toast.makeText(BitDamApplication.applicationContext(), "밝기 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(BitDamApplication.applicationContext(), "밝기 변경이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    // 원래 액티비티로 이동 (DetailActivity)
                }
            }
        }
    }

}