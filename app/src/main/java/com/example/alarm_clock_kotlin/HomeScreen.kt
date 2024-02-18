package com.example.alarm_clock_kotlin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.alarm_clock_kotlin.ui.theme.Alarm_clock_kotlinTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController) {

    LazyColumn(
        Modifier
            .background(color = Color.Black)
            .fillMaxSize()
    ) {
        item {
            alarmCard()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        IconButton(
            onClick = { navController.navigate("alarmTimePicker") },
            modifier = Modifier
                .size(80.dp)
                .background(color = Color.Cyan, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "alarm set",
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }
    }

}


//@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    Alarm_clock_kotlinTheme {
//        HomeScreen()
//    }
//}