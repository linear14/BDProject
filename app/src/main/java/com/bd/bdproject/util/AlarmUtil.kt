package com.bd.bdproject.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import com.bd.bdproject.alarm.DairyAlarmReceiver
import com.bd.bdproject.alarm.DeviceBootReceiver
import com.bd.bdproject.alarm.ThreeDayAlarmReceiver
import com.google.android.material.snackbar.Snackbar
import java.util.*

object AlarmUtil {

    const val NOT_USE_ALARM = -1000

    fun setDairyAlarm(context: Context, view: View, hour: Int) {
        val pm = context.packageManager
        val receiver = ComponentName(context, DeviceBootReceiver::class.java)
        val alarmIntent = Intent(context, DairyAlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(alarmIntent)
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        if(BitDamApplication.pref.usePush) {

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            if(calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
            }
            Snackbar.make(view, "매일 알람을 설정하셨습니다.", Snackbar.LENGTH_SHORT).show()

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent)
        } else {
            Snackbar.make(view, "매일 알람을 해제하셨습니다.", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun setThreeDayAlarm(context: Context) {
        val pm = context.packageManager
        val receiver = ComponentName(context, DeviceBootReceiver::class.java)
        val alarmIntent = Intent(context, ThreeDayAlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(alarmIntent)
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        if(BitDamApplication.pref.usePush) {

            /*val calendar = Calendar.getInstance().apply {
                add(Calendar.DATE, 3)
                set(Calendar.HOUR_OF_DAY, 22)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }*/

            val calendar = Calendar.getInstance().apply {

                add(Calendar.SECOND, 20)
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, 3 * AlarmManager.INTERVAL_DAY, alarmIntent)
        }
    }
}