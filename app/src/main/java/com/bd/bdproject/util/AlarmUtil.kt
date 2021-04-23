package com.bd.bdproject.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_ONE_SHOT
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

    const val REQUEST_ALARM = 1000

    /***
     * * @param flags May be {@link #FLAG_ONE_SHOT}, {@link #FLAG_NO_CREATE},
     * {@link #FLAG_CANCEL_CURRENT}, {@link #FLAG_UPDATE_CURRENT},
     * {@link #FLAG_IMMUTABLE} or any of the flags as supported by
     * {@link Intent#fillIn Intent.fillIn()} to control which unspecified parts
     * of the intent that can be supplied when the actual send happens.
     */

    fun setDairyAlarm(context: Context, view: View, hour: Int) {
        val pm = context.packageManager
        val receiver = ComponentName(context, DeviceBootReceiver::class.java)
        val alarmIntent = Intent(context, DairyAlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, REQUEST_ALARM, intent, 0)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(alarmIntent)
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        if(BitDamApplication.pref.useDairyPush) {

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            if(calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
            }

            Snackbar.make(view, "매일 알람을 설정하셨습니다.", Snackbar.LENGTH_SHORT).show()

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            )
        } else {
            Snackbar.make(view, "매일 알람을 해제하셨습니다.", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun setThreeDayAlarm(context: Context) {
        val pm = context.packageManager
        val receiver = ComponentName(context, DeviceBootReceiver::class.java)
        val alarmIntent = Intent(context, ThreeDayAlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, REQUEST_ALARM, intent, 0)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(alarmIntent)
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        if(BitDamApplication.pref.useAppPush) {

            val calendar = Calendar.getInstance().apply {
                add(Calendar.DATE, 3)
            }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 3,
                alarmIntent
            )
        }
    }
}