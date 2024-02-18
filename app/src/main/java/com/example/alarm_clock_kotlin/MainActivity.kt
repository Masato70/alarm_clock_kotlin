package com.example.alarm_clock_kotlin

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarm_clock_kotlin.ui.theme.Alarm_clock_kotlinTheme



class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Alarm_clock_kotlinTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "homeScreen") {
                    composable("homeScreen") { HomeScreen(navController) }
                    composable("alarmTimePicker") { AlarmTimePicker() }
                }
            }        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    Alarm_clock_kotlinTheme {
//        Greeting("Android")
//    }
//}