package com.example.alarm_clock_kotlin

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModelStoreOwner = context as ViewModelStoreOwner
    val viewModel: AlarmViewModel = viewModel(viewModelStoreOwner = viewModelStoreOwner)

    // 親アラームカードを時刻順にソート
    val parentCards = viewModel.cards.collectAsState().value
        .filter { it.isParent == true }
        .sortedBy { it.alarmTime }

    LazyColumn(
        modifier = Modifier
            .background(color = Color.Black)
            .fillMaxSize()
    ) {
        items(parentCards, key = { card -> card.id }) { card ->
            AlarmCard(navController = navController, cardData = card, viewModel = viewModel)
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


