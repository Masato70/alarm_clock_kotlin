package com.example.alarm_clock_kotlin

import androidx.compose.runtime.Immutable
import java.time.LocalTime

@Immutable
data class CardData(
    val id: String,
    val isParent: Boolean?,
    val childId: String?,
    val alarmTime: LocalTime,
    val switchValue: Boolean
)