package com.example.alarm_clock_kotlin

import android.app.Activity
import android.app.KeyguardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.NotificationManagerCompat

class AlarmStopActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_stop)

        // 画面をロック状態から解除するためのフラグ
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        val stopButton = findViewById<Button>(R.id.button_stop_alarm)

        stopButton.setOnClickListener {
            val alarmId = intent.getStringExtra("id")

            alarmId?.let {
                val notificationId = it.hashCode() // アラームIDから通知IDを生成
                NotificationManagerCompat.from(this).cancel(notificationId)
            }

            Log.d(TAG, "Stopping alarm with ID: $alarmId")
            // アラームを停止する処理

            val stopIntent = Intent(this, AlarmReceiver::class.java).apply {
                action = "STOP_ALARM"
                putExtra("id", alarmId)
            }
            sendBroadcast(stopIntent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        // ロック画面が解除されているかどうかを確認
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardLocked) {
            // ロックが解除されている場合、アクティビティを終了
            finish()
        }
    }
}
