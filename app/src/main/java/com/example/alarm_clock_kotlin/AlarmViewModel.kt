package com.example.alarm_clock_kotlin

import android.content.Context
import android.util.Log
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

class AlarmViewModel : ViewModel() {
    private val _cards = MutableStateFlow<List<CardData>>(emptyList())
    val cards: StateFlow<List<CardData>> = _cards

    companion object {
        const val TAG = "AlarmViewModel"
    }
    fun addCard(card: CardData) {
        viewModelScope.launch {
            val newList = _cards.value.toMutableList()
            newList.add(card)
            _cards.value = newList
            Log.d(TAG, "addCard: カード追加後のリストサイズ: ${_cards.value.size}")
            Log.d(TAG, "addCard: カード追加: ${_cards.value}")
        }
    }


    fun removeCard(cardId: String) {
        viewModelScope.launch {
            _cards.value = _cards.value.filterNot { it.id == cardId }
        }
    }

    fun toggleSwitch(cardId: String, isChecked: Boolean) {
        viewModelScope.launch {
            _cards.value = _cards.value.map { card ->
                if (card.id == cardId) card.copy(switchValue = isChecked) else card
            }
        }
    }
}
