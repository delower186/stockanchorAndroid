package com.sandalia.stockanchor.work

import com.sandalia.stockanchor.util.plural
import com.sandalia.stockanchor.util.str
import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sandalia.stockanchor.MainActivity
import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.StockAnchorApp
import com.sandalia.stockanchor.util.Fmt
import java.util.concurrent.TimeUnit

/** Premium: syncs, then notifies about products at or below their low-stock threshold. Runs twice a day. */
class LowStockWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as StockAnchorApp).container
        val session = container.session.current
        if (!session.premiumActive || !session.lowStockAlerts) return Result.success()

        runCatching { container.sync.pull() } // best effort; fall back to cached data
        LowStockNotifier.check(applicationContext)
        return Result.success()
    }

    companion object {
        private const val NAME = "low_stock_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<LowStockWorker>(12, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(NAME)
    }
}

object LowStockNotifier {
    private const val CHANNEL = "low_stock"
    private const val NOTIFICATION_ID = 4201

    suspend fun check(context: Context) {
        val container = (context.applicationContext as StockAnchorApp).container
        if (!container.session.current.premiumActive || !container.session.current.lowStockAlerts) return
        val low = container.db.catalog().lowStockNow()
        if (low.isEmpty()) return
        notify(context, low.map { str(R.string.x_x_x_left_min_x, it.name, Fmt.qty(it.quantity), it.unit, Fmt.qty(it.lowStockThreshold)) })
    }

    @SuppressLint("MissingPermission")
    private fun notify(context: Context, lines: List<String>) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, str(R.string.low_stock_channel), NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_ROUTE, "low_stock")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val style = NotificationCompat.InboxStyle().also { s -> lines.take(6).forEach { s.addLine(it) } }
        if (lines.size > 6) style.setSummaryText(str(R.string.x_more, lines.size - 6))

        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(plural(R.plurals.n_products_running_low, lines.size, lines.size))
            .setContentText(lines.first())
            .setStyle(style)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
