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

class ThreeDayAlarmReceiver: BroadcastReceiver() {

    private val pushList = listOf<PushMessage>(
        PushMessage("빛담의 불이 꺼지고 있어요…\uD83D\uDE22 돌아와주세요", "빛담은 언제나 당신의 이야기를 들을 준비가 되어있답니다. 언제든 환영할게요!"),
        PushMessage("빛담이 너무 어두워요..", "최근 기분이 어땠는지 빛담은 궁금해요. 당신의 빛을 담아주세요!"),
        PushMessage("빛담을 잊어버리셨나요?", "빛이 사라지고 있어요. 다시 한 번 빛담과 빛을 모아볼까요?")
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
            val builder = NotificationCompat.Builder(it, "three_day_alarm_channel")

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setSmallIcon(R.drawable.ic_bitdam)

                val channelName = "THREE_DAY_CHANNEL"
                val description = "FOR_THREE_DAY_ALARM"
                val importance = NotificationManager.IMPORTANCE_HIGH

                val channel = NotificationChannel("three_day_alarm_channel", channelName, importance)
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

            notificationManager?.notify(5678, builder.build())
        }
    }
}