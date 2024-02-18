package com.example.alarm_clock_kotlin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalTime

class AlarmViewModel : ViewModel() {
    var cards: List<CardData> by mutableStateOf(listOf())
        private set

    fun addCard(card: CardData) {
        cards = cards + card
    }

    fun removeCard(cardId: String) {
        cards = cards.filterNot { it.id == cardId }
    }

    fun toggleSwitch(cardId: String) {
        cards = cards.map {
            if (it.id == cardId) it.copy(switchValue = !it.switchValue) else it
        }
    }

    fun addChildToCard(parentId: String, childTime: LocalTime) {
        val childCard = CardData(
            id = java.util.UUID.randomUUID().toString(),
            isParent = false,
            childId = parentId,
            alarmTime = childTime,
            switchValue = true
        )
        addCard(childCard)
    }
}
