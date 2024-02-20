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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmTimePicker(navController: NavController,  viewModel: AlarmViewModel = viewModel()) {
    var time by remember { mutableStateOf(LocalTime.now()) }
    val context = LocalContext.current
//    val viewModel: AlarmViewModel = viewModel()

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
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            modifier = Modifier.clickable(onClick = { showTimePicker() }),
            color = Color.White,
            style = TextStyle(fontSize = 100.sp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val newCard = CardData(
                    id = UUID.randomUUID().toString(),
                    isParent = true,
                    childId = null,
                    alarmTime = time,
                    switchValue = false
                )
                viewModel.addCard(newCard)
//                navController.navigate("homeScreen")
            },
            modifier = Modifier
                .padding(16.dp)
                .height(60.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
        ) {
            Text(
                "セットする",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}


