package com.chibaminto.compactalarm.view

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import com.chibaminto.compactalarm.R


@Composable
    fun ShowTutorialIfNeeded() {
        val context = LocalContext.current
        val showTutorial = remember { mutableStateOf(false) }

        LaunchedEffect(key1 = Unit) {
            showTutorial.value = isFirstTimeOpeningApp(context)
        }

        if (showTutorial.value) {
            Dialog(onDismissRequest = { showTutorial.value = false }) {
                // チュートリアルの画像を表示
                Image(
                    painter = painterResource(id = R.drawable.tutorial),
                    contentDescription = "Tutorial"
                )
            }
        }
    }

    fun isFirstTimeOpeningApp(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("IsFirstTime", true)
        if (isFirstTime) {
            sharedPreferences.edit().apply {
                putBoolean("IsFirstTime", false)
                apply()
            }
        }
        return isFirstTime
    }