package com.bd.bdproject.view.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.alarm.DairyAlarmReceiver
import com.bd.bdproject.alarm.DeviceBootReceiver
import com.bd.bdproject.databinding.ActivitySettingBinding
import com.bd.bdproject.util.BitDamApplication
import java.text.SimpleDateFormat
import java.util.*

class SettingActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            settingLock.setOnClickListener {
                startActivity(Intent(it.context, SetPasswordActivity::class.java))
            }

            settingHelp.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO/*, Uri.fromParts("mailto", "", null)*/).apply {
                    type = "message/rfc822"
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("bitdam@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "문의 제목")
                    putExtra(Intent.EXTRA_TEXT, "문의내용을 남겨주세요.\n(비밀번호 찾기 및 데이터 복원은 불가능합니다)")
                }
                startActivity(Intent.createChooser(emailIntent,""))
            }

            btnBack.setOnClickListener { onBackPressed() }

            /*** 테스트 코드 ***/
            testDairyAlarm.setOnClickListener { setFirstAlarmTime(true) }
            testDairyAlarmCancel.setOnClickListener { setFirstAlarmTime(false) }
        }


        setSwitchAnimation()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            switchAnimation.isChecked = BitDamApplication.pref.isAnimationActivate
        }
    }

    private fun setSwitchAnimation() {
        binding.switchAnimation.setOnCheckedChangeListener { _, isChecked ->
            BitDamApplication.pref.isAnimationActivate = isChecked
        }
    }

    private fun setFirstAlarmTime(isNewAlarm: Boolean) {
        // val time = BitDamApplication.pref.dairyAlarmTime
        val calendar = Calendar.getInstance()
        // TEST
        calendar.timeInMillis = System.currentTimeMillis() + 1000 * 10
        /*calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 31)
        calendar.set(Calendar.SECOND, 0)*/

        if(calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1)
        }

        BitDamApplication.pref.dairyAlarmTime = calendar.timeInMillis

        dairyNotification(calendar, isNewAlarm)
    }

    private fun dairyNotification(calendar: Calendar, isNewAlarm: Boolean) {
        val pm = this.packageManager
        val receiver = ComponentName(this, DeviceBootReceiver::class.java)
        val alarmIntent = Intent(this, DairyAlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, 0)
        }
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(isNewAlarm) {
            val nextAlarmTime = calendar.time
            val toastMessage = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분").format(nextAlarmTime)
            Toast.makeText(this, "다음 알람은 $toastMessage 으로 설정되었습니다.", Toast.LENGTH_SHORT).show()

            // alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, 15 * 60000, alarmIntent)
            // alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)

            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        } else {
            alarmManager.cancel(alarmIntent)
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            Toast.makeText(this, "알람이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }

    }
}