package com.example.alarm_clock_kotlin

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import com.example.alarm_clock_kotlin.data.AlarmViewModel
import com.example.alarm_clock_kotlin.ui.theme.Alarm_clock_kotlinTheme
import com.example.alarm_clock_kotlin.utils.PermissionUtils
import com.example.alarm_clock_kotlin.view.AlarmTimePicker
import com.example.alarm_clock_kotlin.view.HomeScreen
import com.example.alarm_clock_kotlin.view.ShowTutorialIfNeeded
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var permissionUtils: PermissionUtils
    private lateinit var alarmViewModel: AlarmViewModel
    // BroadcastReceiverのインスタンスを生成
    private val alarmTriggeredReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            val alarmId = intent?.getStringExtra("alarm_id")
            alarmId?.let {
                // ViewModelの関数を呼び出し、スイッチをオフにする
                alarmViewModel.toggleSwitch(it, false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionUtils = PermissionUtils(this)
        permissionUtils.checkAndRequestAlarmPermission(this)
        permissionUtils.checkAndRequestNotificationPermission()
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        setContent {
            Log.d(TAG, "早いかも")
            Alarm_clock_kotlinTheme {
                val navController = rememberNavController()
                MyApp(navController)
            }
        }
        val filter = IntentFilter("com.example.ACTION_ALARM_TRIGGERED")
        LocalBroadcastManager.getInstance(this).registerReceiver(alarmTriggeredReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        // アクティビティ破棄時にReceiverを解除
        LocalBroadcastManager.getInstance(this).unregisterReceiver(alarmTriggeredReceiver)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp(navController: NavHostController) {
    ShowTutorialIfNeeded()
    NavHost(navController = navController, startDestination = "homeScreen") {
        composable("homeScreen") {
            HomeScreen(navController)
        }
        composable("alarmTimePicker/{parentId}") {
            val parentId = it.arguments?.getString("parentId")
            AlarmTimePicker(navController, parentId)
        }
        composable("alarmTimePicker") {
            AlarmTimePicker(navController, null)
        }
    }
}


@HiltAndroidApp
class MyApplication : Application() {
    // 必要に応じてカスタムの初期化コードをここに追加
}