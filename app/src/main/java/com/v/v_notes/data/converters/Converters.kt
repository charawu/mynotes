package com.v.v_notes.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.v.v_notes.data.model.TodoItem
import java.util.Date

class Converters {
    private val gson = Gson()

    // TodoItem列表转换
    @TypeConverter
    fun fromTodoItemList(value: List<TodoItem>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTodoItemList(value: String): List<TodoItem> {
        return try {
            val listType = object : TypeToken<List<TodoItem>>() {}.type
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // String列表转换（用于imageUris）
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Date转换
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}