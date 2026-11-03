package com.sandalia.stockanchor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.sandalia.stockanchor.data.local.SaleStatus
import com.sandalia.stockanchor.domain.Perm
import com.sandalia.stockanchor.ui.components.AppCard
import com.sandalia.stockanchor.ui.components.EmptyState
import com.sandalia.stockanchor.ui.components.KeyValue
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.LocalOpenPaywall
import com.sandalia.stockanchor.ui.components.PremiumChip
import com.sandalia.stockanchor.ui.components.ScreenScaffold
import com.sandalia.stockanchor.ui.components.SearchField
import com.sandalia.stockanchor.ui.components.rememberRunner
import com.sandalia.stockanchor.ui.navigation.Routes
import com.sandalia.stockanchor.ui.theme.Coral
import com.sandalia.stockanchor.ui.theme.Navy
import com.sandalia.stockanchor.ui.theme.Teal
import com.sandalia.stockanchor.util.Dates
import com.sandalia.stockanchor.util.Fmt
import com.sandalia.stockanchor.util.InvoicePdf
import com.sandalia.stockanchor.util.ShareIntents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private enum class SaleRange(val label: String) { TODAY("Today"), WEEK("7 days"), MONTH("This month"), YEAR("This year"), ALL("All") }

@Composable
fun SalesScreen(nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val openPaywall = LocalOpenPaywall.current
    val premium = session.premiumActive
    var query by rememberSaveable { mutableStateOf("") }
    var range by rememberSaveable { mutableStateOf(SaleRange.MONTH) }

    // Basic: the last 30 days, no filters. Premium: full sales management.
    val (from, to) = if (!premium) Dates.startOfDay(LocalDate.now().minusDays(30)) to Long.MAX_VALUE else when (range) {
        SaleRange.TODAY -> Dates.startOfDay() to Long.MAX_VALUE
        SaleRange.WEEK -> Dates.startOfDay(LocalDate.now().minusDays(6)) to Long.MAX_VALUE
        SaleRange.MONTH -> Dates.startOfDay(LocalDate.now().withDayOfMonth(1)) to Long.MAX_VALUE
        SaleRange.YEAR -> Dates.startOfDay(LocalDate.now().withDayOfYear(1)) to Long.MAX_VALUE
        SaleRange.ALL -> 0L to Long.MAX_VALUE
    }
    val q = if (premium) query else ""
    val sales by remember(q, from, to) { container.repo.sales(q, from, to, if (premium) 1000 else 100) }.collectAsStateWithLifecycle(emptyList())
    val completed = sales.filter { it.status == SaleStatus.COMPLETED }

    ScreenScaffold("Sales", subtitle = "${completed.size} sales · ${Fmt.money(session.currency, completed.sumOf { it.total })}") { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (premium) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SearchField(query, { query = it }, "Customer, phone or invoice no.")
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SaleRange.entries.forEach { r -> FilterChip(range == r, onClick = { range = r }, label = { Text(r.label) }) }
                    }
                }
            } else {
                AppCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), onClick = openPaywall) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Showing the last 30 days. Search, date ranges, cancellations and PDF invoices come with Premium.",
                            Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(8.dp)); PremiumChip()
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (sales.isEmpty()) {
                EmptyState(Icons.Filled.PointOfSale, "No sales", "Completed sales and their invoices appear here.", action = {
                    if (session.can(Perm.SALES_CREATE)) Button(onClick = { nav.navigate(Routes.SELL) }) { Text("New sale") }
                })
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sales, key = { it.id }) { s ->
                        val cancelled = s.status == SaleStatus.CANCELLED
                        AppCard(Modifier.fillMaxWidth(), onClick = { nav.navigate(Routes.invoice(s.id)) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(s.customerName, style = MaterialTheme.typography.titleSmall)
                                    Text("${s.invoiceNo} · ${Fmt.dateTime(s.date)}", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(Fmt.money(session.currency, s.total), fontWeight = FontWeight.Bold,
                                        textDecoration = if (cancelled) TextDecoration.LineThrough else null)
                                    if (cancelled) Text("Cancelled", color = Coral, style = MaterialTheme.typography.labelSmall)
                                    else Text("+" + Fmt.money(session.currency, s.total - s.totalCost), color = MaterialTheme.colorScheme.secondary,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================ Invoice */

@Composable
fun InvoiceScreen(saleId: Long, onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val sale by remember(saleId) { container.repo.sale(saleId) }.collectAsStateWithLifecycle(null)
    val items by remember(saleId) { container.repo.saleItems(saleId) }.collectAsStateWithLifecycle(emptyList())
    val warehouses by container.repo.warehouses().collectAsStateWithLifecycle(emptyList())
    val context = LocalContext.current
    val runner = rememberRunner()
    val openPaywall = LocalOpenPaywall.current
    var cancelling by remember { mutableStateOf(false) }

    val s = sale
    ScreenScaffold("Invoice", subtitle = s?.invoiceNo, onBack = onBack, busy = runner.busy) { pad ->
        if (s == null) return@ScreenScaffold
        val cancelled = s.status == SaleStatus.CANCELLED
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                    Column {
                        Box(Modifier.fillMaxWidth().background(Navy).padding(20.dp)) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(session.companyName, color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                                    Text("INVOICE", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                }
                                Text(s.invoiceNo, color = Color(0xFFB8C7D9), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(4.dp).background(Teal))
                        Column(Modifier.padding(20.dp)) {
                            Row {
                                Column(Modifier.weight(1f)) {
                                    Text("BILLED TO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(s.customerName, style = MaterialTheme.typography.titleMedium)
                                    s.customerPhone?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("DATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Fmt.date(s.date), style = MaterialTheme.typography.bodyMedium)
                                    Text(Fmt.time(s.date), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = (if (cancelled) Coral else Teal).copy(alpha = 0.14f),
                            ) {
                                Text(if (cancelled) "CANCELLED" else "PAID", Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    color = if (cancelled) Coral else Teal, style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(Modifier.height(16.dp))
                            items.forEach { i ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        Text(i.productName, fontWeight = FontWeight.Medium)
                                        Text("${Fmt.qty(i.quantity)} × ${Fmt.money(session.currency, i.unitPrice)}",
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(Fmt.money(session.currency, i.quantity * i.unitPrice), fontWeight = FontWeight.SemiBold)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                            Spacer(Modifier.height(8.dp))
                            KeyValue("Subtotal", Fmt.money(session.currency, s.subtotal))
                            if (s.discount > 0) KeyValue("Discount", "-" + Fmt.money(session.currency, s.discount))
                            KeyValue("Total", Fmt.money(session.currency, s.total), strong = true)
                            if (session.can(Perm.REPORTS_VIEW)) KeyValue("Profit", Fmt.money(session.currency, s.total - s.totalCost),
                                valueColor = MaterialTheme.colorScheme.secondary)
                            warehouses.firstOrNull { it.id == s.warehouseId }?.let { KeyValue("Warehouse", it.name) }
                            s.note?.let { Spacer(Modifier.height(6.dp)); Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val text = InvoicePdf.asText(s, items, session.companyName, session.currency)
                        if (!ShareIntents.shareText(context, text, "Invoice ${s.invoiceNo}")) runner.toast("No app available to share with.")
                    }, modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Filled.Share, null); Spacer(Modifier.width(6.dp)); Text("Share")
                    }
                    Button(onClick = {
                        if (!session.premiumActive) openPaywall() else runner.run {
                            val file = withContext(Dispatchers.IO) {
                                InvoicePdf.create(context, s, items, session.companyName, session.currency,
                                    warehouses.firstOrNull { it.id == s.warehouseId }?.name)
                            }
                            // Open in a PDF viewer; fall back to sending it when no viewer is installed.
                            if (!InvoicePdf.open(context, file) && !InvoicePdf.share(context, file, "Invoice ${s.invoiceNo}")) {
                                runner.toast("No app available to open or share PDFs.")
                            }
                        }
                    }, modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Filled.PictureAsPdf, null); Spacer(Modifier.width(6.dp)); Text("PDF")
                        if (!session.premiumActive) { Spacer(Modifier.width(6.dp)); PremiumChip() }
                    }
                }
            }
            if (!cancelled && session.premiumActive && session.can(Perm.SALES_MANAGE)) item {
                TextButton(onClick = { cancelling = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Block, null, tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(6.dp))
                    Text("Cancel sale & return stock", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (cancelling) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { cancelling = false },
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) },
            title = { Text("Cancel this sale?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("All items go back into stock and the sale is excluded from reports.")
                    OutlinedTextField(reason, { reason = it }, label = { Text("Reason") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = { cancelling = false; runner.run("Sale cancelled") { container.repo.cancelSale(saleId, reason) } }) {
                    Text("Cancel sale", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { cancelling = false }) { Text("Keep") } },
        )
    }
}
