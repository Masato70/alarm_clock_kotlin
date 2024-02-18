package com.example.alarm_clock_kotlin

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm_clock_kotlin.ui.theme.Alarm_clock_kotlinTheme

@Composable
fun alarmCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        backgroundColor = Color.Gray.copy(alpha = 0.2f)
    ) {
        Column(
            Modifier.padding(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "02:00",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    fontSize = 60.sp,
//                    fontSize = if (switchValue) 50.sp else 49.sp,
//                    color = if (switchValue) Color.White else Color.Gray
                )
                Switch(
                    checked = null == true,
                    onCheckedChange = {},
                    modifier = Modifier
                        .align(CenterVertically)
                        .scale(1.2f)

                )
            }
            TextButton(onClick = { /*TODO*/ }, Modifier.padding(start = 15.dp)) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "追加",
                    tint = Color(0xFF00FFFF)
                )
                Text(text = "時間を追加する", color = Color(0xFF00FFFF))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "01:50",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    fontSize = 50.sp,
//                    fontSize = if (switchValue) 50.sp else 49.sp,
//                    color = if (switchValue) Color.White else Color.Gray
                )
                Switch(
                    checked = null == true,
                    onCheckedChange = {},
                    modifier = Modifier
                        .align(CenterVertically)
                        .scale(1.2f)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewAlarmCard() {
    Alarm_clock_kotlinTheme {
        alarmCard()
    }
}