package com.sandalia.stockanchor.billing

import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.str
import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.UnfetchedProduct
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryPurchasesAsync
import com.sandalia.stockanchor.BuildConfig
import com.sandalia.stockanchor.data.prefs.SessionStore
import com.sandalia.stockanchor.util.GoogleAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** One purchasable base plan of the Premium subscription. */
data class PlanOffer(
    val planId: String,
    val title: String,
    val price: String,
    val billingPeriod: String,
    val offerToken: String?,
    val details: ProductDetails?,
    /** e.g. "Free trial 1 month" when the offer has a free phase */
    val trial: String? = null,
    /** Length of the free phase, e.g. "1 month", for the terms shown before subscribing */
    val trialPeriod: String? = null,
)

sealed interface PurchaseEvent {
    data class Success(val token: String, val planId: String?) : PurchaseEvent
    data object Cancelled : PurchaseEvent
    data object Pending : PurchaseEvent
    data class Error(val message: String) : PurchaseEvent
}

/**
 * Google Play Billing for the monthly & yearly Premium subscription.
 *
 * Play Console setup: Monetize → Subscriptions → product id set in the server's Admin → System (`sa_premium` by default)
 * with two auto-renewing base plans: `monthly` (P1M) and `yearly` (P1Y), and on each of them a developer-determined
 * offer with the id `free-trial-1-month` (a free phase of P1M). Google Play only returns that offer to accounts that
 * are eligible for it; everyone else gets the normal price.
 *
 * The purchase token is NOT trusted on-device; it is sent to the Laravel API which verifies it with
 * the Play Developer API and unlocks Premium for the whole team.
 */
class BillingManager(context: Context, private val session: SessionStore, private val scope: CoroutineScope) :
    PurchasesUpdatedListener {

    private val client = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection() // Billing 8+: reconnects by itself when Play Services restarts
        .build()

    private val _offers = MutableStateFlow(fallbackOffers())
    val offers: StateFlow<List<PlanOffer>> = _offers

    private val _ready = MutableStateFlow(false)
    /** True when Play Billing is connected and real products were loaded. */
    val ready: StateFlow<Boolean> = _ready

    private val _problem = MutableStateFlow<String?>(null)
    /** Why prices couldn't be loaded (shown on the paywall), null while loading or when ready. */
    val problem: StateFlow<String?> = _problem

    private val _offerSummary = MutableStateFlow<String?>(null)
    /** What Google Play returned for this account, e.g. "monthly: base, free-trial-1-month[P1M free]". For diagnosing missing offers. */
    val offerSummary: StateFlow<String?> = _offerSummary

    private val _events = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<PurchaseEvent> = _events

    private var pendingPlanId: String? = null

    /** Connects to Google Play and loads the plans; safe to call again (the paywall does on every visit). */
    fun connect() {
        if (_ready.value) return
        _problem.value = null
        if (client.isReady) loadProducts() else startConnection()
    }

    /** The server names the subscription to sell (Admin → System); a change takes effect without an app update. */
    fun useProductId(id: String) {
        if (id.isBlank() || id == session.subscriptionProductId()) return
        session.setSubscriptionProductId(id)
        _ready.value = false
        _problem.value = null
        _offers.value = fallbackOffers()
        _offerSummary.value = null
        if (client.isReady) loadProducts() else startConnection()
    }

    private fun startConnection() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) loadProducts(retry = false)
                else _problem.value = setupProblem(result)
            }

            override fun onBillingServiceDisconnected() {
                _ready.value = false
            }
        })
    }

    /** [retry]: a "disconnected" answer reconnects once, so the real setup error (e.g. billing unavailable) is shown. */
    private fun loadProducts(retry: Boolean = true) {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(session.subscriptionProductId())
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        ).build()
        client.queryProductDetailsAsync(params) { result, query ->
            if (result.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED && retry) {
                startConnection()
                return@queryProductDetailsAsync
            }
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _problem.value = setupProblem(result)
                return@queryProductDetailsAsync
            }
            // The ID may have changed while this was loading; that newer query has its own answer on the way.
            val details = query.productDetailsList.firstOrNull { it.productId == session.subscriptionProductId() }
            if (details == null) {
                _problem.value = productProblem(query.unfetchedProductList.firstOrNull()?.statusCode)
                return@queryProductDetailsAsync
            }
            showOffers(details)
        }
    }

    private fun describeOffers(details: ProductDetails): String = details.subscriptionOfferDetails.orEmpty()
        .groupBy { it.basePlanId }
        .entries.joinToString(" | ") { (planId, list) ->
            planId + ": " + list.joinToString(", ") { offer ->
                val free = offer.pricingPhases.pricingPhaseList.firstOrNull { it.priceAmountMicros == 0L }
                (offer.offerId ?: "base") + (free?.let { "[${it.billingPeriod} free]" }.orEmpty())
            }
        }

    private fun showOffers(details: ProductDetails) {
        val summary = describeOffers(details)
        _offerSummary.value = summary
        Log.i("StockAnchorBilling", "Offers from Google Play: $summary")
        val offers = details.subscriptionOfferDetails.orEmpty()
            .groupBy { it.basePlanId }
            .mapNotNull { (planId, list) ->
                // The free trial offer when this account may have it, then any other trial / intro phase, else the plain base plan.
                val offer = list.firstOrNull { it.offerId == BuildConfig.TRIAL_OFFER_ID }
                    ?: list.maxByOrNull { it.pricingPhases.pricingPhaseList.size }
                    ?: return@mapNotNull null
                val phases = offer.pricingPhases.pricingPhaseList
                val recurring = phases.last()
                val free = phases.firstOrNull { it.priceAmountMicros == 0L }
                PlanOffer(
                    planId = planId,
                    title = if (planId == BuildConfig.PLAN_YEARLY) str(R.string.yearly) else str(R.string.monthly),
                    price = recurring.formattedPrice,
                    billingPeriod = recurring.billingPeriod,
                    offerToken = offer.offerToken,
                    details = details,
                    trial = free?.let { str(R.string.free_trial_x, periodLabel(it.billingPeriod)) },
                    trialPeriod = free?.let { periodLabel(it.billingPeriod) },
                )
            }
            .sortedBy { if (it.planId == BuildConfig.PLAN_MONTHLY) 0 else 1 }

        if (offers.isEmpty()) {
            _problem.value = str(R.string.the_subscription_has_no_active_plans)
            return
        }
        _offers.value = offers
        _problem.value = null
        _ready.value = true
    }

    private fun setupProblem(result: BillingResult): String = when (result.responseCode) {
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE, BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ->
            str(R.string.google_play_can_t_sell_subscriptions)
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE, BillingClient.BillingResponseCode.NETWORK_ERROR ->
            str(R.string.couldn_t_reach_google_play_check)
        else -> str(R.string.google_play_billing_error_x_x, result.responseCode, result.debugMessage.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty())
    }

    private fun productProblem(status: Int?): String = when (status) {
        UnfetchedProduct.StatusCode.PRODUCT_NOT_FOUND ->
            str(R.string.google_play_doesn_t_offer_this, session.subscriptionProductId())
        UnfetchedProduct.StatusCode.NO_ELIGIBLE_OFFER ->
            str(R.string.no_subscription_plan_is_available_for)
        else -> str(R.string.subscription_prices_couldn_t_be_loaded)
    }

    /** [accountEmail]: address a new cloud account will use; the purchase is tied to it so the server can check that later. */
    fun launch(activity: Activity, offer: PlanOffer, accountEmail: String? = null) {
        val details = offer.details
        val token = offer.offerToken
        if (details == null || token == null) {
            _events.tryEmit(PurchaseEvent.Error(_problem.value ?: str(R.string.subscription_prices_are_still_loading_please)))
            connect()
            return
        }
        pendingPlanId = offer.planId
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(token)
                        .build()
                )
            )
            .apply { accountEmail?.let { setObfuscatedAccountId(GoogleAccount.purchaseId(it)) } }
            .build()
        client.launchBillingFlow(activity, params)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.forEach { handle(it) }
            BillingClient.BillingResponseCode.USER_CANCELED -> _events.tryEmit(PurchaseEvent.Cancelled)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                _events.tryEmit(PurchaseEvent.Error(str(R.string.you_already_own_premium_use_restore)))
            else -> _events.tryEmit(PurchaseEvent.Error(result.debugMessage.ifBlank { str(R.string.purchase_failed_x, result.responseCode) }))
        }
    }

    private fun handle(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                session.setPendingPurchase(purchase.purchaseToken, pendingPlanId)
                _events.tryEmit(PurchaseEvent.Success(purchase.purchaseToken, pendingPlanId))
            }
            Purchase.PurchaseState.PENDING -> _events.tryEmit(PurchaseEvent.Pending)
            else -> Unit
        }
    }

    /** Acknowledge after the server confirmed the token (Play refunds unacknowledged purchases after 3 days). */
    suspend fun acknowledgeAll() {
        if (!client.isReady) return
        activeSubscriptions().filter { !it.isAcknowledged }.forEach {
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(it.purchaseToken).build())
        }
    }

    /** Active Play subscriptions owned by this Google account (Restore purchase). */
    suspend fun activeSubscriptions(): List<Purchase> {
        if (!client.isReady) return emptyList()
        val result = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        return result.purchasesList.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
    }

    /** Debug builds: simulate a purchase that the API accepts in SUBSCRIPTION_VERIFY_MODE=fake. */
    fun testPurchase(planId: String) {
        val token = "test_${planId}_${System.currentTimeMillis()}"
        session.setPendingPurchase(token, planId)
        _events.tryEmit(PurchaseEvent.Success(token, planId))
    }

    private fun fallbackOffers() = listOf(
        PlanOffer(BuildConfig.PLAN_MONTHLY, str(R.string.monthly), "—", "P1M", null, null),
        PlanOffer(BuildConfig.PLAN_YEARLY, str(R.string.yearly), "—", "P1Y", null, null),
    )

    companion object {
        fun periodLabel(iso: String): String = when (iso) {
            "P4W" -> str(R.string.s_4_weeks); "P1W" -> str(R.string.s_1_week); "P3D" -> str(R.string.s_3_days); "P7D" -> str(R.string.s_7_days); "P14D" -> str(R.string.s_14_days)
            "P1M" -> str(R.string.s_1_month); "P3M" -> str(R.string.s_3_months); "P6M" -> str(R.string.s_6_months); "P1Y" -> str(R.string.s_1_year)
            else -> iso.removePrefix("P").lowercase()
        }
    }
}
