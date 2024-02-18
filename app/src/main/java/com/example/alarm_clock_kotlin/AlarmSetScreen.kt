package com.example.alarm_clock_kotlin

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmTimePicker() {
    var time by remember { mutableStateOf(LocalTime.now()) }
    val context = LocalContext.current

    fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(time.hour)
            .setMinute(time.minute)
            .setTitleText("Select Alarm Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            time = LocalTime.of(picker.hour, picker.minute)
        }

        picker.show((context as FragmentActivity).supportFragmentManager, "tag")
    }

    LaunchedEffect(key1 = true) {
        showTimePicker()
    }

    Column(
        modifier = Modifier
            .fillMaxSize() // 画面全体を有効にする
            .background(Color.Black), // 背景色を黒に設定
        horizontalAlignment = Alignment.CenterHorizontally, // 水平方向の中央揃え
        verticalArrangement = Arrangement.Center // 垂直方向の中央揃え
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            modifier = Modifier.clickable(onClick = { showTimePicker() }),
            color = Color.White, // テキストの色を白に設定
            style = TextStyle(fontSize = 100.sp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                // ボタンがクリックされたときの動作
            },
            modifier = Modifier
                .padding(16.dp) // ボタンの周りに余白を追加
                .height(60.dp) // ボタンの高さを指定
                .fillMaxWidth(), // ボタンの幅を最大にする
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan), // ボタンの背景色を水色に設定
        ) {
            Text(
                "セットする",
                style = TextStyle(
                    fontSize = 20.sp, // テキストのフォントサイズを大きくする
                    fontWeight = FontWeight.Bold // テキストを太くする
                )
            )
        }

    }
}


