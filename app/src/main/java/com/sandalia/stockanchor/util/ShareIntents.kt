package com.sandalia.stockanchor.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

/**
 * Starts share / view intents directly instead of through Intent.createChooser: the system chooser
 * (ChooserActivity) crashes on some OEM builds, e.g. ColorOS 11 on OPPO, and takes the app down with it.
 * Returns false when no app can handle the intent.
 */
object ShareIntents {
    fun start(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    fun shareText(context: Context, text: String, subject: String): Boolean = start(context, Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text); putExtra(Intent.EXTRA_SUBJECT, subject)
    })
}
