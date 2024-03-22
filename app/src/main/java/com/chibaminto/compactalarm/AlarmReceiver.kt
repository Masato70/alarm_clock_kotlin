package com.chibaminto.compactalarm

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import com.chibaminto.compactalarm.data.AppDataStore
import com.chibaminto.compactalarm.data.CardData
import com.chibaminto.compactalarm.data.CardRepository
import com.chibaminto.compactalarm.data.dataStore
import com.chibaminto.compactalarm.utils.AlarmManagerHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class AlarmReceiver : BroadcastReceiver() {
    private val isAlarmPlayed = AtomicBoolean(false)
    private val CHANNEL_ID = "11"

    object MediaPlayerSingleton {
        var mediaPlayer: MediaPlayer? = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "STOP_ALARM" -> handleStopAlarmAction(context, intent)
            Intent.ACTION_MY_PACKAGE_REPLACED -> processAlarmData(context)
            Intent.ACTION_BOOT_COMPLETED -> processAlarmData(context)
            else -> {
                intent.getStringExtra("id")?.let { startAlarmProcess(context, it) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processAlarmData(context: Context) {
        val cardRepository = CardRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            val cards = cardRepository.cards.first()

            cards.filter { it.switchValue }.forEach { card ->
                val alarmManagerHelper = AlarmManagerHelper(context)
                alarmManagerHelper.setAlarm(card.id, card.alarmTime)
            }
        }
    }

    private fun handleStopAlarmAction(context: Context, intent: Intent) {
        stopAlarm()
        stopVibrate(context)

        intent.getStringExtra("id")?.let {
            cancelNotification(context, it)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startAlarmProcess(context: Context, setAlarmId: String?) {
        setAlarmId ?: return

        try {
            if (isAlarmPlayed.compareAndSet(false, true)) {
                stopAlarm()
                startAlarm(context, setAlarmId)
                startVibrate(context)
                showNotificationBasedOnLockStatus(context, setAlarmId)
                toggleSwitch(context, setAlarmId, false)
            }
        } catch (e: Exception) {
            Log.e("AlarmProcess", "アラームがトリガーされるプロセスでエラーになりました。", e)
        }
    }

    private fun startAlarm(context: Context, newAlarmId: String) {
        MediaPlayerSingleton.mediaPlayer?.let {
            it.stop()
            it.start()
        }

        try {
            MediaPlayerSingleton.mediaPlayer = initializeMediaPlayer(context)
            MediaPlayerSingleton.mediaPlayer?.start()
        } catch (e: Exception) {
            Log.d("startAlarm", "アラームサウンドのスタートに失敗しました。")
        }
    }

    private fun startVibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 1000, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val vibrationAttributes = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_ALARM)
                .build()

            val vibrationEffect = VibrationEffect.createWaveform(pattern, 0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(vibrationEffect, vibrationAttributes)
            }
        } else {
            vibrator.vibrate(pattern, 0)
        }
    }

    private fun stopVibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
    }

    private fun initializeMediaPlayer(context: Context): MediaPlayer? {
        return MediaPlayer().apply {
            try {
                val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setDataSource(context, alarmSoundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
            } catch (e: IOException) {
                Log.e("initializeMediaPlayer", "MediaPlayeのエラーで失敗しました。r", e)
                release()
                return null
            }
        }
    }

    private fun stopAlarm() {
        MediaPlayerSingleton.mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
                reset()
                release()
            } catch (e: Exception) {
                Log.e("stopAlarm", "アラームがストップできませんでした。", e)
            } finally {
                MediaPlayerSingleton.mediaPlayer = null
            }
        }
    }

    private fun showNotification(context: Context, setAlarmId: String) {
        createNotificationChannel(context)

        val notificationId = setAlarmId.hashCode()

        val fullScreenIntent = Intent(context, AlarmStopActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.alarm))
            .setContentText(context.getString(R.string.swipe_to_stop_alarm))
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

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Channel"
            val descriptionText = "This is my channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleSwitch(context: Context, cardId: String, isChecked: Boolean) {
        GlobalScope.launch {
            val gson: Gson = AppDataStore.provideGson()
            val dataStore = context.dataStore
            val cardsKey = AppDataStore.provideCardsKey()

            val preferences = dataStore.data.first()

            val cardsJson = preferences[cardsKey] ?: ""
            val type = object : TypeToken<List<CardData>>() {}.type
            var cards: List<CardData> =
                if (cardsJson.isNotEmpty()) gson.fromJson(cardsJson, type) else listOf()

            cards = cards.map { card ->
                if (card.id == cardId) {
                    card.copy(switchValue = isChecked)
                } else {
                    card
                }
            }

            val updatedCardsJson = gson.toJson(cards)
            dataStore.edit { settings ->
                settings[cardsKey] = updatedCardsJson
            }
        }
    }
}