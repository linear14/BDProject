package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bd.bdproject.databinding.ActivityPushBinding
import com.bd.bdproject.util.AlarmUtil
import com.bd.bdproject.util.AlarmUtil.NOT_USE_ALARM
import com.bd.bdproject.util.BitDamApplication

class PushActivity : AppCompatActivity() {

    lateinit var binding: ActivityPushBinding

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
            BitDamApplication.pref.useDairyPush = newCheckedState

            if (newCheckedState) {
                if (oldCheckedState != newCheckedState) {
                    BitDamApplication.pref.dairyAlarmTime = 22
                    AlarmUtil.setDairyAlarm(view.context, binding.root, 22)
                }
            } else {
                if (oldCheckedState != newCheckedState) {
                    BitDamApplication.pref.dairyAlarmTime = -1
                    AlarmUtil.setDairyAlarm(view.context, binding.root, NOT_USE_ALARM)
                }
            }
        }
    }
}