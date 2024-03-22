package com.chibaminto.compactalarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Button


class AlarmStopActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_stop)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        val stopButton = findViewById<Button>(R.id.button_stop_alarm)

        stopButton.setOnClickListener {
            val alarmId = intent.getStringExtra("id")

            alarmId?.let {
                val notificationId = it.hashCode()
                NotificationManagerCompat.from(this).cancel(notificationId)
            }

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
        Log.d(TAG, "実行されているが")

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardLocked) {
            finish()
        }
    }
}


//class AlarmStopActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        // 画面をロック状態から解除するフラグ
//        Log.d(TAG, "ロック画面だから実行されてるよ")
//        window.addFlags(
//            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//        )
//
//        setContent {
//            AlarmStopScreen { alarmId ->
//                stopAlarm(alarmId)
//                finish()
//            }
//        }
//    }
//
//    private fun stopAlarm(alarmId: String?) {
//        Log.d(TAG, "ロック画面だから実行されてるよ!!!!!!")
//        alarmId?.let {
//            val notificationId = it.hashCode() // アラームIDから通知IDを生成
//            NotificationManagerCompat.from(this).cancel(notificationId)
//            Log.d("AlarmStopActivity", "Stopping alarm with ID: $alarmId")
//            // アラームを停止する処理
//            val stopIntent = Intent(this, AlarmReceiver::class.java).apply {
//                action = "STOP_ALARM"
//                putExtra("id", alarmId)
//            }
//            sendBroadcast(stopIntent)
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // ロック画面が解除されているかどうかを確認
//        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//        if (!keyguardManager.isKeyguardLocked) {
//            // ロックが解除されている場合、アクティビティを終了
//            finish()
//        }
//    }
//}
//
//@Composable
//fun AlarmStopScreen(onStopClick: (String?) -> Unit) {
//    val alarmId = "取得する方法に応じて変更" // Intent から alarmId を取得する方法に置き換える
//
//    Button(onClick = { onStopClick(alarmId) }) {
//        Text(text = "アラームを停止")
//    }
//}