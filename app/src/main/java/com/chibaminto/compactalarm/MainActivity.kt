package com.chibaminto.compactalarm

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import com.chibaminto.compactalarm.data.AlarmViewModel
import com.chibaminto.compactalarm.ui.theme.Alarm_clock_kotlinTheme
import com.chibaminto.compactalarm.utils.PermissionUtils
import com.chibaminto.compactalarm.view.AlarmTimePicker
import com.chibaminto.compactalarm.view.HomeScreen
import com.chibaminto.compactalarm.view.ShowTutorialIfNeeded
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var permissionUtils: PermissionUtils
    private lateinit var alarmViewModel: AlarmViewModel
    private val alarmTriggeredReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            val alarmId = intent?.getStringExtra("alarm_id")
            alarmId?.let {
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
            Alarm_clock_kotlinTheme {
                val navController = rememberNavController()
                MyApp(navController)
            }
        }

        val filter = IntentFilter("com.chibamint.ACTION_ALARM_TRIGGERED")
        LocalBroadcastManager.getInstance(this).registerReceiver(alarmTriggeredReceiver, filter)

        checkAndRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(alarmTriggeredReceiver)
    }

    private fun checkAndRequestPermissions() {
        lifecycleScope.launch {
            permissionUtils.checkAndRequestAlarmPermission(this@MainActivity)
            permissionUtils.checkAndRequestNotificationPermission()
        }
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
}