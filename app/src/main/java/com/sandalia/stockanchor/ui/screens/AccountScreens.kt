package com.sandalia.stockanchor.ui.screens

import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.ui.components.FormField
import com.sandalia.stockanchor.util.FieldLimit
import com.sandalia.stockanchor.util.Validate
import com.sandalia.stockanchor.util.str
import com.sandalia.stockanchor.ui.components.LocalSnackbar
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import android.app.Activity
import androidx.activity.compose.LocalActivity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.sandalia.stockanchor.AppContainer
import com.sandalia.stockanchor.BuildConfig
import com.sandalia.stockanchor.billing.PlanOffer
import com.sandalia.stockanchor.billing.PurchaseEvent
import com.sandalia.stockanchor.data.remote.ApiException
import com.sandalia.stockanchor.data.repo.ActivationResult
import com.sandalia.stockanchor.domain.Limits
import com.sandalia.stockanchor.data.prefs.SessionStore
import com.sandalia.stockanchor.domain.Perm
import com.sandalia.stockanchor.domain.PremiumFeature
import com.sandalia.stockanchor.ui.components.AppCard
import com.sandalia.stockanchor.ui.components.ConfirmDialog
import com.sandalia.stockanchor.ui.components.DropdownField
import com.sandalia.stockanchor.ui.components.KeyValue
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.MenuRow
import com.sandalia.stockanchor.ui.components.PremiumChip
import com.sandalia.stockanchor.ui.components.Runner
import com.sandalia.stockanchor.ui.components.ScreenScaffold
import com.sandalia.stockanchor.ui.components.SectionTitle
import com.sandalia.stockanchor.ui.components.rememberRunner
import com.sandalia.stockanchor.ui.navigation.Routes
import com.sandalia.stockanchor.ui.navigation.goHome
import com.sandalia.stockanchor.ui.theme.Amber
import com.sandalia.stockanchor.ui.theme.Navy
import com.sandalia.stockanchor.ui.theme.NavyLight
import com.sandalia.stockanchor.ui.theme.TealLight
import com.sandalia.stockanchor.util.Dates
import com.sandalia.stockanchor.util.Fmt
import com.sandalia.stockanchor.util.I18n
import com.sandalia.stockanchor.util.rememberGoogleAccountPicker
import kotlinx.coroutines.delay

/* ============================================================ Upgrade → cloud activation */

/**
 * After Premium is confirmed by the server: ask whether to upload this device's Basic data,
 * handle the "cloud already has data" case, then switch the app to cloud mode.
 */
class ActivationFlow(private val container: AppContainer, private val runner: Runner, private val onFinished: () -> Unit) {
    var askUpload by mutableStateOf(false)
    var askMerge by mutableStateOf(false)

    fun begin() = runner.run {
        container.billing.acknowledgeAll()
        when {
            container.session.current.isCloud -> onFinished()
            // Uploading this device's Basic data needs data.import; team members without it just join the cloud data.
            container.sync.hasLocalData() && container.session.current.can(Perm.DATA_IMPORT) -> { askUpload = true }
            else -> doActivate(upload = false, merge = false)
        }
    }

    fun activate(upload: Boolean, merge: Boolean) = runner.run { doActivate(upload, merge) }

    // Called directly from begin(): a nested runner.run would be skipped because the runner is already busy.
    private suspend fun doActivate(upload: Boolean, merge: Boolean) {
        when (container.account.activateCloud(upload, merge)) {
            ActivationResult.Activated -> { runner.toast(str(R.string.stock_anchor_premium_is_active)); onFinished() }
            ActivationResult.NeedsMergeDecision -> { askMerge = true }
            ActivationResult.NotPremium -> runner.toast(str(R.string.subscription_not_active_yet_try_restore))
        }
    }
}

@Composable
fun rememberActivation(nav: NavHostController): ActivationFlow {
    val container = LocalContainer.current
    val runner = rememberRunner()
    val flow = remember { ActivationFlow(container, runner) { nav.goHome() } }
    if (flow.askUpload) AlertDialog(
        onDismissRequest = {},
        icon = { Icon(Icons.Filled.Cloud, null) },
        title = { Text(str(R.string.move_your_data_to_the_cloud)) },
        text = { Text(str(R.string.upload_the_products_stock_and_sales)) },
        confirmButton = { TextButton(onClick = { flow.askUpload = false; flow.activate(upload = true, merge = false) }) { Text(str(R.string.upload_my_data)) } },
        dismissButton = { TextButton(onClick = { flow.askUpload = false; flow.activate(upload = false, merge = false) }) { Text(str(R.string.start_fresh)) } },
    )
    if (flow.askMerge) AlertDialog(
        onDismissRequest = {},
        icon = { Icon(Icons.Filled.Warning, null) },
        title = { Text(str(R.string.your_cloud_already_has_data)) },
        text = { Text(str(R.string.add_this_device_s_products_and)) },
        confirmButton = { TextButton(onClick = { flow.askMerge = false; flow.activate(upload = true, merge = true) }) { Text(str(R.string.merge_both)) } },
        dismissButton = { TextButton(onClick = { flow.askMerge = false; flow.activate(upload = false, merge = false) }) { Text(str(R.string.use_cloud_only)) } },
    )
    return flow
}

/* ============================================================ Paywall */

@Composable
fun PaywallScreen(nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val offers by container.billing.offers.collectAsStateWithLifecycle()
    val billingReady by container.billing.ready.collectAsStateWithLifecycle()
    val billingProblem by container.billing.problem.collectAsStateWithLifecycle()
    val offerSummary by container.billing.offerSummary.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val runner = rememberRunner()
    val activation = rememberActivation(nav)
    var selected by rememberSaveable { mutableStateOf(BuildConfig.PLAN_YEARLY) }
    var promoOpen by rememberSaveable { mutableStateOf(false) }
    var promoError by remember { mutableStateOf<String?>(null) }

    // A new customer's Google account: filled by Android's account picker, never typed. Debug builds with a simulated
    // purchase may run on an emulator without any Google account, so they don't need one.
    val needsAccount = !session.loggedIn && !(BuildConfig.ALLOW_TEST_PURCHASES && !billingReady)
    var accountEmail by rememberSaveable {
        mutableStateOf(if (container.session.hasPendingProof()) container.session.billingEmail().orEmpty() else "")
    }
    var askedForAccount by rememberSaveable { mutableStateOf(false) }
    var afterAccountChosen by remember { mutableStateOf<(() -> Unit)?>(null) }
    val pickAccount = rememberGoogleAccountPicker(str(R.string.choose_the_google_account_for_google_play)) { email ->
        accountEmail = email
        container.session.setBillingEmail(email)
        afterAccountChosen?.invoke()
        afterAccountChosen = null
    }
    val withAccount: (() -> Unit) -> Unit = { action ->
        if (!needsAccount || accountEmail.isNotBlank()) action() else { afterAccountChosen = action; pickAccount() }
    }
    LaunchedEffect(needsAccount) {
        if (needsAccount && accountEmail.isBlank() && !askedForAccount) { askedForAccount = true; pickAccount() }
    }

    // Retry every visit: a connection that failed at app start (Play Store updating, offline) would otherwise never recover.
    LaunchedEffect(Unit) { container.billing.connect(); container.syncSubscriptionProduct() }

    // Purchase completed in Google Play → verify with our server (or create the cloud account first).
    LaunchedEffect(Unit) {
        container.billing.events.collect { e ->
            when (e) {
                is PurchaseEvent.Success ->
                    if (container.session.current.loggedIn) runner.run {
                        if (container.account.verifyPendingPurchase()) activation.begin() else runner.toast(str(R.string.we_couldn_t_confirm_the_subscription))
                    } else nav.navigate(Routes.account("upgrade"))
                PurchaseEvent.Pending -> runner.toast(str(R.string.payment_pending_premium_unlocks_as_soon))
                PurchaseEvent.Cancelled -> Unit
                is PurchaseEvent.Error -> runner.toast(e.message)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
        Box(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Navy, NavyLight, Color(0xFF0D5A6B))))
                .statusBarsPadding().padding(20.dp)
        ) {
            Column {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, str(R.string.back), tint = Color.White) }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Anchor, null, tint = TealLight, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(str(R.string.stock_anchor_premium), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(str(R.string.cloud_inventory_for_growing_businesses_and), color = Color(0xFFB8C7D9))
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (session.premiumActive) {
                AppCard(Modifier.fillMaxWidth()) {
                    Text(str(R.string.you_re_on_premium), style = MaterialTheme.typography.titleMedium)
                    Text(str(R.string.plan_x, session.plan ?: "—"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (needsAccount) {
                OutlinedTextField(
                    accountEmail, {}, readOnly = true, enabled = false, singleLine = true,
                    label = { Text(str(R.string.google_play_account)) },
                    supportingText = { Text(str(R.string.google_play_account_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                offers.forEach { o -> PlanCard(o, selected == o.planId, Modifier.weight(1f)) { selected = o.planId } }
            }

            Button(
                onClick = {
                    val offer = offers.firstOrNull { it.planId == selected } ?: return@Button
                    when {
                        billingReady -> withAccount { container.billing.launch(context as Activity, offer, accountEmail.takeIf { needsAccount }) }
                        BuildConfig.ALLOW_TEST_PURCHASES -> container.billing.testPurchase(offer.planId)
                        else -> { container.billing.connect(); runner.toast(billingProblem ?: str(R.string.loading_prices_from_google_play)) }
                    }
                },
                enabled = !runner.busy,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Navy),
            ) {
                Icon(Icons.Filled.WorkspacePremium, null); Spacer(Modifier.width(8.dp))
                val selectedOffer = offers.firstOrNull { it.planId == selected }
                Text(
                    when {
                        selectedOffer?.trial != null -> str(R.string.start_free_trial)
                        selected == BuildConfig.PLAN_YEARLY -> str(R.string.subscribe_yearly)
                        else -> str(R.string.subscribe_monthly)
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
            offers.firstOrNull { it.planId == selected }?.let { o ->
                o.trialPeriod?.let { period ->
                    Text(
                        str(R.string.free_for_x_then_y, period, o.price, if (o.planId == BuildConfig.PLAN_YEARLY) str(R.string.per_year) else str(R.string.per_month)),
                        Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!billingReady && !BuildConfig.ALLOW_TEST_PURCHASES) {
                AppCard(Modifier.fillMaxWidth()) {
                    if (billingProblem == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp); Spacer(Modifier.width(10.dp))
                            Text(str(R.string.loading_prices_from_google_play), style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Text(billingProblem.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { container.billing.connect() }) { Text(str(R.string.try_again)) }
                    }
                }
            }
            if (BuildConfig.DEBUG) offerSummary?.let {
                Text("Debug: offers from Google Play for this account: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!billingReady && BuildConfig.ALLOW_TEST_PURCHASES) {
                Text("Debug build: Play Billing not connected — a test purchase will be used (API must run SUBSCRIPTION_VERIFY_MODE=fake).",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = {
                    runner.run {
                        val p = container.billing.activeSubscriptions().firstOrNull()
                        if (p == null) { runner.toast(str(R.string.no_active_subscription_found_on_this)); return@run }
                        container.session.setPendingPurchase(p.purchaseToken, null)
                        if (container.session.current.loggedIn) {
                            if (container.account.verifyPendingPurchase()) activation.begin()
                        } else nav.navigate(Routes.account("upgrade"))
                    }
                }) { Text(str(R.string.restore_purchase)) }
                if (!session.loggedIn) TextButton(onClick = { nav.navigate(Routes.account("login")) }) { Text(str(R.string.team_member_sign_in)) }
            }
            TextButton(onClick = { promoOpen = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(str(R.string.have_a_promo_code)) }

            SectionTitle(str(R.string.everything_in_basic_plus))
            AppCard(Modifier.fillMaxWidth()) {
                PremiumFeature.entries.forEach { f ->
                    Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(f.title, fontWeight = FontWeight.SemiBold)
                            Text(f.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Text(
                str(R.string.subscriptions_renew_automatically_until_cancelle),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
    // The paywall has no ScreenScaffold, so it hosts the snackbar itself (purchase errors, billing problems).
    SnackbarHost(LocalSnackbar.current, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }

    if (promoOpen) PromoCodeDialog(
        busy = runner.busy,
        error = promoError,
        onDismiss = { promoOpen = false; promoError = null },
        onSubmit = { code ->
            withAccount {
                runner.run {
                    try {
                        promoError = null
                        if (container.session.current.loggedIn) {
                            if (container.account.redeemPromo(code)) { promoOpen = false; activation.begin() }
                        } else {
                            container.account.checkPromo(code)
                            promoOpen = false
                            nav.navigate(Routes.account("upgrade"))
                        }
                    } catch (e: ApiException) {
                        promoError = e.message
                    }
                }
            }
        },
    )
}

@Composable
private fun PromoCodeDialog(busy: Boolean, error: String?, onSubmit: (String) -> Unit, onDismiss: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Redeem, null) },
        title = { Text(str(R.string.have_a_promo_code)) },
        text = {
            OutlinedTextField(
                code, { typed -> code = typed.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' || it == '-' || it == '_' }.take(FieldLimit.PROMO_CODE) }, label = { Text(str(R.string.promo_code)) }, singleLine = true, isError = error != null,
                supportingText = { error?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Ascii),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { Button(onClick = { onSubmit(code) }, enabled = !busy && code.isNotBlank()) { Text(str(R.string.redeem)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(str(R.string.cancel)) } },
    )
}

/** Six-digit code from the verification email. A wrong code keeps the dialog open and says so. */
@Composable
private fun EmailCodeDialog(email: String, busy: Boolean, error: String?, resendInSeconds: Int, onSubmit: (String) -> Unit, onResend: () -> Unit, onDismiss: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.MarkEmailRead, null) },
        title = { Text(str(R.string.confirm_your_email)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(str(R.string.we_sent_a_6_digit_code_to_x, email), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    code, { code = it.filter(Char::isDigit).take(6) },
                    label = { Text(str(R.string.verification_code)) }, singleLine = true, isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = onResend, enabled = !busy && resendInSeconds == 0) {
                    Text(if (resendInSeconds > 0) str(R.string.resend_code_in_x_seconds, resendInSeconds) else str(R.string.resend_code))
                }
            }
        },
        confirmButton = { Button(onClick = { onSubmit(code) }, enabled = !busy && code.length == 6) { Text(str(R.string.verify)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(str(R.string.cancel)) } },
    )
}

/** Local prices vary a lot in length ("\$4.99", "BDT 7,100.00"): shrinks to stay on one line instead of breaking mid-number. */
@Composable
private fun PlanPrice(price: String) {
    val style = MaterialTheme.typography.headlineSmall
    var fontSize by remember(price) { mutableStateOf(style.fontSize) }
    var fits by remember(price) { mutableStateOf(false) }
    Text(
        price,
        style = style, fontSize = fontSize, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false,
        onTextLayout = { layout ->
            if (layout.hasVisualOverflow && fontSize.value > 12f) fontSize *= 0.92f else fits = true
        },
        modifier = Modifier.drawWithContent { if (fits) drawContent() },
    )
}

@Composable
private fun PlanCard(offer: PlanOffer, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val yearly = offer.planId == BuildConfig.PLAN_YEARLY
    Card(
        onClick = onClick, modifier = modifier,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            if (yearly) Surface(shape = RoundedCornerShape(50), color = Amber) {
                Text(str(R.string.best_value), Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Navy)
            } else Spacer(Modifier.height(18.dp))
            Spacer(Modifier.height(8.dp))
            Text(offer.title, style = MaterialTheme.typography.titleMedium)
            PlanPrice(offer.price)
            Text(if (yearly) str(R.string.per_year) else str(R.string.per_month), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            offer.trial?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary) }
        }
    }
}

/* ============================================================ Cloud account (login / register) */

@Composable
fun AccountScreen(mode: String, nav: NavHostController) {
    val container = LocalContainer.current
    val runner = rememberRunner()
    val activation = rememberActivation(nav)
    // Create Account exists only after a payment or a valid promo code; otherwise this screen is sign-in only.
    val paid = remember(mode) { mode == "upgrade" && container.session.hasPendingProof() }
    var tab by rememberSaveable { mutableStateOf(if (paid) 0 else 1) }
    var name by rememberSaveable { mutableStateOf("") }
    var company by rememberSaveable { mutableStateOf(container.session.current.companyName.takeIf { it != "My Business" }.orEmpty()) }
    var currency by rememberSaveable { mutableStateOf("BDT") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var notPremium by remember { mutableStateOf(false) }
    var purchaseRefused by remember { mutableStateOf(false) }
    var challenge by rememberSaveable { mutableStateOf<String?>(null) }

    // A new account always uses the Google account from the paywall. It can arrive here without one (Restore purchase).
    var accountEmail by rememberSaveable { mutableStateOf(container.session.billingEmail().orEmpty()) }
    var askedForAccount by rememberSaveable { mutableStateOf(false) }
    val pickAccount = rememberGoogleAccountPicker(str(R.string.choose_the_google_account_for_google_play)) {
        accountEmail = it
        container.session.setBillingEmail(it)
    }
    LaunchedEffect(paid) {
        if (paid && accountEmail.isBlank() && !BuildConfig.ALLOW_TEST_PURCHASES && !askedForAccount) { askedForAccount = true; pickAccount() }
    }
    val newAccountEmail = accountEmail.ifBlank { if (BuildConfig.ALLOW_TEST_PURCHASES) email else "" }

    var codeSentTo by rememberSaveable { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }
    var resendIn by rememberSaveable { mutableStateOf(0) }
    LaunchedEffect(resendIn) {
        if (resendIn > 0) { delay(1000); resendIn -= 1 }
    }

    // Plain suspend funs: they run inside the button's runner.run, and a nested runner.run is dropped while busy.
    suspend fun afterAuth() {
        if (container.session.current.isPremium) activation.begin()
        else { container.account.discardUnpaidSession(); notPremium = true }
    }

    suspend fun refusedForNoPlan(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: ApiException) {
            if (e.code == "premium_required") { challenge = null; notPremium = true } else throw e
        }
    }

    // Google Play no longer counts this purchase, so retrying with it can never work: forget it and go back to the plans.
    fun purchaseNoLongerActive() {
        container.session.setPendingPurchase(null, null)
        codeSentTo = null
        purchaseRefused = true
        notPremium = true
    }

    suspend fun sendCode() {
        val sent = try {
            container.account.sendRegistrationCode(newAccountEmail)
        } catch (e: ApiException) {
            if (e.code == "premium_required") { purchaseNoLongerActive(); return } else throw e
        }
        codeError = null
        codeSentTo = newAccountEmail
        resendIn = sent.resendAfterSeconds
        // The server just confirmed the purchase. Acknowledge it now, or Google refunds it while the person is still signing up.
        runCatching { container.billing.acknowledgeAll() }
    }

    ScreenScaffold(
        title = if (paid) str(R.string.create_your_cloud_account) else str(R.string.stock_anchor_cloud),
        subtitle = if (!paid) null else if (container.session.pendingPromo() != null) str(R.string.promo_code_accepted_one_last_step) else str(R.string.payment_received_one_last_step),
        onBack = { nav.popBackStack() }, busy = runner.busy,
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (paid) AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(10.dp))
                    Text(str(R.string.your_subscription_is_ready_create_an), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (paid) PrimaryTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
                Tab(tab == 0, { tab = 0 }, text = { Text(str(R.string.create_account)) })
                Tab(tab == 1, { tab = 1 }, text = { Text(str(R.string.sign_in)) })
            }
            if (tab == 0) {
                FormField(name, { name = it }, str(R.string.your_name), Modifier.fillMaxWidth(), singleLine = true, maxLength = FieldLimit.NAME, error = Validate.name(name))
                FormField(company, { company = it }, str(R.string.business_name), Modifier.fillMaxWidth(), singleLine = true, maxLength = FieldLimit.COMPANY, counter = true, error = Validate.name(company, FieldLimit.COMPANY))
                DropdownField(str(R.string.currency), listOf("BDT", "USD", "EUR", "GBP", "INR", "PKR"), currency, { it }, { if (it != null) currency = it }, Modifier.fillMaxWidth())
            }
            if (tab == 0 && accountEmail.isNotBlank()) {
                OutlinedTextField(
                    accountEmail, {}, readOnly = true, enabled = false, singleLine = true,
                    label = { Text(str(R.string.google_play_account)) },
                    supportingText = { Text(str(R.string.google_play_account_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else if (tab == 0 && !BuildConfig.ALLOW_TEST_PURCHASES) {
                OutlinedButton(onClick = pickAccount, modifier = Modifier.fillMaxWidth()) { Text(str(R.string.choose_google_account)) }
            } else {
                FormField(email, { email = it }, str(R.string.email), Modifier.fillMaxWidth(), singleLine = true, maxLength = FieldLimit.EMAIL, error = Validate.email(email, required = true),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            }
            FormField(password, { password = it }, str(R.string.password), Modifier.fillMaxWidth(), singleLine = true, maxLength = FieldLimit.PASSWORD,
                error = if (tab == 0) Validate.password(password) else Validate.required(password),
                hint = if (tab == 0) str(R.string.at_least_8_characters) else null,
                visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            Button(
                onClick = {
                    runner.run {
                        if (tab == 0) sendCode()
                        else refusedForNoPlan {
                            challenge = container.account.login(email, password)
                            if (challenge == null) afterAuth()
                        }
                    }
                },
                enabled = !runner.busy && (if (tab == 0) {
                    Validate.password(password) == null && Validate.email(newAccountEmail, required = true) == null &&
                        Validate.name(name) == null && Validate.name(company, FieldLimit.COMPANY) == null
                } else {
                    password.isNotEmpty() && Validate.email(email, required = true) == null
                }),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(if (tab == 0) str(R.string.create_account) else str(R.string.sign_in)) }
            if (!paid) TextButton(onClick = { nav.navigate(Routes.PAYWALL) { popUpTo(Routes.PAYWALL) { inclusive = true } } }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(str(R.string.new_to_stock_anchor_see_plans))
            }
            if (BuildConfig.ALLOW_SERVER_OVERRIDE) Text("Server: ${container.session.baseUrl()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    codeSentTo?.let { address ->
        EmailCodeDialog(
            email = address,
            busy = runner.busy,
            error = codeError,
            resendInSeconds = resendIn,
            onSubmit = { code ->
                runner.run {
                    try {
                        container.account.register(name, address, password, company, currency, code)
                        codeSentTo = null
                    } catch (e: ApiException) {
                        if (e.code == "premium_required") purchaseNoLongerActive() else codeError = e.message
                        return@run
                    }
                    afterAuth()
                }
            },
            onResend = {
                runner.run {
                    try {
                        sendCode()
                    } catch (e: ApiException) {
                        codeError = e.message
                    }
                }
            },
            onDismiss = { codeSentTo = null },
        )
    }

    challenge?.let { pending ->
        TwoFactorCodeDialog(
            busy = runner.busy,
            onSubmit = { code ->
                runner.run {
                    refusedForNoPlan {
                        container.account.completeTwoFactor(pending, code)
                        challenge = null
                        afterAuth()
                    }
                }
            },
            onDismiss = { challenge = null },
        )
    }

    if (notPremium) AlertDialog(
        onDismissRequest = { notPremium = false; if (purchaseRefused) nav.popBackStack() },
        title = { Text(str(R.string.no_active_subscription)) },
        text = { Text(str(if (purchaseRefused) R.string.purchase_no_longer_active else R.string.this_account_has_no_active_plan)) },
        confirmButton = { TextButton(onClick = { notPremium = false; nav.navigate(Routes.PAYWALL) { popUpTo(Routes.PAYWALL) { inclusive = true } } }) { Text(str(R.string.see_plans)) } },
        dismissButton = { TextButton(onClick = { notPremium = false; nav.popBackStack() }) { Text(str(R.string.later)) } },
    )
}

/* ============================================================ Subscription status */

@Composable
fun SubscriptionScreen(nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val runner = rememberRunner()
    var confirmSignOut by remember { mutableStateOf(false) }
    var confirmDowngrade by remember { mutableStateOf(false) }

    ScreenScaffold(str(R.string.plan_account), onBack = { nav.popBackStack() }, busy = runner.busy) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (session.premiumActive) str(R.string.premium) else str(R.string.basic), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    if (session.premiumActive) PremiumChip()
                }
                Spacer(Modifier.height(6.dp))
                KeyValue(str(R.string.data_stored), if (session.isCloud) str(R.string.cloud_synced) else str(R.string.this_device_only))
                if (session.loggedIn) {
                    KeyValue(str(R.string.signed_in_as), session.email)
                    KeyValue(str(R.string.role), session.role.replaceFirstChar { it.uppercase() })
                    KeyValue(str(R.string.billing), session.plan?.replaceFirstChar { it.uppercase() } ?: "—")
                    KeyValue(str(R.string.status), session.subscriptionStatus ?: "—")
                    session.expiresAt?.let { KeyValue(if (session.subscriptionStatus == "canceled") str(R.string.ends) else str(R.string.renews), Fmt.date(Dates.parse(it))) }
                }
            }
            if (!session.premiumActive) {
                Button(onClick = { nav.navigate(Routes.PAYWALL) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Filled.WorkspacePremium, null); Spacer(Modifier.width(8.dp)); Text(str(R.string.upgrade_to_premium))
                }
                if (!session.loggedIn) OutlinedButton(onClick = { nav.navigate(Routes.account("login")) }, modifier = Modifier.fillMaxWidth()) {
                    Text(str(R.string.sign_in_to_an_existing_cloud))
                }
            } else {
                if (session.role == "owner") OutlinedButton(onClick = {
                    val uri = Uri.parse("https://play.google.com/store/account/subscriptions?sku=${container.session.subscriptionProductId()}&package=${context.packageName}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }, modifier = Modifier.fillMaxWidth()) { Text(str(R.string.manage_or_cancel_in_google_play)) }
                if (session.role == "owner") OutlinedButton(onClick = { nav.navigate(Routes.PAYWALL) }, modifier = Modifier.fillMaxWidth()) {
                    Text(str(R.string.switch_monthly_yearly))
                }
                OutlinedButton(onClick = { runner.run(str(R.string.plan_refreshed)) { container.account.refresh() } }, modifier = Modifier.fillMaxWidth()) {
                    Text(str(R.string.refresh_plan_status))
                }
            }
            if (session.loggedIn) TwoFactorCard(runner)
            if (session.isCloud) {
                HorizontalDivider()
                TextButton(onClick = { confirmDowngrade = true }) { Text(str(R.string.work_offline_on_basic_keep_data)) }
            }
            if (session.loggedIn && session.isPremium) TextButton(onClick = { confirmSignOut = true }) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(6.dp))
                Text(str(R.string.sign_out_of_the_cloud), color = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (confirmSignOut) ConfirmDialog(
        str(R.string.sign_out), str(R.string.cloud_data_cached_on_this_device), confirmLabel = str(R.string.sign_out_2),
        onConfirm = { runner.run(str(R.string.signed_out)) { container.account.signOut(); nav.goHome() } }, onDismiss = { confirmSignOut = false },
    )
    if (confirmDowngrade) ConfirmDialog(
        str(R.string.switch_to_basic), str(R.string.you_ll_keep_working_offline_with),
        confirmLabel = str(R.string.switch_label), destructive = false,
        onConfirm = { container.account.downgradeToBasic(); nav.goHome() }, onDismiss = { confirmDowngrade = false },
    )
}

/* ============================================================ More (menu) */

@Composable
fun MoreScreen(nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val premium = session.premiumActive
    val locked = !premium
    val go: (String) -> Unit = { nav.navigate(it) }
    val context = LocalContext.current

    ScreenScaffold(str(R.string.more)) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppCard(Modifier.fillMaxWidth(), onClick = { go(Routes.SUBSCRIPTION) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(session.companyName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            if (session.loggedIn) "${session.userName} · ${session.role}" else str(R.string.basic_plan_data_on_this_device),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (premium) PremiumChip() else Button(onClick = { go(Routes.PAYWALL) }, colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Navy)) {
                        Text(str(R.string.upgrade))
                    }
                }
            }
            SectionTitle(str(R.string.inventory))
            MenuRow(Icons.Filled.Category, str(R.string.categories), str(R.string.organise_your_catalogue)) { go(Routes.CATEGORIES) }
            MenuRow(Icons.Filled.Warehouse, str(R.string.warehouses), if (premium) str(R.string.unlimited_locations) else str(R.string.x_on_basic, Limits.warehouses())) { go(Routes.WAREHOUSES) }
            MenuRow(Icons.Filled.AddBox, str(R.string.purchases), str(R.string.stock_received)) { go(Routes.PURCHASES) }
            if (session.can(Perm.TRANSFERS_CREATE) || locked) MenuRow(Icons.AutoMirrored.Filled.CompareArrows, str(R.string.stock_transfers), str(R.string.between_warehouses), premium = locked) { go(Routes.TRANSFERS) }
            MenuRow(Icons.Filled.History, str(R.string.movement_history), str(R.string.every_stock_change), premium = locked) { go(Routes.movements()) }
            MenuRow(Icons.Filled.Warning, str(R.string.low_stock), str(R.string.products_below_minimum), premium = locked) { go(Routes.LOW_STOCK) }
            MenuRow(Icons.Filled.LocalShipping, str(R.string.suppliers), str(R.string.who_you_buy_from), premium = locked) { go(Routes.SUPPLIERS) }
            SectionTitle(str(R.string.business))
            MenuRow(Icons.AutoMirrored.Filled.ReceiptLong, str(R.string.sales_invoices), str(R.string.history_cancellations_pdf)) { go(Routes.SALES) }
            if (session.can(Perm.EXPENSES_MANAGE) || session.can(Perm.REPORTS_VIEW) || locked) MenuRow(Icons.Filled.Payments, str(R.string.expenses), str(R.string.rent_salaries_for_net_profit), premium = locked) { go(Routes.EXPENSES) }
            if (session.can(Perm.DATA_EXPORT) || session.can(Perm.DATA_IMPORT) || locked) MenuRow(Icons.Filled.ImportExport, str(R.string.import_export_csv), str(R.string.spreadsheets_in_and_out), premium = locked) { go(Routes.DATA) }
            if (session.can(Perm.TEAM_MANAGE) || locked) MenuRow(Icons.Filled.Group, str(R.string.team_permissions), str(R.string.staff_accounts), premium = locked) { go(Routes.TEAM) }
            if (session.can(Perm.BACKUPS_MANAGE) || locked) MenuRow(Icons.Filled.Backup, str(R.string.cloud_backup), str(R.string.backup_restore), premium = locked) { go(Routes.BACKUPS) }
            SectionTitle(str(R.string.app))
            MenuRow(Icons.Filled.WorkspacePremium, str(R.string.plan_account), if (premium) str(R.string.premium) else str(R.string.basic), tint = Amber) { go(Routes.SUBSCRIPTION) }
            MenuRow(Icons.Filled.Settings, str(R.string.settings), str(R.string.business_profile_alerts_sync)) { go(Routes.SETTINGS) }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (premium) {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.WEB_PANEL_URL))) }
                            .onFailure { Toast.makeText(context, str(R.string.no_web_browser_found_please_install), Toast.LENGTH_LONG).show() }
                    } else {
                        go(Routes.PAYWALL)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Filled.OpenInBrowser, null); Spacer(Modifier.width(8.dp))
                Text(str(R.string.open_web_panel))
                if (locked) { Spacer(Modifier.width(8.dp)); PremiumChip() }
            }
            Text(
                if (premium) str(R.string.use_premium_features_in_your_browser, Uri.parse(BuildConfig.WEB_PANEL_URL).host)
                else str(R.string.the_web_panel_is_part_of),
                Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            Text("Stock Anchor ${BuildConfig.VERSION_NAME}", Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ============================================================ Settings */

@Composable
fun SettingsScreen(nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val runner = rememberRunner()
    var company by rememberSaveable { mutableStateOf(session.companyName) }
    var currency by rememberSaveable { mutableStateOf(session.currency) }
    var server by rememberSaveable { mutableStateOf(container.session.baseUrl()) }

    ScreenScaffold(str(R.string.settings), onBack = { nav.popBackStack() }, busy = runner.busy) { pad ->
        Column(Modifier.padding(pad).imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(str(R.string.business_profile))
            AppCard(Modifier.fillMaxWidth()) {
                FormField(company, { company = it }, str(R.string.business_name_on_invoices), Modifier.fillMaxWidth(), singleLine = true, maxLength = FieldLimit.COMPANY, counter = true, error = Validate.name(company, FieldLimit.COMPANY))
                Spacer(Modifier.height(8.dp))
                // The cloud stores a currency code, so there only the symbols it knows can be kept.
                val currencyError = Validate.required(currency)
                    ?: str(R.string.use_a_supported_currency_x, SessionStore.SUPPORTED_SYMBOLS).takeIf { session.isCloud && SessionStore.codeFor(currency) == null }
                FormField(currency, { currency = it }, str(R.string.currency_symbol), Modifier.fillMaxWidth(), singleLine = true, maxLength = FieldLimit.CURRENCY_SYMBOL, error = currencyError)
                Spacer(Modifier.height(8.dp))
                val canEdit = !session.isCloud || session.can(Perm.TEAM_MANAGE)
                Button(
                    enabled = canEdit && !runner.busy && Validate.name(company, FieldLimit.COMPANY) == null && currencyError == null,
                    onClick = {
                        runner.run(str(R.string.saved)) {
                            container.account.saveCompany(company.trim(), currency.trim())
                            company = container.session.current.companyName
                            currency = container.session.current.currency
                        }
                    },
                ) { Text(str(R.string.save)) }
                if (!canEdit) Text(str(R.string.only_a_team_manager_can_change_this), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            }
            SectionTitle(str(R.string.language))
            AppCard(Modifier.fillMaxWidth()) {
                val activity = LocalActivity.current ?: return@AppCard
                val currentLanguage = remember { I18n.current(activity) }
                DropdownField(
                    str(R.string.language), I18n.LANGUAGES.map { it.first }, currentLanguage,
                    { tag -> I18n.LANGUAGES.first { it.first == tag }.second ?: str(R.string.language_auto_x, I18n.nameOf(I18n.automatic(activity))) },
                    { tag -> if (tag != null && tag != currentLanguage) I18n.setLanguage(activity, tag) },
                    Modifier.fillMaxWidth(),
                )
                Text(str(R.string.language_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SectionTitle(str(R.string.notifications))
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text(str(R.string.low_stock_channel)); Spacer(Modifier.width(6.dp)); if (!session.premiumActive) PremiumChip() }
                        Text(str(R.string.daily_reminders_for_products_below_minimum), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(session.lowStockAlerts, { container.session.setLowStockAlerts(it) }, enabled = session.premiumActive)
                }
            }
            if (BuildConfig.ALLOW_SERVER_OVERRIDE) {
                SectionTitle(str(R.string.cloud_server))
                AppCard(Modifier.fillMaxWidth()) {
                    OutlinedTextField(server, { server = it }, label = { Text("API base URL") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Your Laravel API, ending in /api/ (debug builds only)") })
                    Row {
                        TextButton(onClick = { container.session.setBaseUrl(server); runner.toast(str(R.string.server_updated)) }) { Text(str(R.string.save)) }
                        TextButton(onClick = { server = BuildConfig.API_BASE_URL; container.session.setBaseUrl("") }) { Text(str(R.string.reset)) }
                    }
                }
            }
            if (session.isCloud) {
                SectionTitle(str(R.string.cloud_sync))
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(str(R.string.last_synced))
                            Text(session.lastSync?.let { Fmt.dateTime(Dates.parse(it)) } ?: str(R.string.never), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = { runner.run(str(R.string.full_sync_complete)) { container.sync.pull(full = true) } }) { Text(str(R.string.full_re_sync)) }
                    }
                }
            }
        }
    }
}
