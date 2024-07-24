package com.s21845.groceryapp // Package declaration

// Import necessary package for Room type converters
import androidx.room.TypeConverter
import java.util.Date // Import for handling Date objects

// Converters class to handle data type conversions for Room
class Converters {

    // Type converter to convert a Long timestamp to a Date object
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        // If the input value is null, return null
        // Otherwise, create and return a Date object from the Long value
        return value?.let { Date(it) }
    }

    // Type converter to convert a Date object to a Long timestamp
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        // If the input Date is null, return null
        // Otherwise, return the time in milliseconds as Long
        return date?.time
    }
}
