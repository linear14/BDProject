package com.bd.bdproject.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bd.bdproject.util.BitDamApplication
import java.text.SimpleDateFormat
import java.util.*

class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Objects.equals(intent.action, "android.intent.action.BOOT_COMPLETED")) {

            // on device boot complete, reset the alarm
            val alarmIntent = Intent(context, DairyAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val nextTimeMillis = BitDamApplication.pref.dairyAlarmTime

            val nextNotifyTime = GregorianCalendar()
            nextNotifyTime.timeInMillis = nextTimeMillis

            if(nextNotifyTime.before(Calendar.getInstance())) {
                nextNotifyTime.add(Calendar.DATE, 1)
            }

            val nextAlarmTime: Date = nextNotifyTime.time
            val toastMessage = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ").format(nextAlarmTime)
            Toast.makeText(
                context.applicationContext,
                "[재부팅후] 다음 알람은 " + toastMessage + "으로 알람이 설정되었습니다!",
                Toast.LENGTH_SHORT
            ).show()

            manager.setRepeating(
                AlarmManager.RTC_WAKEUP, nextNotifyTime.timeInMillis,
                AlarmManager.INTERVAL_DAY, pendingIntent
            )
        }
    }
}