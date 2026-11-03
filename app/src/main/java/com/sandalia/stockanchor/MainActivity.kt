package com.sandalia.stockanchor

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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var startRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startRoute = intent?.getStringExtra(EXTRA_ROUTE)

        val container = (application as StockAnchorApp).container

        if (Build.VERSION.SDK_INT >= 33 &&
            androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Keep the cache fresh and the plan status current whenever the app comes to the foreground.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (container.session.current.isCloud) {
                    runCatching { container.account.refresh() }
                    runCatching { container.sync.pull() }
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
                        ApiEvent.UNAUTHENTICATED -> "Your session expired. Please sign in again."
                        ApiEvent.ACCOUNT_DISABLED -> "Your account has been disabled by your team owner."
                        ApiEvent.TEAM_SUSPENDED -> "Your company account is suspended. Please contact Stock Anchor support."
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
                            title = { Text("Premium has ended") },
                            text = {
                                Text(
                                    "Your team's Stock Anchor Premium subscription is no longer active. Renew to keep using the cloud, " +
                                        "or continue on Basic — everything already synced stays on this device."
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { showLapsed = false; startRoute = "paywall" }) { Text("Renew") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showLapsed = false
                                    scope.launch {
                                        container.account.downgradeToBasic()
                                        snackbar.showSnackbar("Switched to Basic. Your data is kept on this device.")
                                    }
                                }) { Text("Continue on Basic") }
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
