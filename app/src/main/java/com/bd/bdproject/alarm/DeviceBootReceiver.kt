package com.bd.bdproject.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bd.bdproject.util.AlarmUtil.REQUEST_ALARM
import com.bd.bdproject.common.BitDamApplication
import java.util.*

class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Objects.equals(intent.action, "android.intent.action.BOOT_COMPLETED")) {

            val alarmIntent = Intent(context, DairyAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_ALARM, alarmIntent, 0)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // 매일알람시간, 마지막 접속시간(3일 알람을 위한) 순
            val dairyAlarmHour = BitDamApplication.pref.dairyAlarmHour
            val dairyAlarmMin = BitDamApplication.pref.dairyAlarmMin
            val lastVisitedTime = BitDamApplication.pref.lastVisitedTime

            if(BitDamApplication.pref.useAppPush) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, dairyAlarmHour)
                    set(Calendar.MINUTE, dairyAlarmMin)
                    set(Calendar.SECOND, 0)
                }

                if(calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1)
                }

                manager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }

            if(BitDamApplication.pref.useDairyPush) {
                val calendar = Calendar.getInstance()

                if (System.currentTimeMillis() - lastVisitedTime < AlarmManager.INTERVAL_DAY * 3) {
                    calendar.timeInMillis = lastVisitedTime + AlarmManager.INTERVAL_DAY * 3
                }

                manager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 3,
                    pendingIntent
                )
            }

        }
    }
}