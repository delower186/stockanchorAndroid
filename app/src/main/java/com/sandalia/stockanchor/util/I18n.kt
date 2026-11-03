package com.sandalia.stockanchor.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import java.util.Locale

/**
 * In-app language. The choice is stored on the device and applied to every Activity (attachBaseContext) and to
 * [str], so screens, toasts, notifications and invoices all follow it. Arabic switches the layout to right-to-left.
 * Number and date formats keep the phone's own locale.
 */
@SuppressLint("StaticFieldLeak")
object I18n {
    /** Language tag → name shown in its own language. "" = follow the phone. */
    val LANGUAGES = listOf(
        "" to null,
        "en" to "English",
        "bn-BD" to "বাংলা",
        "hi" to "हिन्दी",
        "es" to "Español",
        "fr" to "Français",
        "ar" to "العربية",
        "de" to "Deutsch",
    )

    private const val PREFS = "i18n"
    private const val KEY = "language"

    // Only ever the Application context (wrapped with the chosen locale), so nothing is leaked.
    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context

    fun init(app: Context) {
        context = wrap(app)
    }

    fun current(base: Context): String = base.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "").orEmpty()

    /** [base] with the chosen language applied, or unchanged when following the phone. */
    fun wrap(base: Context): Context {
        val tag = current(base)
        if (tag.isBlank()) return base
        val locale = Locale.forLanguageTag(tag)
        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    /** Saves the choice and reloads the screen in the new language. */
    fun setLanguage(activity: Activity, tag: String) {
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, tag).apply()
        init(activity.applicationContext)
        activity.recreate()
    }

    fun string(@StringRes id: Int, vararg args: Any?): String = context.getString(id, *args)

    fun plural(@PluralsRes id: Int, count: Int, vararg args: Any?): String = context.resources.getQuantityString(id, count, *args)
}

/** Translated text in the app's language, usable in and outside Compose. */
fun str(@StringRes id: Int, vararg args: Any?): String = I18n.string(id, *args)

/** Translated plural ("1 product" / "5 products"). */
fun plural(@PluralsRes id: Int, count: Int, vararg args: Any?): String = I18n.plural(id, count, *args)
