package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.InterventionMode

class FeedSurgeonConverters {
    @TypeConverter
    fun fromInterventionMode(mode: InterventionMode): String = mode.name

    @TypeConverter
    fun toInterventionMode(value: String): InterventionMode =
        InterventionMode.entries.firstOrNull { it.name == value } ?: InterventionMode.BACK
}
