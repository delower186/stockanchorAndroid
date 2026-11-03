package com.sandalia.stockanchor.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandalia.stockanchor.domain.Perm
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.LocalOpenPaywall
import com.sandalia.stockanchor.ui.screens.AccountScreen
import com.sandalia.stockanchor.ui.screens.BackupScreen
import com.sandalia.stockanchor.ui.screens.CategoriesScreen
import com.sandalia.stockanchor.ui.screens.DashboardScreen
import com.sandalia.stockanchor.ui.screens.DataToolsScreen
import com.sandalia.stockanchor.ui.screens.ExpensesScreen
import com.sandalia.stockanchor.ui.screens.InvoiceScreen
import com.sandalia.stockanchor.ui.screens.LowStockScreen
import com.sandalia.stockanchor.ui.screens.MoreScreen
import com.sandalia.stockanchor.ui.screens.MovementsScreen
import com.sandalia.stockanchor.ui.screens.PaywallScreen
import com.sandalia.stockanchor.ui.screens.ProductDetailScreen
import com.sandalia.stockanchor.ui.screens.ProductEditScreen
import com.sandalia.stockanchor.ui.screens.ProductsScreen
import com.sandalia.stockanchor.ui.screens.PurchaseScreen
import com.sandalia.stockanchor.ui.screens.PurchasesScreen
import com.sandalia.stockanchor.ui.screens.SalesScreen
import com.sandalia.stockanchor.ui.screens.SellScreen
import com.sandalia.stockanchor.ui.screens.SettingsScreen
import com.sandalia.stockanchor.ui.screens.SubscriptionScreen
import com.sandalia.stockanchor.ui.screens.SuppliersScreen
import com.sandalia.stockanchor.ui.screens.TeamScreen
import com.sandalia.stockanchor.ui.screens.TransfersScreen
import com.sandalia.stockanchor.ui.screens.WarehousesScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val SELL = "sell"
    const val SALES = "sales"
    const val MORE = "more"
    const val PRODUCT_EDIT = "product_edit/{id}"
    const val PRODUCT = "product/{id}"
    const val CATEGORIES = "categories"
    const val WAREHOUSES = "warehouses"
    const val SUPPLIERS = "suppliers"
    const val PURCHASE = "purchase"
    const val PURCHASES = "purchases"
    const val INVOICE = "invoice/{id}"
    const val TRANSFERS = "transfers"
    const val MOVEMENTS = "movements?productId={productId}"
    const val LOW_STOCK = "low_stock"
    const val DATA = "data"
    const val TEAM = "team"
    const val BACKUPS = "backups"
    const val PAYWALL = "paywall"
    const val ACCOUNT = "account?mode={mode}"
    const val SUBSCRIPTION = "subscription"
    const val SETTINGS = "settings"
    const val EXPENSES = "expenses"

    fun productEdit(id: Long = 0) = "product_edit/$id"
    fun product(id: Long) = "product/$id"
    fun invoice(id: Long) = "invoice/$id"
    fun movements(productId: Long? = null) = "movements?productId=${productId ?: -1}"
    /** mode: "upgrade" (after purchase), "login" or "register" */
    fun account(mode: String) = "account?mode=$mode"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val tabs = listOf(
    Tab(Routes.DASHBOARD, "Dashboard", Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
    Tab(Routes.PRODUCTS, "Products", Icons.Outlined.Inventory2, Icons.Filled.Inventory2),
    Tab(Routes.SELL, "Sell", Icons.Outlined.PointOfSale, Icons.Filled.PointOfSale),
    Tab(Routes.SALES, "Sales", Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Filled.ReceiptLong),
    Tab(Routes.MORE, "More", Icons.Outlined.Apps, Icons.Filled.Apps),
)

@Composable
fun AppNav(startRoute: String?, onStartRouteConsumed: () -> Unit) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val session by LocalContainer.current.session.state.collectAsStateWithLifecycle()
    // Members without sales.create don't get the Sell tab.
    val visibleTabs = tabs.filter { it.route != Routes.SELL || session.can(Perm.SALES_CREATE) }
    val showBar = tabs.any { it.route == current }

    LaunchedEffect(startRoute) {
        if (startRoute != null) {
            // Dashboard means "leave whatever flow is open" (e.g. after the session ended), not a push on top of it.
            if (startRoute == Routes.DASHBOARD) nav.goHome() else nav.navigate(startRoute)
            onStartRouteConsumed()
        }
    }

    CompositionLocalProvider(LocalOpenPaywall provides { nav.navigate(Routes.PAYWALL) }) {
        Scaffold(
            bottomBar = {
                if (showBar) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    visibleTabs.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { nav.switchTab(tab.route) },
                            icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = tab.label) },
                            // Five tabs on a narrow screen: keep labels on one line ("Dashboard" used to wrap).
                            label = { Text(tab.label, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { inner ->
            // Only the bottom bar is handled here; each screen's own top bar still needs the status bar inset.
            val bottom = PaddingValues(bottom = inner.calculateBottomPadding())
            NavHost(nav, startDestination = Routes.DASHBOARD, modifier = Modifier.padding(bottom).consumeWindowInsets(bottom)) {
                val back: () -> Unit = { nav.popBackStack() }

                composable(Routes.DASHBOARD) { DashboardScreen(nav) }
                composable(Routes.PRODUCTS) { ProductsScreen(nav) }
                composable(Routes.SELL) { SellScreen(onDone = { saleId -> nav.navigate(Routes.invoice(saleId)) }, nav = nav) }
                composable(Routes.SALES) { SalesScreen(nav) }
                composable(Routes.MORE) { MoreScreen(nav) }

                composable(Routes.PRODUCT_EDIT, listOf(navArgument("id") { type = NavType.LongType })) {
                    ProductEditScreen(it.arguments?.getLong("id") ?: 0L, back)
                }
                composable(Routes.PRODUCT, listOf(navArgument("id") { type = NavType.LongType })) {
                    ProductDetailScreen(it.arguments?.getLong("id") ?: 0L, nav)
                }
                composable(Routes.CATEGORIES) { CategoriesScreen(back) }
                composable(Routes.WAREHOUSES) { WarehousesScreen(back) }
                composable(Routes.SUPPLIERS) { SuppliersScreen(back) }
                composable(Routes.PURCHASE) { PurchaseScreen(back) }
                composable(Routes.PURCHASES) { PurchasesScreen(nav) }
                composable(Routes.INVOICE, listOf(navArgument("id") { type = NavType.LongType })) {
                    InvoiceScreen(it.arguments?.getLong("id") ?: 0L, back)
                }
                composable(Routes.TRANSFERS) { TransfersScreen(back) }
                composable(Routes.EXPENSES) { ExpensesScreen(back) }
                composable(Routes.MOVEMENTS, listOf(navArgument("productId") { type = NavType.LongType; defaultValue = -1L })) {
                    MovementsScreen(it.arguments?.getLong("productId")?.takeIf { id -> id > 0 }, back)
                }
                composable(Routes.LOW_STOCK) { LowStockScreen(nav) }
                composable(Routes.DATA) { DataToolsScreen(back) }
                composable(Routes.TEAM) { TeamScreen(back) }
                composable(Routes.BACKUPS) { BackupScreen(back) }
                composable(Routes.PAYWALL) { PaywallScreen(nav) }
                composable(Routes.ACCOUNT, listOf(navArgument("mode") { type = NavType.StringType; defaultValue = "login" })) {
                    AccountScreen(it.arguments?.getString("mode") ?: "login", nav)
                }
                composable(Routes.SUBSCRIPTION) { SubscriptionScreen(nav) }
                composable(Routes.SETTINGS) { SettingsScreen(nav) }
            }
        }
    }
}

fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Leaves a flow (sign in, sign out, activation) for the dashboard. Unlike [switchTab] it neither saves nor restores
 * tab state, which could bring the account screen straight back.
 */
fun NavHostController.goHome() {
    navigate(Routes.DASHBOARD) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}
