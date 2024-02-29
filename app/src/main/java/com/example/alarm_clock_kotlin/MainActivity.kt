package com.example.alarm_clock_kotlin

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.annotation.RequiresApi
import androidx.navigation.NavHostController
import com.example.alarm_clock_kotlin.ui.theme.Alarm_clock_kotlinTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var permissionUtils: PermissionUtils
    private lateinit var alarmViewModel: AlarmViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionUtils = PermissionUtils(this)
        permissionUtils.checkAndRequestNotificationPermission()
        permissionUtils.checkAndRequestAlarmPermission()


        setContent {
            Alarm_clock_kotlinTheme {
                val navController = rememberNavController()
                MyApp(navController)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp(navController: NavHostController) {
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