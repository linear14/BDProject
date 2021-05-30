package com.bd.bdproject.view.fragment.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bd.bdproject.databinding.FragmentControlBrightnessBinding
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.util.LightUtil
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            actionEnroll.setOnClickListener { editBrightness() }

            handleSeekBarEvent()
        }
    }

    override fun onResume() {
        super.onResume()
        makeBackground(args.light?.bright?:0)

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }

    private fun makeBackground(brightness: Int) {
        setEntireLightFragmentColor(brightness)
        gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
        parentActivity.binding.root.background = gradientDrawable
        binding.apply {
            btnBack.visibility = View.VISIBLE
            btnDrawer.visibility = View.GONE
            tvBrightness.visibility = View.VISIBLE
            tvBrightness.text = brightness.toString()
            sbLight.firstProgress = brightness * 2
            sbLight.thumbAvailable = true
            sbLight.alpha = 1.0f
            sbLight.makeBarVisible()
            actionEnroll.visibility = View.VISIBLE
        }
    }

    private fun handleSeekBarEvent() {
        binding.apply {
            sbLight.setOnProgressChangeListener { progress ->
                if(!isChangingFragment) {
                    val brightness = getBrightness(progress)
                    setEntireLightFragmentColor(brightness)

                    tvBrightness.text = brightness.toString()

                    gradientDrawable.colors = LightUtil.getDiagonalLight(progress)
                    parentActivity.binding.root.background = gradientDrawable
                    isFirstPressed = false
                }
            }
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
                    parentActivity.returnToDetailActivity()
                }
            }
        }
    }

}