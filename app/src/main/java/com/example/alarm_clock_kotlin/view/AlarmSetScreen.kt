package com.example.alarm_clock_kotlin.view

import android.os.Build
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
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.alarm_clock_kotlin.data.AlarmViewModel
import com.example.alarm_clock_kotlin.data.CardData
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmTimePicker(navController: NavController, parentId: String? = null) {
    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }
    val context = LocalContext.current

    val viewModelStoreOwner = context as ViewModelStoreOwner
    val viewModel: AlarmViewModel = viewModel(viewModelStoreOwner = viewModelStoreOwner)

    fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(dateTime.hour)
            .setMinute(dateTime.minute)
            .setTitleText("Select Alarm Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            dateTime = dateTime.withHour(picker.hour).withMinute(picker.minute)
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
            text = dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            modifier = Modifier.clickable(onClick = { showTimePicker() }),
            color = Color.White,
            style = TextStyle(fontSize = 100.sp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val timeOnly = dateTime.toLocalTime().withSecond(0).withNano(0)
                if (parentId == null) {
                    val newCard = CardData(
                        id = UUID.randomUUID().toString(),
                        isParent = true,
                        childId = null,
                        alarmTime = timeOnly,
                        switchValue = true
                    )
                    viewModel.addParentCard(newCard)
                } else {
                    viewModel.addChildCard(parentId, dateTime.toLocalTime())
                }
                navController.navigate("homeScreen")
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

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .padding(16.dp)
                .height(60.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
        ) {
            Text(
                "戻る",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}


