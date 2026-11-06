package com.sandalia.stockanchor.util

import android.util.Patterns
import com.sandalia.stockanchor.R

/** Field sizes, the same as the Laravel API's validation and database columns. */
object FieldLimit {
    /** Long enough for a business name, short enough to stay on one line on every screen and invoice. */
    const val COMPANY = 30
    const val CURRENCY_SYMBOL = 4
    const val NAME = 255
    const val PHONE = 25
    const val EMAIL = 255
    const val PASSWORD = 255
    const val PASSWORD_MIN = 8
    const val ADDRESS = 255
    const val SHORT_NOTE = 255
    const val NOTE = 2000
    const val DESCRIPTION = 2000
    const val PRODUCT_DESCRIPTION = 5000
    const val SKU = 64
    const val BARCODE = 128
    const val UNIT = 20
    const val EXPENSE_CATEGORY = 60
    const val PROMO_CODE = 40
    const val MONEY_MAX = 999_999_999_999.99
    const val QUANTITY_MAX = 99_999_999_999.999
}

/**
 * Rules for form fields. Basic plan data never reaches the server, so the app can't leave checking to the API.
 * Each returns the message to show, or null when the value is fine.
 */
object Validate {
    private val PHONE = Regex("^\\+?[\\p{Nd}\\s().\\-]{6,25}$")

    fun required(value: String): String? = if (value.isBlank()) str(R.string.this_field_is_required) else null

    /** [max] only matters for values saved before the limit existed; typing can't go past it. */
    fun name(value: String, max: Int = FieldLimit.NAME): String? = required(value) ?: maxLength(value, max)

    fun maxLength(value: String, max: Int): String? = if (value.trim().length > max) str(R.string.use_at_most_x_characters, max) else null

    fun email(value: String, required: Boolean = false): String? = when {
        value.isBlank() -> if (required) str(R.string.this_field_is_required) else null
        !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> str(R.string.enter_a_valid_email)
        else -> null
    }

    fun phone(value: String): String? =
        if (value.isBlank() || PHONE.matches(value.trim())) null else str(R.string.enter_a_valid_phone_number)

    fun password(value: String, required: Boolean = true): String? = when {
        value.isEmpty() -> if (required) str(R.string.this_field_is_required) else null
        value.length < FieldLimit.PASSWORD_MIN -> str(R.string.at_least_8_characters)
        else -> null
    }

    /** Price or other money value; zero is allowed unless [aboveZero]. */
    fun money(value: String, required: Boolean = false, aboveZero: Boolean = false): String? =
        number(value, required, aboveZero, FieldLimit.MONEY_MAX)

    fun quantity(value: String, required: Boolean = false, aboveZero: Boolean = false): String? =
        number(value, required, aboveZero, FieldLimit.QUANTITY_MAX)

    private fun number(value: String, required: Boolean, aboveZero: Boolean, max: Double): String? {
        if (value.isBlank()) return if (required) str(R.string.this_field_is_required) else null
        val n = value.toDoubleOrNull() ?: return str(R.string.enter_a_valid_number)
        return when {
            n < 0 -> str(R.string.enter_a_valid_number)
            aboveZero && n == 0.0 -> str(R.string.must_be_more_than_zero)
            n > max -> str(R.string.this_number_is_too_large)
            else -> null
        }
    }
}
