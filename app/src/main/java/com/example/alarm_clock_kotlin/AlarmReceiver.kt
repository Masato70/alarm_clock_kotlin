package com.example.alarm_clock_kotlin

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean

class AlarmReceiver : BroadcastReceiver() {
    private val isAlarmPlayed = AtomicBoolean(false)
    private val CHANNEL_Id = "${BuildConfig.APPLICATION_ID}.test"

    object MediaPlayerSingleton {
        var mediaPlayer: MediaPlayer? = null
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "STOP_ALARM" -> handleStopAlarmAction(context, intent)
//            Intent.ACTION_MY_PACKAGE_REPLACED -> processAlarmData(context)
//            Intent.ACTION_BOOT_COMPLETED -> processAlarmData(context)
            else -> {
                val setAlarmId = intent.getStringExtra("id")
                startAlarmProcess(context, setAlarmId)
            }
        }
    }

    private fun handleStopAlarmAction(context: Context, intent: Intent) {
        Log.d(ContentValues.TAG, "アラームをストップします！ on thread ${Thread.currentThread().id}")
        stopAlarm()

        val setAlarmId = intent.getStringExtra("id")
        if (setAlarmId != null) {
            cancelNotification(context, setAlarmId)
            Log.d(ContentValues.TAG, "Canceling notification for alarm ID: $setAlarmId")
        } else {
            Log.d(ContentValues.TAG, "STOP_ALARM action received, but no alarm ID found.")
        }
    }

    private fun cancelNotification(context: Context, alarmId: String) {
        val notificationId = alarmId.hashCode()
        getNotificationManager(context)?.cancel(notificationId)
    }

    private fun getNotificationManager(context: Context): NotificationManager? {
        return ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as? NotificationManager
    }

    private fun startAlarmProcess(context: Context, setAlarmId: String?) {
        if (setAlarmId == null) {
            return
        }
        try {
            if (isAlarmPlayed.compareAndSet(false, true)) {
                stopAlarm()
                startAlarm(context, setAlarmId)
                showNotificationBasedOnLockStatus(context, setAlarmId)

            }
        } catch (e: Exception) {
            Log.d(ContentValues.TAG, "Exception", e)
        }
    }


    private fun startAlarm(context: Context, newAlarmId: String) {
        try {
            Log.d(TAG, "アラームスタート")
            // 既存のMediaPlayerがあればリリース
            MediaPlayerSingleton.mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            MediaPlayerSingleton.mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmSound)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            Log.d(
                "AlarmReceiver",
                "mediaPlayer created: $MediaPlayerSingleton.mediaPlayer on thread ${Thread.currentThread().id}"
            )
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error playing alarm sound", e)
        }
    }

    private fun stopAlarm() {
        try {
            MediaPlayerSingleton.mediaPlayer?.let { mediaPlayer ->
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.reset()
                mediaPlayer.release()
                MediaPlayerSingleton.mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error stopping alarm", e)
        }
    }

    private fun showNotification(context: Context, setAlarmId: String) {

        val notificationId = setAlarmId.hashCode()
        Log.d(ContentValues.TAG, "通知を表示させます！ on thread ${Thread.currentThread().id}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Channel"
            val descriptionText = "This is my channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_Id, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, AlarmStopActivity::class.java).apply {
            putExtra("id", setAlarmId)
            Log.d(ContentValues.TAG, "setAlarmIdはこちら $setAlarmId")
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(context, CHANNEL_Id)
//                .setContentTitle(context.getString(R.string.alarm))
                .setContentTitle("タイトル")
//                .setContentText(context.getString(R.string.swipe_to_stop_alarm))
                .setContentText("ああああ")
                .setContentText("スワイプして！")
                .setSmallIcon(R.drawable.icon_115930_256)
                .setAutoCancel(true)
                .setColor(Color.GRAY)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(getPendingIntent(context))
                .setDeleteIntent(getPendingIntent(context))
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "STOP_ALARM"
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun showNotificationBasedOnLockStatus(context: Context, setAlarmId: String) {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isLocked = keyguardManager.isKeyguardLocked
        showNotification(context, setAlarmId)

        Log.d(ContentValues.TAG, if (isLocked) "ロック画面" else "ロック画面じゃない！")
    }
}