package com.chibaminto.compactalarm.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chibaminto.compactalarm.utils.AlarmManagerHelper
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmManagerHelper: AlarmManagerHelper,
    private val cardRepository: CardRepository,
) : ViewModel() {
    private val _cards = MutableStateFlow<List<CardData>>(emptyList())
    val cards: StateFlow<List<CardData>> = _cards

    init {
        loadCards()
    }

    fun addParentCard(card: CardData) {
        viewModelScope.launch {
            updateAndSaveCards { it + card }
            alarmManagerHelper.setAlarm(card.id, card.alarmTime)
        }
    }

    fun addChildCard(parentId: String, childAlarmTime: LocalTime) {
        val childCard = CardData(
            id = UUID.randomUUID().toString(),
            isParent = false,
            childId = parentId,
            alarmTime = childAlarmTime.withSecond(0).withNano(0),
            switchValue = true
        )
        updateAndSaveCards { it + childCard }
        alarmManagerHelper.setAlarm(childCard.id, childCard.alarmTime)
    }

    fun removeCard(cardId: String) {
        val idsToCancelAlarm = mutableListOf<String>()

        // カードをフィルタリングして削除し、削除対象のカードのIDを取得
        updateAndSaveCards { cards ->
            cards.filterNot { card ->
                val isTargetCard = card.id == cardId || card.childId == cardId
                if (isTargetCard) {
                    // 削除するカードが見つかった場合、アラームをキャンセルするためのIDリストに追加
                    idsToCancelAlarm.add(card.id)
                }
                isTargetCard
            }
        }

        // 特定したすべてのカードのアラームをキャンセル
        idsToCancelAlarm.forEach { id ->
            alarmManagerHelper.cancelAlarm(id)
        }
    }


    fun toggleSwitch(cardId: String, isChecked: Boolean) {
        viewModelScope.launch {
            val updatedCards = _cards.value.map { card ->
                if (card.id == cardId || card.childId == cardId) {
                    if (isChecked) {
                        alarmManagerHelper.setAlarm(card.id, card.alarmTime)
                    } else {
                        alarmManagerHelper.cancelAlarm(card.id)
                    }
                    card.copy(switchValue = isChecked)
                } else {
                    card
                }
            }
            _cards.value = updatedCards
            cardRepository.saveCards(updatedCards)

            // If the toggled card is a parent, update all its children
            if (_cards.value.any { it.id == cardId && it.isParent!! }) {
                val children = _cards.value.filter { it.childId == cardId }
                children.forEach { child ->
                    toggleSwitch(child.id, isChecked) // This will set or cancel the alarm for each child
                }
            }
        }
    }

     fun loadCards() {
        viewModelScope.launch {
            cardRepository.cards.collect { cardsList ->
                _cards.value = cardsList
                setAlarmsForSwitchedOnCards(cardsList)
            }
        }
    }

     fun setAlarmsForSwitchedOnCards(cardsList: List<CardData>) {
        cardsList.filter { it.switchValue }
            .forEach { card ->
                alarmManagerHelper.setAlarm(card.id, card.alarmTime)
            }
    }

    fun getChildAlarms(parentId: String): List<CardData> {
        return _cards.value.filter { it.childId == parentId }
    }

    private fun updateAndSaveCards(updateFunction: (List<CardData>) -> List<CardData>) {
        viewModelScope.launch {
            _cards.value = updateFunction(_cards.value)
            cardRepository.saveCards(_cards.value)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
class CardRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val gson: Gson = AppDataStore.provideGson()
    private val cardsKey = AppDataStore.provideCardsKey()

    suspend fun saveCards(cards: List<CardData>) {
        val jsonData = gson.toJson(cards)
        // 修正: AppDataStore.dataStore(context) -> context.dataStore
        context.dataStore.edit { preferences ->
            preferences[cardsKey] = jsonData
        }
    }

    val cards: Flow<List<CardData>> = context.dataStore.data.map { preferences ->
        val jsonData = preferences[cardsKey] ?: return@map emptyList<CardData>()
        parseCardsJson(jsonData)
    }

    private fun parseCardsJson(jsonData: String): List<CardData> {
        return try {
            val type = object : TypeToken<List<CardData>>() {}.type
            gson.fromJson(jsonData, type)
        } catch (e: JsonSyntaxException) {
            Log.e("CardRepository", "JSONのパース中にエラーが発生しました: ", e)
            emptyList()
        }
    }
}
