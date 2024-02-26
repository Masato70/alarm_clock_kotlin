package com.example.alarm_clock_kotlin

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
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
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val cardRepository: CardRepository,
) : ViewModel() {
    private val _cards = MutableStateFlow<List<CardData>>(emptyList())
    val cards: StateFlow<List<CardData>> = _cards

    init {
        loadCards()
    }

    fun addCard(card: CardData) {
        updateAndSaveCards { it + card }
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
    }

    fun removeCard(cardId: String) {
        updateAndSaveCards { it.filterNot { card -> card.id == cardId || card.childId == cardId } }
    }

    fun toggleSwitch(cardId: String, isChecked: Boolean) {
        updateAndSaveCards { cards ->
            cards.map { card ->
                if (card.id == cardId) card.copy(switchValue = isChecked) else card
            }
        }
    }

    private fun loadCards() {
        viewModelScope.launch {
            cardRepository.cards.collect { cardsList ->
                _cards.value = cardsList
            }
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

    companion object {
        private val CARDS_KEY = stringPreferencesKey("cards_data")

        @RequiresApi(Build.VERSION_CODES.O)
        private fun provideGson(): Gson = GsonBuilder()
            .registerTypeAdapter(LocalTime::class.java, JsonSerializer<LocalTime> { src, _, _ ->
                JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME))
            })
            .registerTypeAdapter(LocalTime::class.java, JsonDeserializer { json, _, _ ->
                LocalTime.parse(json.asJsonPrimitive.asString, DateTimeFormatter.ISO_LOCAL_TIME)
            })
            .create()
    }

    private val gson: Gson = provideGson()
    private val Context.cardDataStore by preferencesDataStore(name = "card_data_store")

    suspend fun saveCards(cards: List<CardData>) {
        val jsonData = gson.toJson(cards)
        context.cardDataStore.edit { preferences ->
            preferences[CARDS_KEY] = jsonData
        }
    }

    val cards: Flow<List<CardData>> = context.cardDataStore.data
        .map { preferences ->
            val jsonData = preferences[CARDS_KEY] ?: return@map emptyList<CardData>()
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
