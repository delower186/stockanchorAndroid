package com.sandalia.stockanchor

import android.app.Application
import com.sandalia.stockanchor.billing.BillingManager
import com.sandalia.stockanchor.data.local.AppDatabase
import com.sandalia.stockanchor.data.prefs.SessionStore
import com.sandalia.stockanchor.data.remote.ApiClient
import com.sandalia.stockanchor.data.repo.AccountManager
import com.sandalia.stockanchor.data.repo.StockRepository
import com.sandalia.stockanchor.data.repo.SyncManager
import com.sandalia.stockanchor.util.I18n
import com.sandalia.stockanchor.work.LowStockWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

/** Manual dependency container — small enough that a DI framework would only add build time. */
class AppContainer(app: Application) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val session = SessionStore(app)
    val db = AppDatabase.build(app)
    val api = ApiClient(session)
    val sync = SyncManager(db, api, session)
    val repo = StockRepository(db, api, session, sync)
    val account = AccountManager(api, session, sync)
    val billing = BillingManager(app, session, appScope)
}

class StockAnchorApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        I18n.init(this)
        container = AppContainer(this)
        container.billing.connect()
        LowStockWorker.schedule(this)
    }
}
