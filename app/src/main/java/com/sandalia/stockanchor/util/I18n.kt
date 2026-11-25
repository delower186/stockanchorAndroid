package com.sandalia.stockanchor.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import android.telephony.TelephonyManager
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import java.util.Locale

/**
 * In-app language. By default ("Automatic") it is picked from the user's country (SIM, then mobile network, then the
 * phone's region); countries without a translation get English. A language chosen in Settings overrides that.
 * The result is applied to every Activity (attachBaseContext) and to [str], so screens, toasts, notifications and
 * invoices all follow it. Arabic switches the layout to right-to-left. Number and date formats keep the phone's locale.
 */
@SuppressLint("StaticFieldLeak")
object I18n {
    /** Used when the country has no translation. Also the text in res/values (the resource fallback). */
    const val FALLBACK = "en"

    /** Language tag → name shown in its own language. "" = Automatic (by country). */
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

    /** ISO country code → language used when the language is set to Automatic. Any other country gets [FALLBACK]. */
    private val COUNTRY_LANGUAGE: Map<String, String> = buildMap {
        put("BD", "bn-BD")
        put("IN", "hi")
        listOf(
            "ES", "MX", "AR", "CO", "CL", "PE", "VE", "EC", "GT", "CU", "BO", "DO", "HN", "PY", "SV", "NI", "CR", "PA",
            "UY", "PR", "GQ",
        ).forEach { put(it, "es") }
        listOf(
            "FR", "MC", "SN", "CI", "ML", "BF", "NE", "TG", "BJ", "GA", "CG", "CD", "CM", "MG", "GN", "TD", "CF", "BI",
            "DJ", "KM", "HT",
        ).forEach { put(it, "fr") }
        listOf(
            "SA", "AE", "EG", "QA", "KW", "BH", "OM", "JO", "LB", "SY", "IQ", "YE", "PS", "LY", "SD", "TN", "DZ", "MA",
            "MR",
        ).forEach { put(it, "ar") }
        listOf("DE", "AT", "CH", "LI").forEach { put(it, "de") }
    }

    private const val PREFS = "i18n"
    private const val KEY = "language"

    // Only ever the Application context (wrapped with the chosen locale), so nothing is leaked.
    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context

    fun init(app: Context) {
        context = wrap(app)
    }

    /** The language saved in Settings, or "" for Automatic. */
    fun current(base: Context): String = base.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "").orEmpty()

    /** Language Automatic would use: the country's language when the app has it, otherwise English. */
    fun automatic(base: Context): String = COUNTRY_LANGUAGE[country(base)] ?: FALLBACK

    /** The language actually shown. */
    fun resolved(base: Context): String = current(base).ifBlank { automatic(base) }

    /** The language shown right now, for the server's messages and emails. */
    fun language(): String = resolved(context)

    fun nameOf(tag: String): String = LANGUAGES.firstOrNull { it.first == tag }?.second ?: tag

    /** Home country: SIM first (stays the same when travelling), then the mobile network, then the phone's region. */
    private fun country(base: Context): String {
        val telephony = base.applicationContext.getSystemService(TelephonyManager::class.java)
        return listOfNotNull(
            runCatching { telephony?.simCountryIso }.getOrNull(),
            runCatching { telephony?.networkCountryIso }.getOrNull(),
            base.resources.configuration.locales[0]?.country,
        ).firstOrNull { it.length == 2 }.orEmpty().uppercase(Locale.ROOT)
    }

    /** [base] with the resolved language applied. */
    fun wrap(base: Context): Context {
        val locale = Locale.forLanguageTag(resolved(base))
        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    /** Saves the choice ("" = Automatic) and reloads the screen in the new language. */
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
