package com.jules.loader.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private val DATE_FORMATS = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    private val formatters = ThreadLocal<MutableMap<String, SimpleDateFormat>>()

    private fun getFormatter(pattern: String): SimpleDateFormat {
        var map = formatters.get()
        if (map == null) {
            map = HashMap()
            formatters.set(map)
        }
        var sdf = map[pattern]
        if (sdf == null) {
            sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            map[pattern] = sdf
        }
        return sdf
    }

    fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        for (format in DATE_FORMATS) {
            try {
                return getFormatter(format).parse(dateString)
            } catch (e: java.text.ParseException) {
                // Try next format
            }
        }
        return null
    }

    fun formatDate(dateString: String?): String? {
        val date = parseDate(dateString) ?: return null
        val dayFormatter = SimpleDateFormat("d", Locale.getDefault())
        val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())

        val day = dayFormatter.format(date).toInt()
        val month = monthFormatter.format(date)
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
