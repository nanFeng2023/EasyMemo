package com.momo.builder.u

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

object AlarmU {
    fun setRepeatOrPeriodAlarm(
        context: Context,
        intent: Intent,
        action: String,
        isPeriodAlarm: Boolean = false
    ) {
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val hour = intent.getIntExtra("hour", 0)
        val minute = intent.getIntExtra("minute", 0)
        var clockId = intent.getIntExtra("clockId", 0)
        val cal = Calendar.getInstance()
        if (isPeriodAlarm) {
            val week = intent.getIntExtra("week", 0)
            cal.set(Calendar.DAY_OF_WEEK, week)
            clockId += week
        }
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        val currentTimeMillis = System.currentTimeMillis()
        val curSetTime = cal.timeInMillis
        Log.d(
            "----",
            "setRepeatAlarm111---selectTime:${getFormatTime(curSetTime)}---systemTime:${
                getFormatTime(currentTimeMillis)
            }"
        )
        if (currentTimeMillis > curSetTime) {
            if (isPeriodAlarm) {
                cal.add(Calendar.DAY_OF_MONTH, 7)
            } else {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            Log.d("----", "setRepeatAlarm---add day")
        }
        val mIntent = Intent(action)
        mIntent.putExtra("title", title)
        mIntent.putExtra("content", content)
        val pendingIntent =
            PendingIntent.getBroadcast(context, clockId, mIntent, PendingIntent.FLAG_IMMUTABLE)
        val setTime = cal.timeInMillis
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                setTime,
                pendingIntent
            )
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, setTime, pendingIntent)
        }
        Log.d(
            "----",
            "setRepeatAlarm222---selectTime:${getFormatTime(setTime)}---systemTime:${
                getFormatTime(
                    System.currentTimeMillis()
                )
            }---clockId:$clockId"
        )
    }

    private fun getFormatTime(l: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = l
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val builder = StringBuilder()
        builder.append(year).append("-").append(month).append("-").append(day).append(" ")
            .append(hour).append(":").append(minute).append(":").append(second)
        return builder.toString()
    }

//    fun cancelAlarm(context: Context, memoBean: MemoBean) {
//        Log.d("----", "cancelAlarm---clockId:${memoBean.clockId}---timeType:${memoBean.timeType}")
//        when (memoBean.timeType) {
//            0, 2 -> realCancelAlarm(context, memoBean, NotificationBroadcast.actionClock)
//            1 -> realCancelAlarm(context, memoBean, NotificationBroadcast.actionRepeatClock)
//            3 -> periodCancelAlarm(context, memoBean, arrayOf("2", "3", "4", "5", "6"))
//            4 -> periodCancelAlarm(context, memoBean, arrayOf("7", "1"))
//        }
//    }
//
//    fun getLockIntent(
//        memoBean: MemoBean,
//        action: String = NotificationBroadcast.actionClock
//    ): Intent {
//        val intent = Intent(action)
//        intent.putExtra("title", memoBean.title)
//        intent.putExtra("content", memoBean.content)
//        return intent
//    }
//
//    fun getRepeatIntent(memoBean: MemoBean): Intent {
//        val intent = getLockIntent(memoBean, NotificationBroadcast.actionRepeatClock)
//        intent.putExtra("hour", hour)
//        intent.putExtra("minute", minute)
//        intent.putExtra("clockId", memoBean.clockId)
//        return intent
//    }
//
//    fun getPeriodIntent(memoBean: MemoBean, week: Int): Intent {
//        val intent = getLockIntent(memoBean, NotificationBroadcast.actionPeriodClock)
//        intent.putExtra("hour", hour)
//        intent.putExtra("minute", minute)
//        intent.putExtra("clockId", memoBean.clockId)
//        intent.putExtra("week", week)
//        return intent
//    }
//
//    private fun realCancelAlarm(context: Context, memoBean: MemoBean, action: String) {
//        val intent = when (action) {
//            NotificationBroadcast.actionClock -> {
//                getLockIntent(memoBean, action)
//            }
//
//            else -> {
//                getRepeatIntent()
//            }
//        }
//        val pendingIntent = PendingIntent.getBroadcast(
//            context, memoBean.clockId, intent, PendingIntent.FLAG_IMMUTABLE
//        )
//        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
//        alarmManager.cancel(pendingIntent)
//    }
//
//    private fun periodCancelAlarm(context: Context, memoBean: MemoBean, periodList: Array<String>) {
//        for (i in periodList) {
//            realCancelPeriodAlarm(context, memoBean, i.toInt())
//        }
//    }
//
//    private fun realCancelPeriodAlarm(context: Context, memoBean: MemoBean, week: Int) {
//        val intent = getPeriodIntent(memoBean,week)
//        val pendingIntent = PendingIntent.getBroadcast(
//            context, memoBean.clockId + week, intent, PendingIntent.FLAG_IMMUTABLE
//        )
//        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
//        alarmManager.cancel(pendingIntent)
//    }

}