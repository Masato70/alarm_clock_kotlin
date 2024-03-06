package com.example.alarm_clock_kotlin

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "card_data_store")

object AppDataStore {
    private val CARDS_KEY = stringPreferencesKey("cards_data")

    fun provideCardsKey() = CARDS_KEY

    @RequiresApi(Build.VERSION_CODES.O)
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, JsonSerializer<LocalTime> { src, _, _ ->
            JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME))
        })
        .registerTypeAdapter(LocalTime::class.java, JsonDeserializer { json, _, _ ->
            LocalTime.parse(json.asJsonPrimitive.asString, DateTimeFormatter.ISO_LOCAL_TIME)
        })
        .create()
}
