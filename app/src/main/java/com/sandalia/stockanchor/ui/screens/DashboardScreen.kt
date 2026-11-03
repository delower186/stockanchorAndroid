package com.sandalia.stockanchor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.sandalia.stockanchor.domain.Perm
import com.sandalia.stockanchor.AppContainer
import com.sandalia.stockanchor.data.local.DayBucket
import com.sandalia.stockanchor.data.local.PeriodTotals
import com.sandalia.stockanchor.data.local.ProductWithStock
import com.sandalia.stockanchor.data.local.StockTotals
import com.sandalia.stockanchor.ui.components.AppCard
import com.sandalia.stockanchor.ui.components.BarChart
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.LocalOpenPaywall
import com.sandalia.stockanchor.ui.components.PremiumChip
import com.sandalia.stockanchor.ui.components.SectionTitle
import com.sandalia.stockanchor.ui.components.StatCard
import com.sandalia.stockanchor.ui.components.StockPill
import com.sandalia.stockanchor.ui.components.rememberRunner
import com.sandalia.stockanchor.ui.navigation.Routes
import com.sandalia.stockanchor.ui.theme.Amber
import com.sandalia.stockanchor.ui.theme.Navy
import com.sandalia.stockanchor.ui.theme.NavyLight
import com.sandalia.stockanchor.ui.theme.Positive
import com.sandalia.stockanchor.ui.theme.Teal
import com.sandalia.stockanchor.ui.theme.TealLight
import com.sandalia.stockanchor.util.Dates
import com.sandalia.stockanchor.util.Fmt
import com.sandalia.stockanchor.util.Period
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class DashboardState(
    val period: Period = Period.DAY,
    val totals: PeriodTotals = PeriodTotals(0, 0.0, 0.0),
    val purchases: Double = 0.0,
    val itemsSold: Double = 0.0,
    val stockIn: Double = 0.0,
    val stock: StockTotals = StockTotals(0.0, 0.0, 0.0),
    val chartValues: List<Double> = emptyList(),
    val chartLabels: List<String> = emptyList(),
    val lowStock: List<ProductWithStock> = emptyList(),
    val expenses: Double = 0.0,
) {
    /** Gross profit: sales minus cost of goods. */
    val profit get() = totals.revenue - totals.cost
    val margin get() = if (totals.revenue > 0) profit / totals.revenue * 100 else 0.0
    /** Net profit: gross profit minus expenses. */
    val netProfit get() = profit - expenses
    val netMargin get() = if (totals.revenue > 0) netProfit / totals.revenue * 100 else 0.0
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(container: AppContainer) : ViewModel() {
    private val reports = container.db.reports()
    val period = MutableStateFlow(Period.DAY)

    val state = period.flatMapLatest { p ->
        val (from, to) = Dates.range(p)
        val main = combine(
            reports.periodTotals(from, to),
            reports.purchaseTotal(from, to),
            reports.itemsSold(from, to),
            reports.stockIn(from, to),
            reports.stockTotals(),
        ) { totals, purchases, sold, stockIn, stock -> DashboardState(p, totals, purchases, sold, stockIn, stock) }
        combine(main, reports.buckets(p.sqlFormat, from, to), container.repo.lowStock(), reports.expenseTotal(from, to)) { s, buckets, low, expenses ->
            val (labels, values) = fillSeries(p, buckets)
            s.copy(chartLabels = labels, chartValues = values, lowStock = low, expenses = expenses)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    private fun fillSeries(p: Period, buckets: List<DayBucket>): Pair<List<String>, List<Double>> {
        val map = buckets.associate { it.bucket to it.revenue }
        val today = LocalDate.now()
        return when (p) {
            Period.DAY -> (0..23).map { h -> "%02d".format(h) }.let { keys -> keys to keys.map { map[it] ?: 0.0 } }
            Period.MONTH -> (1..today.lengthOfMonth()).map { d -> today.withDayOfMonth(d) }
                .let { days -> days.map { it.dayOfMonth.toString() } to days.map { map[it.toString()] ?: 0.0 } }
            Period.YEAR -> (1..12).map { m -> today.withMonth(m).withDayOfMonth(1) }.let { months ->
                months.map { it.month.getDisplayName(TextStyle.NARROW, Locale.getDefault()) } to
                    months.map { map["%04d-%02d".format(it.year, it.monthValue)] ?: 0.0 }
            }
        }
    }
}

@Composable
fun DashboardScreen(nav: NavHostController) {
    val container = LocalContainer.current
    val vm: DashboardViewModel = viewModel(factory = viewModelFactory { initializer { DashboardViewModel(container) } })
    val state by vm.state.collectAsStateWithLifecycle()
    val session by container.session.state.collectAsStateWithLifecycle()
    val syncing by container.sync.syncing.collectAsStateWithLifecycle()
    val openPaywall = LocalOpenPaywall.current
    val runner = rememberRunner()
    val cur = session.currency
    val canReports = session.can(Perm.REPORTS_VIEW)

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.statusBarsPadding(),
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(session.companyName, style = MaterialTheme.typography.headlineMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (session.isCloud) Icons.Filled.Cloud else Icons.Filled.CloudOff, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (session.isCloud) "Cloud · ${session.userName}" else "Basic · on this device",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (session.isCloud) {
                    IconButton(onClick = { runner.run("Synced") { container.sync.pull() } }, enabled = !syncing) {
                        Icon(Icons.Filled.Sync, "Sync now")
                    }
                }
                if (session.premiumActive) PremiumChip() else TextButton(onClick = openPaywall) {
                    Icon(Icons.Filled.WorkspacePremium, null, tint = Amber); Spacer(Modifier.width(4.dp)); Text("Upgrade")
                }
            }
        }

        // Revenue, profit, stock value and purchases are reports: hidden without reports.view.
        if (canReports) {
        item {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Period.entries.forEachIndexed { i, p ->
                    SegmentedButton(
                        selected = state.period == p,
                        onClick = { vm.period.value = p },
                        shape = SegmentedButtonDefaults.itemShape(i, Period.entries.size),
                    ) { Text(p.label) }
                }
            }
        }

        item { HeroCard(state, cur, showNet = session.premiumActive) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total stock", Fmt.qty(state.stock.quantity), Icons.Filled.Inventory, Teal, Modifier.weight(1f),
                    caption = "+${Fmt.qty(state.stockIn)} in ${state.period.label.lowercase()}")
                StatCard("Stock value", Fmt.moneyShort(cur, state.stock.costValue), Icons.Filled.Warehouse, MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f), caption = "Retail ${Fmt.moneyShort(cur, state.stock.retailValue)}")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Purchases", Fmt.moneyShort(cur, state.purchases), Icons.Filled.LocalShipping, Amber, Modifier.weight(1f),
                    caption = state.period.label)
                StatCard("Items sold", Fmt.qty(state.itemsSold), Icons.Filled.ShoppingCart, Positive, Modifier.weight(1f),
                    caption = "${state.totals.salesCount} invoices")
            }
        }

        item {
            AppCard(Modifier.fillMaxWidth()) {
                Text("Revenue", style = MaterialTheme.typography.titleMedium)
                Text(
                    when (state.period) { Period.DAY -> "By hour, today"; Period.MONTH -> "By day, this month"; Period.YEAR -> "By month, this year" },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                BarChart(state.chartValues, state.chartLabels)
            }
        }
        }

        val canSell = session.can(Perm.SALES_CREATE)
        val canPurchase = session.can(Perm.PURCHASES_CREATE)
        val canCatalog = session.can(Perm.CATALOG_MANAGE)
        if (canSell || canPurchase || canCatalog) item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (canSell) QuickAction("New sale", Icons.Filled.PointOfSale, Modifier.weight(1f)) { nav.navigate(Routes.SELL) }
                if (canPurchase) QuickAction("Add stock", Icons.Filled.AddBox, Modifier.weight(1f)) { nav.navigate(Routes.PURCHASE) }
                if (canCatalog) QuickAction("Product", Icons.Filled.Inventory, Modifier.weight(1f)) { nav.navigate(Routes.productEdit()) }
            }
        }

        item {
            SectionTitle("Low stock", action = {
                if (session.premiumActive && state.lowStock.isNotEmpty()) TextButton(onClick = { nav.navigate(Routes.LOW_STOCK) }) { Text("See all") }
            })
            if (!session.premiumActive) {
                AppCard(Modifier.fillMaxWidth(), onClick = openPaywall) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = Amber)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Low stock alerts", style = MaterialTheme.typography.titleSmall)
                            Text("Get notified before you run out.", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        PremiumChip()
                    }
                }
            } else if (state.lowStock.isEmpty()) {
                AppCard(Modifier.fillMaxWidth()) {
                    Text("All products are above their minimum level. 🎉", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                AppCard(Modifier.fillMaxWidth()) {
                    state.lowStock.take(5).forEach { p ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text("Min ${Fmt.qty(p.lowStockThreshold)} ${p.unit}", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StockPill(p.quantity, p.lowStockThreshold, p.unit)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(state: DashboardState, cur: String, showNet: Boolean) {
    val profitColor = if (state.profit >= 0) TealLight else Color(0xFFFF9A9E)
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(Navy, NavyLight, Color(0xFF0D5A6B))))
            .padding(20.dp)
    ) {
        Column {
            Text("TOTAL SALES · ${state.period.label.uppercase()}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB8C7D9))
            Spacer(Modifier.height(6.dp))
            Text(Fmt.money(cur, state.totals.revenue), style = MaterialTheme.typography.displaySmall, color = Color.White)
            Spacer(Modifier.height(16.dp))
            Row {
                HeroMetric(
                    if (state.profit >= 0) "Gross profit" else "Gross loss", Fmt.money(cur, state.profit), profitColor,
                    if (state.profit >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, Modifier.weight(1f),
                )
                HeroMetric("Cost of goods", Fmt.money(cur, state.totals.cost), Color.White, Icons.Filled.MonetizationOn, Modifier.weight(1f))
            }
            // Expenses / net profit are Premium.
            if (showNet) Spacer(Modifier.height(10.dp))
            if (showNet) Row {
                HeroMetric("Expenses", "−" + Fmt.money(cur, state.expenses), Color.White, Icons.Filled.Payments, Modifier.weight(1f))
                HeroMetric(
                    if (state.netProfit >= 0) "Net profit" else "Net loss", Fmt.money(cur, state.netProfit),
                    if (state.netProfit >= 0) TealLight else Color(0xFFFF9A9E), Icons.Filled.AccountBalanceWallet, Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
                Text(
                    "${state.totals.salesCount} sales · margin ${"%.1f".format(state.margin)}%" +
                        if (showNet) " · net ${"%.1f".format(state.netMargin)}%" else "",
                    Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium, color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String, color: Color, icon: ImageVector, modifier: Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFFB8C7D9))
        }
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(6.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(label, maxLines = 1)
    }
}
