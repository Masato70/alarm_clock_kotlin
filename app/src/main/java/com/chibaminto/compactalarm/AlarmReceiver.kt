package com.chibaminto.compactalarm

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
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
import java.util.concurrent.atomic.AtomicBoolean

class AlarmReceiver : BroadcastReceiver() {
    private val isAlarmPlayed = AtomicBoolean(false)
//    private val CHANNEL_Id = "${BuildConfig.APPLICATION_ID}.test"
    private val CHANNEL_Id = "11"

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
                val setAlarmId = intent.getStringExtra("id")
                startAlarmProcess(context, setAlarmId)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processAlarmData(context: Context) {
        // CardRepositoryを初期化（この例では、手動で初期化しています）
        val cardRepository = CardRepository(context)

        // ここでは、コルーチンを使って非同期操作を行います
        CoroutineScope(Dispatchers.IO).launch {
            val cards = cardRepository.cards.first() // 最新のカードデータを取得

            // スイッチがオンになっているカードに対してアラームをセット
            cards.filter { it.switchValue }.forEach { card ->
                val alarmManagerHelper = AlarmManagerHelper(context)
                alarmManagerHelper.setAlarm(card.id, card.alarmTime)
            }
        }
    }

    private fun handleStopAlarmAction(context: Context, intent: Intent) {
        stopAlarm()

        val setAlarmId = intent.getStringExtra("id")
        if (setAlarmId != null) {
            cancelNotification(context, setAlarmId)
        } else {
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
        if (setAlarmId == null)  return
        try {
            if (isAlarmPlayed.compareAndSet(false, true)) {
                stopAlarm()
                startAlarm(context, setAlarmId)
                showNotificationBasedOnLockStatus(context, setAlarmId)
                toggleSwitch(context, setAlarmId, false)
            }
        } catch (e: Exception) {
        }
    }


    private fun startAlarm(context: Context, newAlarmId: String) {
        try {
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

        val fullScreenIntent = Intent(context, AlarmStopActivity::class.java).apply {}
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(context, CHANNEL_Id)
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

//        Log.d(ContentValues.TAG, if (isLocked) "ロック画面" else "ロック画面じゃない！")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleSwitch(context: Context, cardId: String, isChecked: Boolean) {
        GlobalScope.launch {
            val gson: Gson = AppDataStore.provideGson() // GsonのインスタンスをAppDataStoreから取得
            val dataStore = context.dataStore // DataStoreのインスタンスをAppDataStoreから取得
            val cardsKey = AppDataStore.provideCardsKey() // CARDS_KEYをAppDataStoreから取得

            val preferences = dataStore.data.first() // 現在のPreferencesを取得

            // 現在保存されているカードデータを取得
            val cardsJson = preferences[cardsKey] ?: ""
            val type = object : TypeToken<List<CardData>>() {}.type
            var cards: List<CardData> = if (cardsJson.isNotEmpty()) gson.fromJson(cardsJson, type) else listOf()

            // カードのスイッチ状態を更新
            cards = cards.map { card ->
                if (card.id == cardId) {
                    card.copy(switchValue = isChecked)
                } else {
                    card
                }
            }

            // 更新されたカードデータをDataStoreに保存
            val updatedCardsJson = gson.toJson(cards)
            dataStore.edit { settings ->
                settings[cardsKey] = updatedCardsJson
            }
        }
    }
}