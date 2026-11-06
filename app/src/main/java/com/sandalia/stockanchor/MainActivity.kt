package com.sandalia.stockanchor

import com.sandalia.stockanchor.util.str
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sandalia.stockanchor.data.remote.ApiEvent
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.LocalSnackbar
import com.sandalia.stockanchor.ui.navigation.AppNav
import com.sandalia.stockanchor.ui.navigation.Routes
import com.sandalia.stockanchor.ui.theme.StockAnchorTheme
import com.sandalia.stockanchor.util.I18n
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var startRoute by mutableStateOf<String?>(null)

    // Applies the language chosen in Settings (and right-to-left layout for Arabic).
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(I18n.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startRoute = intent?.getStringExtra(EXTRA_ROUTE)

        val container = (application as StockAnchorApp).container

        // Once per launch: a rotation or fold recreates the activity and must not ask again.
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= 33 &&
            androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Keep the cache fresh and the plan status current whenever the app comes to the foreground.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val session = container.session.current
                if (session.isCloud) {
                    runCatching { container.account.refresh() }
                    runCatching { container.sync.pull() }
                } else if (session.loggedIn && !session.isPremium) {
                    // Left by an older version, which kept the sign-in without a plan. Local data stays.
                    if (runCatching { container.account.refresh() }.getOrNull() == false) container.account.discardUnpaidSession()
                }
            }
        }

        setContent {
            val session by container.session.state.collectAsStateWithLifecycle()
            val snackbar = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            var showLapsed by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                container.api.events.collect { event ->
                    val ended = when (event) {
                        ApiEvent.SUBSCRIPTION_REQUIRED -> { showLapsed = true; null }
                        ApiEvent.UNAUTHENTICATED -> str(R.string.your_session_expired_please_sign_in)
                        ApiEvent.ACCOUNT_DISABLED -> str(R.string.your_account_has_been_disabled_by)
                        ApiEvent.TEAM_SUSPENDED -> str(R.string.your_company_account_is_suspended_please)
                    } ?: return@collect
                    // Leave the cloud exactly like "Sign out", then go home and say why.
                    scope.launch {
                        if (container.account.endSession()) {
                            showLapsed = false
                            startRoute = Routes.DASHBOARD
                            snackbar.showSnackbar(ended)
                        }
                    }
                }
            }

            StockAnchorTheme {
                CompositionLocalProvider(LocalContainer provides container, LocalSnackbar provides snackbar) {
                    AppNav(startRoute = startRoute, onStartRouteConsumed = { startRoute = null })

                    if (showLapsed && session.isCloud) {
                        AlertDialog(
                            onDismissRequest = { showLapsed = false },
                            title = { Text(str(R.string.premium_has_ended)) },
                            text = {
                                Text(
                                    str(R.string.your_team_s_stock_anchor_premium)
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { showLapsed = false; startRoute = "paywall" }) { Text(str(R.string.renew)) }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showLapsed = false
                                    scope.launch {
                                        container.account.downgradeToBasic()
                                        snackbar.showSnackbar(str(R.string.switched_to_basic_your_data_is))
                                    }
                                }) { Text(str(R.string.continue_on_basic)) }
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(EXTRA_ROUTE)?.let { startRoute = it }
    }

    companion object {
        const val EXTRA_ROUTE = "route"
    }
}
