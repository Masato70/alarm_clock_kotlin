package com.example.alarm_clock_kotlin.view

import android.content.ContentValues
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm_clock_kotlin.R
//import com.example.alarm_clock_kotlin.R
import com.example.alarm_clock_kotlin.data.AlarmViewModel
import com.example.alarm_clock_kotlin.data.CardData


@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmCard(navController: NavController, cardData: CardData, viewModel: AlarmViewModel) {
    val childAlarms = viewModel.getChildAlarms(cardData.id).sortedBy { it.alarmTime }

    var isSwipedToEnd by remember { mutableStateOf(false) }

    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToEnd) {
                viewModel.removeCard(cardData.id)
                isSwipedToEnd = true
                true
            } else {
                false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd),
        dismissThresholds = { FractionalThreshold(0.5f) },
        background = {
            val color = when {
                isSwipedToEnd -> Color.Black
                dismissState.dismissDirection == DismissDirection.StartToEnd -> Color.Red
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(start = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
            }
        },
        dismissContent = {
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
                            text = cardData.alarmTime.toString(),
                            style = TextStyle(fontWeight = FontWeight.Bold),
                            color = if (cardData.switchValue) Color.White else Color.Gray,
                            fontSize = 60.sp,
                        )
                        Switch(
                            checked = cardData.switchValue,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleSwitch(
                                    cardData.id,
                                    isChecked
                                )
                            },
                            modifier = Modifier
                                .align(CenterVertically)
                                .scale(1.2f)

                        )
                    }
                    TextButton(
                        onClick = {
                            val route = "alarmTimePicker/${cardData.id}"
                            navController.navigate(route)
                        },
                        Modifier.padding(start = 15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "追加",
                            tint = Color(0xFF00FFFF)
                        )
                        Text(
                            text = stringResource(R.string.add_time),
                            color = Color(0xFF00FFFF),
                        )
                    }


                    childAlarms.forEach { child ->
                        val childDismissState = rememberDismissState(
                            confirmStateChange = {
                                if (it == DismissValue.DismissedToEnd) {
                                    viewModel.removeCard(child.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        SwipeToDismiss(
                            state = childDismissState,
                            directions = setOf(DismissDirection.StartToEnd),
                            dismissThresholds = { FractionalThreshold(0.25f) },
                            background = {
                                val backgroundColor =
                                    if (childDismissState.dismissDirection == DismissDirection.StartToEnd) Color.Red else Color.Transparent
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(backgroundColor)
                                        .padding(start = 20.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                }
                            },
                            dismissContent = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 25.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = child.alarmTime.toString(),
                                        style = TextStyle(fontWeight = FontWeight.Bold),
                                        color = if (child.switchValue) Color.White else Color.Gray,
                                        fontSize = 50.sp,
                                    )
                                    Switch(
                                        checked = child.switchValue,
                                        onCheckedChange = { isChecked ->
                                            viewModel.toggleSwitch(child.id, isChecked)
                                        },
                                        modifier = Modifier
                                            .align(CenterVertically)
                                            .scale(1.2f)
                                    )
                                }
                            }
                        )
                    }

                }
            }
        }
    )
}
