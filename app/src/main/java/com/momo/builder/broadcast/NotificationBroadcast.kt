package com.momo.builder.broadcast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.momo.builder.R
import com.momo.builder.activity.LaunchActivity
import com.momo.builder.activity.MemoListActivity
import com.momo.builder.u.AlarmU
import com.momo.builder.u.EventReportU
import kotlin.random.Random

class NotificationBroadcast : BroadcastReceiver() {
    companion object {
        const val actionClock = "action_clock"
        const val actionRepeatClock = "action_repeat_clock"
        const val actionPeriodClock = "action_period_clock"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("----", "NotificationBroadcast---onReceive")
        if (intent == null || context == null) return
        val action = intent.action
        Log.d("----", "NotificationBroadcast---onReceive---action:$action")
        val randomId = Random(System.currentTimeMillis()).nextInt(0, 10000)
        Log.d("----", "NotificationBroadcast---onReceive---action:$action---randomId:$randomId")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("id", "name", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val builder = Notification.Builder(context)
        builder.setChannelId("id")
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
        builder.setWhen(System.currentTimeMillis())
        builder.setAutoCancel(true)
        val launchIntent = Intent(context, LaunchActivity::class.java)
        launchIntent.putExtra("isFromNotificationClick", true)
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        builder.setContentIntent(
            PendingIntent.getActivities(
                context, 0,
                arrayOf(launchIntent),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        MemoListActivity.mCurrentPos = 1
        val notification = builder.build()
        notificationManager.notify(randomId, notification)
        EventReportU.reportCustomEvent(EventReportU.notice_done)

        if (action == actionRepeatClock) {//每日闹钟
            AlarmU.setRepeatOrPeriodAlarm(context, intent, action)
        } else if (action == actionPeriodClock) {//周期闹钟
            AlarmU.setRepeatOrPeriodAlarm(context, intent, action, true)
        }
    }
}