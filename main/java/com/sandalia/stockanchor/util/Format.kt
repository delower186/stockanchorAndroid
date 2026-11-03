package com.sandalia.stockanchor.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

object Fmt {
    private val money = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2; maximumFractionDigits = 2
    }
    private val compact = DecimalFormat("#,##0.##")

    fun money(symbol: String, amount: Double): String {
        val sign = if (amount < 0) "-" else ""
        return "$sign$symbol${money.format(kotlin.math.abs(amount))}"
    }

    /** 1.2K / 3.4M for dashboard tiles. */
    fun moneyShort(symbol: String, amount: Double): String {
        val abs = kotlin.math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            abs >= 1_000_000 -> "$sign$symbol${compact.format(abs / 1_000_000)}M"
            abs >= 100_000 -> "$sign$symbol${compact.format(abs / 1_000)}K"
            else -> money(symbol, amount)
        }
    }

    fun qty(q: Double): String = compact.format(q)

    fun date(millis: Long): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    fun dateTime(millis: Long): String = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))
    fun time(millis: Long): String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))

    fun movementLabel(type: String): String = when (type) {
        "purchase" -> "Purchase"
        "sale" -> "Sale"
        "sale_cancel" -> "Sale cancelled"
        "transfer_in" -> "Transfer in"
        "transfer_out" -> "Transfer out"
        "adjustment" -> "Adjustment"
        "import" -> "CSV import"
        else -> type.replaceFirstChar { it.uppercase() }
    }
}

object Dates {
    /** ISO-8601 from the API (e.g. 2026-10-26T08:00:00+00:00) → epoch millis. */
    fun parse(iso: String?): Long =
        iso?.let { runCatching { OffsetDateTime.parse(it).toInstant().toEpochMilli() }.getOrNull() } ?: System.currentTimeMillis()

    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun startOfDay(d: LocalDate = LocalDate.now()) = d.atStartOfDay(zone).toInstant().toEpochMilli()
    fun endOfDay(d: LocalDate = LocalDate.now()) = d.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    /** [from, to] millis for "day", "month" or "year" containing [anchor]. */
    fun range(period: Period, anchor: LocalDate = LocalDate.now()): Pair<Long, Long> = when (period) {
        Period.DAY -> startOfDay(anchor) to endOfDay(anchor)
        Period.MONTH -> startOfDay(anchor.withDayOfMonth(1)) to endOfDay(anchor.withDayOfMonth(anchor.lengthOfMonth()))
        Period.YEAR -> startOfDay(anchor.withDayOfYear(1)) to endOfDay(anchor.withDayOfYear(anchor.lengthOfYear()))
    }
}

enum class Period(val label: String, val sqlFormat: String) {
    DAY("Today", "%H"),
    MONTH("This month", "%Y-%m-%d"),
    YEAR("This year", "%Y-%m"),
}
