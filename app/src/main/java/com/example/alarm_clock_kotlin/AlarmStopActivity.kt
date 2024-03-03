package com.example.alarm_clock_kotlin

import android.app.KeyguardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationManagerCompat
import com.example.alarm_clock_kotlin.AlarmReceiver
import android.Manifest
import android.app.Activity
import android.widget.Button
import com.example.alarm_clock_kotlin.R

class AlarmStopActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 画面をロック状態から解除するフラグ
        Log.d(TAG, "ロック画面だから実行されてるよ")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContent {
            AlarmStopScreen { alarmId ->
                stopAlarm(alarmId)
                finish()
            }
        }
    }

    private fun stopAlarm(alarmId: String?) {
        Log.d(TAG, "ロック画面だから実行されてるよ!!!!!!")
        alarmId?.let {
            val notificationId = it.hashCode() // アラームIDから通知IDを生成
            NotificationManagerCompat.from(this).cancel(notificationId)
            Log.d("AlarmStopActivity", "Stopping alarm with ID: $alarmId")
            // アラームを停止する処理
            val stopIntent = Intent(this, AlarmReceiver::class.java).apply {
                action = "STOP_ALARM"
                putExtra("id", alarmId)
            }
            sendBroadcast(stopIntent)
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

@Composable
fun AlarmStopScreen(onStopClick: (String?) -> Unit) {
    val alarmId = "取得する方法に応じて変更" // Intent から alarmId を取得する方法に置き換える

    Button(onClick = { onStopClick(alarmId) }) {
        Text(text = "アラームを停止")
    }
}