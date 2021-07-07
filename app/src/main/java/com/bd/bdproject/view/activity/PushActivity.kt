package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bd.bdproject.databinding.ActivityPushBinding
import com.bd.bdproject.dialog.SlideTimePicker
import com.bd.bdproject.util.AlarmUtil
import com.bd.bdproject.common.BitDamApplication

class PushActivity : AppCompatActivity() {

    lateinit var binding: ActivityPushBinding
    private var timePicker: SlideTimePicker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPushBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnBack.setOnClickListener { onBackPressed() }
        }

        setSwitchPush()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            switchPushApp.isChecked = BitDamApplication.pref.useAppPush
            switchPushDairy.isChecked = BitDamApplication.pref.useDairyPush
        }
    }

    override fun onStop() {
        super.onStop()
        timePicker?.dismissAllowingStateLoss()
    }

    private fun setSwitchPush() {
        binding.switchPushApp.setOnCheckedChangeListener { view, newCheckedState ->
            val oldCheckedState = BitDamApplication.pref.useAppPush
            BitDamApplication.pref.useAppPush = newCheckedState

            if (newCheckedState) {
                if (oldCheckedState != newCheckedState) {
                    AlarmUtil.setThreeDayAlarm(view.context, binding.root, true)
                }
            } else {
                if (oldCheckedState != newCheckedState) {
                    AlarmUtil.setThreeDayAlarm(view.context, binding.root, false)
                }
            }
        }

        binding.switchPushDairy.setOnCheckedChangeListener { view, newCheckedState ->
            val oldCheckedState = BitDamApplication.pref.useDairyPush

            if (newCheckedState) {
                if (oldCheckedState != newCheckedState) {
                    openTimePicker(true)
                }
            } else {
                if (oldCheckedState != newCheckedState) {
                    BitDamApplication.pref.dairyAlarmHour = -1
                    BitDamApplication.pref.dairyAlarmMin = -1
                    BitDamApplication.pref.useDairyPush = false
                    AlarmUtil.setDairyAlarm(view.context, binding.root, -1, -1)
                }
            }
        }
    }

    private fun openTimePicker(use: Boolean) {
        timePicker = SlideTimePicker { hour, min, ap ->
            if(hour == -1 && min == -1 && ap == -1) {
                binding.switchPushDairy.isChecked = false
            } else {
                BitDamApplication.pref.dairyAlarmHour = if(ap == 0) hour else hour+12
                BitDamApplication.pref.dairyAlarmMin = min

                BitDamApplication.pref.useDairyPush = use
                AlarmUtil.setDairyAlarm(binding.root.context, binding.root, if(ap == 0) hour else hour+12, min)
            }
        }

        timePicker?.show(supportFragmentManager, "SlideTimePicker")
    }
}