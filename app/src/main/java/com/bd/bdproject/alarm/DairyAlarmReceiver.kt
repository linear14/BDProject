package com.bd.bdproject.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.bd.bdproject.R
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.view.activity.SplashActivity
import java.text.SimpleDateFormat
import java.util.*

class DairyAlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val pendingIntent = PendingIntent.getActivity(
            context, 0, Intent(
                context,
                SplashActivity::class.java
            ), PendingIntent.FLAG_UPDATE_CURRENT
        )

        context?.let {
            val builder = NotificationCompat.Builder(it, "default")

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setSmallIcon(R.drawable.ic_bitdam)

                val channelName = "PUSH_CHANNEL"
                val description = "푸쉬 테스트"
                val importance = NotificationManager.IMPORTANCE_HIGH

                val channel = NotificationChannel("default", channelName, importance)
                channel.description = description

                notificationManager?.createNotificationChannel(channel)
            } else {
                builder.setSmallIcon(R.mipmap.ic_launcher)
            }

            builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("매일 알림")
                .setContentText("오늘의 빛을 등록하는 시간입니다")
                .setContentIntent(pendingIntent)

            notificationManager?.notify(1234, builder.build())
        }

        val nextNotifyTime = Calendar.getInstance()
        nextNotifyTime.set(Calendar.SECOND, 0)
        nextNotifyTime.add(Calendar.MINUTE, 15)

        // BitDamApplication.pref.dairyAlarmTime = nextNotifyTime.timeInMillis

        val currentDateTime = nextNotifyTime.time
        val date_text = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime)
        Toast.makeText(
            context?.applicationContext,
            "다음 알람은 " + date_text + "으로 알람이 설정되었습니다!",
            Toast.LENGTH_SHORT
        ).show()

    }
}