package com.bd.bdproject.view.fragment.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bd.bdproject.common.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.common.convertToBrightness
import com.bd.bdproject.databinding.FragmentControlBrightnessBinding
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.view.activity.BitdamEditActivity
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.viewmodel.EditViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditBrightnessFragment: BaseFragment() {

    private var _binding: FragmentControlBrightnessBinding? = null
    val binding get() = _binding!!
    private val editViewModel: EditViewModel by activityViewModels()

    private val parentActivity by lazy {
        activity as BitdamEditActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlBrightnessBinding.inflate(inflater, container, false)

        initViewAndData(editViewModel.light?.bright?:0)

        binding.apply {
            actionEnroll.setOnClickListener { editBrightness() }
            btnBack.setOnClickListener { parentActivity.onBackPressed() }
        }

        handleSeekBar()

        return binding.root
    }


    private fun initViewAndData(brightness: Int) {

        binding.apply {
            setEntireLightFragmentColor(brightness)
            parentActivity.updateBackgroundColor(LightUtil.getDiagonalLight(brightness * 2))

            sbLight.apply {
                firstProgress = brightness * 2
                thumbAvailable = true
                alpha = 1.0f
            }.also { it.makeBarVisible() }
            sbLightFake.visibility = View.GONE
            thumbFake.visibility = View.GONE

            tvBrightness.text = brightness.toString()
            tvBrightness.alpha = 1.0f
            actionEnroll.visibility = View.VISIBLE
        }
    }

    private fun setEntireLightFragmentColor(brightness: Int) {
        binding.apply {
            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                actionEnroll,
                btnBack
            )
        }
    }

    private fun handleSeekBar() {
        binding.apply {
            sbLight.setOnProgressChangeListener { progress ->
                val brightness = progress.convertToBrightness()
                setEntireLightFragmentColor(brightness)
                tvBrightness.text = brightness.toString()
                parentActivity.updateBackgroundColor(LightUtil.getDiagonalLight(progress))
                editViewModel.light = editViewModel.light?.copy(bright = brightness)
            }
        }
    }

    private fun editBrightness() {
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                val light = editViewModel.light
                if(light != null) {
                    val brightness = binding.tvBrightness.text.toString().toInt()
                    editViewModel.editBrightness(brightness, light.dateCode)
                }
            }.join()

            CoroutineScope(Dispatchers.Main).launch {
                parentActivity.returnToDetailActivity(CONTROL_BRIGHTNESS)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}