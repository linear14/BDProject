package com.bd.bdproject.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bd.bdproject.R
import com.bd.bdproject.data.model.PushMessage
import com.bd.bdproject.util.AlarmUtil.REQUEST_ALARM
import com.bd.bdproject.view.activity.SplashActivity
import java.util.*

class DairyAlarmReceiver: BroadcastReceiver() {

    private val pushList = listOf<PushMessage>(
        PushMessage("오늘의 빛을 담을 시간이에요✨", "소중한 빛을 지금 바로 기록해볼까요?"),
        PushMessage("오늘의 빛을 담을 시간이에요✨", "오늘도 빛을 담아주실거죠? 얼마나 밝을지 궁금해요!")
    )

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val pendingIntent = PendingIntent.getActivity(
            context, REQUEST_ALARM, Intent(
                context,
                SplashActivity::class.java
            ), 0
        )

        context?.let {
            val builder = NotificationCompat.Builder(it, "dairy_alarm_channel")

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setSmallIcon(R.drawable.ic_bitdam)

                val channelName = "DAIRY_CHANNEL"
                val description = "FOR_DAIRY_ALARM"
                val importance = NotificationManager.IMPORTANCE_HIGH

                val channel = NotificationChannel("dairy_alarm_channel", channelName, importance)
                channel.description = description

                notificationManager?.createNotificationChannel(channel)
            } else {
                builder.setSmallIcon(R.mipmap.ic_launcher)
            }

            val rand = Random()
            val randIndex = rand.nextInt(pushList.size)

            builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(pushList[randIndex].title)
                .setContentText(pushList[randIndex].content)
                .setContentIntent(pendingIntent)

            notificationManager?.notify(1234, builder.build())
        }

    }
}