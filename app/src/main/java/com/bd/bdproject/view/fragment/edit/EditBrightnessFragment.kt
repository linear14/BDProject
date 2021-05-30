package com.bd.bdproject.view.fragment.edit

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bd.bdproject.databinding.FragmentControlBrightnessBinding
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.view.activity.BitdamEditActivity
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.view.fragment.ControlBrightnessFragment
import com.bd.bdproject.viewmodel.common.LightViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class EditBrightnessFragment: BaseFragment() {

    var _binding: FragmentControlBrightnessBinding? = null
    val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()

    private val args: EditBrightnessFragmentArgs by navArgs()

    private val parentActivity by lazy {
        activity as BitdamEditActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlBrightnessBinding.inflate(inflater, container, false).apply {

        }
        return binding.root
    }

/*    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            actionEnroll.setOnClickListener { editBrightness() }
        }
    }

    override fun onResume() {
        super.onResume()
        isFirstPressed = true
        isChangingFragment = false
        makeBackground(args.light?.bright?:0)

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }

    fun makeBackground(brightness: Int) {

        binding.apply {
            setEntireLightFragmentColor(brightness)
            gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
            layoutAddLight.background = gradientDrawable
            tvBrightness.text = brightness.toString()
            tvBrightness.visibility = View.VISIBLE
            sbLight.firstProgress = brightness * 2
            sbLight.thumbAvailable = true

            btnBack.visibility = View.VISIBLE
            btnDrawer.visibility = View.GONE
            sbLight.alpha = 1.0f
            sbLight.makeBarVisible()
            actionEnroll.visibility = View.VISIBLE
        }
    }

    private fun getBrightness(progress: Int): Int {
        val converted = progress / 10
        return (converted * 5)
    }

    fun setEntireLightFragmentColor(brightness: Int) {
        binding.apply {
            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                actionEnroll,
                btnDrawer,
                btnBack
            )
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

    private fun setSeekBarProgressChangedListener() {
        binding.apply {
            sbLight.setOnProgressChangeListener { progress ->
                if(!isChangingFragment) {
                    val brightness = getBrightness(progress)
                    setEntireLightFragmentColor(brightness)

                    tvBrightness.text = brightness.toString()

                    gradientDrawable.colors = LightUtil.getDiagonalLight(progress)
                    layoutAddLight.background = gradientDrawable
                    isFirstPressed = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }*/

}