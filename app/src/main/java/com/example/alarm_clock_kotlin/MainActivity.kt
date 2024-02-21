package com.example.alarm_clock_kotlin

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.alarm_clock_kotlin.ui.theme.Alarm_clock_kotlinTheme

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // アクティビティスコープでViewModelを取得
        val alarmViewModel: AlarmViewModel by viewModels()

        setContent {
            Alarm_clock_kotlinTheme {
                // NavControllerのインスタンスを作成
                val navController = rememberNavController()
                // NavHostをセットアップし、viewModelを各画面に渡す
                MyApp(navController,)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp(navController: NavHostController, ) {
    NavHost(navController = navController, startDestination = "homeScreen") {
        composable("homeScreen") {
            HomeScreen(navController)
        }
        composable("alarmTimePicker") {
            AlarmTimePicker(navController)
        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    Alarm_clock_kotlinTheme {
//        Greeting("Android")
//    }
//}