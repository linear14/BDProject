package com.bd.bdproject.view.fragment.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bd.bdproject.databinding.FragmentControlBrightnessBinding
import com.bd.bdproject.common.BitDamApplication
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.common.convertToBrightness
import com.bd.bdproject.view.activity.BitdamEditActivity
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.viewmodel.common.LightViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class EditBrightnessFragment: BaseFragment() {

    private var _binding: FragmentControlBrightnessBinding? = null
    val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()

    private val args: EditBrightnessFragmentArgs by navArgs()

    private val parentActivity by lazy {
        activity as BitdamEditActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlBrightnessBinding.inflate(inflater, container, false)

        initViewAndData(args.light?.bright?:0)

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
            }
        }
    }

    private fun editBrightness() {
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                val light = args.light
                if(light != null) {
                    lightViewModel.editBrightness(binding.tvBrightness.text.toString().toInt(), light.dateCode)
                }
            }.join()

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(BitDamApplication.applicationContext(), "밝기 변경이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                parentActivity.returnToDetailActivity()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}