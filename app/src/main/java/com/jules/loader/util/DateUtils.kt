package com.jules.loader.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(dateString)
            } catch (e: Exception) {
                // Try next format
            }
        }
        return null
    }

    fun formatDate(dateString: String?): String? {
        val date = parseDate(dateString) ?: return null
        val day = SimpleDateFormat("d", Locale.getDefault()).format(date).toInt()
        val month = SimpleDateFormat("MMM", Locale.getDefault()).format(date)
        val suffix = getDayOfMonthSuffix(day)
        return "$day$suffix $month"
    }

    private fun getDayOfMonthSuffix(n: Int): String {
        if (n in 11..13) return "th"
        return when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}
