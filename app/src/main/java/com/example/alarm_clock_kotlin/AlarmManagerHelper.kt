package com.example.alarm_clock_kotlin

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

class AlarmManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnspecifiedImmutableFlag", "ScheduleExactAlarm")
    fun setAlarm(id: String, alarmTime: LocalTime) {
        val alarmManager =
            ContextCompat.getSystemService(context, AlarmManager::class.java) as AlarmManager?

        // LocalTimeからCalendarインスタンスを作成
        val now = Calendar.getInstance()
        val alarmCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarmTime.hour)
            set(Calendar.MINUTE, alarmTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // もし設定時間が現在時刻より前なら、次の日にアラームをセット
            if (before(now)) {
                add(Calendar.DATE, 1)
            }
        }

        val requestCodeId = id.hashCode()
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("id", id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmCalendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelAlarm(id: String?) {
        if (id == null) return

        val requestCodeId = id.hashCode()
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
        alarmManager?.cancel(alarmPendingIntent)
    }
}
