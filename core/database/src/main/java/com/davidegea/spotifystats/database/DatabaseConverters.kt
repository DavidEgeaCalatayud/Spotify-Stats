package com.davidegea.spotifystats.database

import androidx.room.TypeConverter
import com.davidegea.spotifystats.model.PlaySource

class DatabaseConverters {
    @TypeConverter
    fun playSourceToString(value: PlaySource): String = value.name

    @TypeConverter
    fun stringToPlaySource(value: String): PlaySource = PlaySource.valueOf(value)
}
