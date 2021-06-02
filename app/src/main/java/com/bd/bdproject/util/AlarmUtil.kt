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
import com.bd.bdproject.common.BitDamApplication
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

    fun setDairyAlarm(context: Context, view: View, hour: Int, min: Int) {
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
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0)
            }

            if(calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
            }

            Snackbar.make(view, "작성 시간 알림이 설정되었어요", Snackbar.LENGTH_SHORT).show()

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            )
        } else {
            Snackbar.make(view, "작성 시간 알림이 해지되었어요", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun setThreeDayAlarm(context: Context, view: View? = null, use: Boolean) {
        val pm = context.packageManager
        val receiver = ComponentName(context, DeviceBootReceiver::class.java)
        val alarmIntent = Intent(context, ThreeDayAlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, REQUEST_ALARM, intent, 0)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(alarmIntent)
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        if(BitDamApplication.pref.useAppPush && use) {

            val nextAlarmTime = BitDamApplication.pref.lastVisitedTime + AlarmManager.INTERVAL_DAY * 3

            view?.let { Snackbar.make(view, "빛담 앱 알림을 수신합니다", Snackbar.LENGTH_SHORT).show() }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTime,
                AlarmManager.INTERVAL_DAY * 3,
                alarmIntent
            )
        } else {
            view?.let { Snackbar.make(view, "빛담 앱 알림을 수신하지 않습니다", Snackbar.LENGTH_SHORT).show() }
        }
    }
}