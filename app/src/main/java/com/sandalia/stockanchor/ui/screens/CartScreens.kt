package com.sandalia.stockanchor.ui.screens

import com.sandalia.stockanchor.util.plural
import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.str
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.sandalia.stockanchor.data.local.ProductWithStock
import com.sandalia.stockanchor.data.repo.CartLine
import com.sandalia.stockanchor.domain.Perm
import com.sandalia.stockanchor.ui.components.AppCard
import com.sandalia.stockanchor.ui.components.DropdownField
import com.sandalia.stockanchor.ui.components.EmptyState
import com.sandalia.stockanchor.ui.components.KeyValue
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.LocalOpenPaywall
import com.sandalia.stockanchor.ui.components.NumberField
import com.sandalia.stockanchor.ui.components.PremiumChip
import com.sandalia.stockanchor.ui.components.ProductPickerSheet
import com.sandalia.stockanchor.ui.components.ScreenScaffold
import com.sandalia.stockanchor.ui.components.rememberRunner
import com.sandalia.stockanchor.ui.navigation.Routes
import com.sandalia.stockanchor.ui.theme.Amber
import com.sandalia.stockanchor.util.BarcodeScanner
import com.sandalia.stockanchor.util.Fmt
import kotlinx.coroutines.launch

/** Cart state survives tab switches and rotation. */
class CartViewModel : ViewModel() {
    val lines = mutableStateListOf<CartLine>()
    var warehouseId by mutableStateOf<Long?>(null)
    var customer by mutableStateOf("")
    var phone by mutableStateOf("")
    var discount by mutableStateOf("")
    var note by mutableStateOf("")
    var supplierId by mutableStateOf<Long?>(null)
    var updateCost by mutableStateOf(true)

    fun add(p: ProductWithStock, purchase: Boolean) {
        val i = lines.indexOfFirst { it.productId == p.id }
        if (i >= 0) lines[i] = lines[i].copy(quantity = lines[i].quantity + 1)
        else lines.add(CartLine(p.id, p.name, p.unit, 1.0, if (purchase) p.costPrice else p.salePrice, p.costPrice, p.quantity))
    }

    fun setQty(index: Int, q: Double) { if (q <= 0) lines.removeAt(index) else lines[index] = lines[index].copy(quantity = q) }
    fun setAmount(index: Int, a: Double) { lines[index] = lines[index].copy(unitAmount = a) }

    fun reset() {
        lines.clear(); customer = ""; phone = ""; discount = ""; note = ""; supplierId = null
    }
}

/* ============================================================ Sell (POS) */

@Composable
fun SellScreen(onDone: (Long) -> Unit, nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val warehouses by container.repo.warehouses().collectAsStateWithLifecycle(emptyList())
    val vm: CartViewModel = viewModel(key = "sell")
    val runner = rememberRunner()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val openPaywall = LocalOpenPaywall.current
    var picking by remember { mutableStateOf(false) }

    LaunchedEffect(warehouses) {
        if (vm.warehouseId == null || warehouses.none { it.id == vm.warehouseId }) vm.warehouseId = warehouses.firstOrNull { it.isDefault }?.id ?: warehouses.firstOrNull()?.id
    }

    val subtotal = vm.lines.sumOf { it.lineTotal }
    val discount = (vm.discount.toDoubleOrNull() ?: 0.0).coerceIn(0.0, subtotal)
    val total = subtotal - discount

    ScreenScaffold(
        str(R.string.new_sale), busy = runner.busy,
        actions = { IconButton(onClick = { nav.navigate(Routes.SALES) }) { Icon(Icons.Filled.History, str(R.string.sales)) } },
        bottomBar = {
            if (vm.lines.isNotEmpty()) Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(str(R.string.label_total), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Fmt.money(session.currency, total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val wh = vm.warehouseId ?: return@Button
                            runner.run(str(R.string.sale_completed)) {
                                val id = container.repo.sale(vm.customer, vm.phone, wh, discount, vm.note, vm.lines.toList())
                                vm.reset()
                                com.sandalia.stockanchor.work.LowStockNotifier.check(context)
                                onDone(id)
                            }
                        },
                        enabled = !runner.busy && vm.customer.isNotBlank() && session.can(Perm.SALES_CREATE),
                        modifier = Modifier.height(52.dp),
                    ) { Text(str(R.string.complete_sale)) }
                }
            }
        },
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize().imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    OutlinedTextField(vm.customer, { vm.customer = it }, label = { Text(str(R.string.customer_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(vm.phone, { vm.phone = it }, label = { Text(str(R.string.phone)) }, singleLine = true, modifier = Modifier.weight(1f))
                        DropdownField(str(R.string.sell_from), warehouses, warehouses.firstOrNull { it.id == vm.warehouseId }, { it.name },
                            { it?.let { w -> vm.warehouseId = w.id } }, Modifier.weight(1f))
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { picking = true }, modifier = Modifier.weight(1f).height(48.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Icon(Icons.Filled.AddShoppingCart, null); Spacer(Modifier.width(8.dp))
                        Text(str(R.string.add_product), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = {
                            if (!session.premiumActive) openPaywall() else scope.launch {
                                val code = BarcodeScanner.scan(context) ?: return@launch
                                val p = container.repo.findByCode(code)
                                val here = p?.let { found -> vm.warehouseId?.let { container.repo.stockOf(found.id, it) } } ?: 0.0
                                // Same rule as the picker: assigned to the selected warehouse, or stocked there.
                                if (p == null) runner.toast(str(R.string.no_product_with_barcode_x, code))
                                else if (p.warehouseId != vm.warehouseId && here <= 0) {
                                    val wName = warehouses.firstOrNull { it.id == vm.warehouseId }?.name ?: str(R.string.this_warehouse)
                                    runner.toast(str(R.string.x_isn_t_available_in_x, p.name, wName))
                                } else {
                                    vm.add(ProductWithStock(p.id, p.name, p.sku, p.barcode, p.categoryId, null, p.warehouseId, null, p.unit, p.costPrice, p.salePrice, p.lowStockThreshold, here), purchase = false)
                                }
                            }
                        },
                        modifier = Modifier.height(48.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Icon(Icons.Filled.QrCodeScanner, null)
                        if (!session.premiumActive) { Spacer(Modifier.width(6.dp)); PremiumChip() }
                    }
                }
            }
            if (vm.lines.isEmpty()) item {
                EmptyState(Icons.Filled.AddShoppingCart, str(R.string.cart_is_empty), str(R.string.add_products_to_start_a_sale))
            }
            itemsIndexed(vm.lines, key = { _, l -> l.productId }) { i, line ->
                CartLineCard(line, session.currency, str(R.string.price), onQty = { vm.setQty(i, it) }, onAmount = { vm.setAmount(i, it) },
                    warnStock = line.quantity > line.available)
            }
            if (vm.lines.isNotEmpty()) item {
                AppCard(Modifier.fillMaxWidth()) {
                    NumberField(vm.discount, { vm.discount = it }, str(R.string.discount), Modifier.fillMaxWidth(), prefix = session.currency)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(vm.note, { vm.note = it }, label = { Text(str(R.string.note_optional)) }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    KeyValue(str(R.string.subtotal), Fmt.money(session.currency, subtotal))
                    if (discount > 0) KeyValue(str(R.string.discount), "-" + Fmt.money(session.currency, discount))
                    KeyValue(str(R.string.total), Fmt.money(session.currency, total), strong = true)
                    val profit = total - vm.lines.sumOf { it.quantity * it.unitCost }
                    KeyValue(str(R.string.estimated_profit), Fmt.money(session.currency, profit),
                        valueColor = if (profit >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (picking) ProductPickerSheet(vm.warehouseId, showCost = false, onlyWarehouse = true, onPick = { vm.add(it, purchase = false); picking = false }, onDismiss = { picking = false })
}

@Composable
private fun CartLineCard(
    line: CartLine, currency: String, amountLabel: String, onQty: (Double) -> Unit, onAmount: (Double) -> Unit, warnStock: Boolean = false,
) {
    var amountText by remember(line.productId) { mutableStateOf(Fmt.qty(line.unitAmount).replace(",", "")) }
    var qtyText by remember(line.productId, line.quantity) { mutableStateOf(Fmt.qty(line.quantity).replace(",", "")) }
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(line.name, style = MaterialTheme.typography.titleSmall)
                if (warnStock) Text(str(R.string.only_x_x_in_this_warehouse, Fmt.qty(line.available), line.unit),
                    style = MaterialTheme.typography.bodySmall, color = Amber)
            }
            IconButton(onClick = { onQty(0.0) }) { Icon(Icons.Filled.Close, str(R.string.remove)) }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilledTonalIconButton(onClick = { onQty(line.quantity - 1) }, modifier = Modifier.size(38.dp)) { Icon(Icons.Filled.Remove, str(R.string.less)) }
            OutlinedTextField(
                qtyText, { v -> if (v.isEmpty() || v.matches(Regex("^\\d*\\.?\\d*$"))) { qtyText = v; v.toDoubleOrNull()?.let { if (it > 0) onQty(it) } } },
                singleLine = true, modifier = Modifier.width(78.dp), shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
            )
            FilledTonalIconButton(onClick = { onQty(line.quantity + 1) }, modifier = Modifier.size(38.dp)) { Icon(Icons.Filled.Add, str(R.string.more)) }
            NumberField(amountText, { amountText = it; onAmount(it.toDoubleOrNull() ?: 0.0) }, amountLabel, Modifier.weight(1f), prefix = currency)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "${Fmt.qty(line.quantity)} ${line.unit} × ${Fmt.money(currency, line.unitAmount)} = ${Fmt.money(currency, line.lineTotal)}",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/* ============================================================ Purchase (add stock) */

@Composable
fun PurchaseScreen(onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val warehouses by container.repo.warehouses().collectAsStateWithLifecycle(emptyList())
    val suppliers by container.repo.suppliers().collectAsStateWithLifecycle(emptyList())
    val vm: CartViewModel = viewModel(key = "purchase")
    val runner = rememberRunner()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val openPaywall = LocalOpenPaywall.current
    var picking by remember { mutableStateOf(false) }

    LaunchedEffect(warehouses) {
        if (vm.warehouseId == null || warehouses.none { it.id == vm.warehouseId }) vm.warehouseId = warehouses.firstOrNull { it.isDefault }?.id ?: warehouses.firstOrNull()?.id
    }
    val total = vm.lines.sumOf { it.lineTotal }

    ScreenScaffold(
        str(R.string.add_stock), subtitle = str(R.string.record_a_purchase), onBack = onBack, busy = runner.busy,
        bottomBar = {
            if (vm.lines.isNotEmpty()) Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(str(R.string.purchase_total), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Fmt.money(session.currency, total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val wh = vm.warehouseId ?: return@Button
                            runner.run(str(R.string.stock_added), onDone = onBack) {
                                container.repo.purchase(wh, vm.supplierId, vm.note, vm.lines.toList(), vm.updateCost)
                                vm.reset()
                            }
                        },
                        enabled = !runner.busy && session.can(Perm.PURCHASES_CREATE),
                        modifier = Modifier.height(52.dp),
                    ) { Text(str(R.string.save_purchase)) }
                }
            }
        },
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize().imePadding(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    DropdownField(str(R.string.receive_into_warehouse), warehouses, warehouses.firstOrNull { it.id == vm.warehouseId }, { it.name },
                        { it?.let { w -> vm.warehouseId = w.id } }, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    if (session.premiumActive) {
                        DropdownField(str(R.string.supplier), suppliers, suppliers.firstOrNull { it.id == vm.supplierId }, { it.name },
                            { vm.supplierId = it?.id }, Modifier.fillMaxWidth(), noneLabel = str(R.string.no_supplier))
                    } else {
                        OutlinedButton(onClick = openPaywall, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.LocalShipping, null); Spacer(Modifier.width(8.dp)); Text(str(R.string.link_a_supplier)); Spacer(Modifier.width(8.dp)); PremiumChip()
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(vm.note, { vm.note = it }, label = { Text(str(R.string.reference_note)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(vm.updateCost, { vm.updateCost = it })
                        Text(str(R.string.update_product_cost_price_to_this), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { picking = true }, modifier = Modifier.weight(1f).height(48.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp))
                        Text(str(R.string.add_product), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(onClick = {
                        if (!session.premiumActive) openPaywall() else scope.launch {
                            val code = BarcodeScanner.scan(context) ?: return@launch
                            val p = container.repo.findByCode(code)
                            if (p == null) runner.toast(str(R.string.no_product_with_barcode_x, code))
                            else vm.add(ProductWithStock(p.id, p.name, p.sku, p.barcode, p.categoryId, null, p.warehouseId, null, p.unit, p.costPrice, p.salePrice, p.lowStockThreshold, 0.0), purchase = true)
                        }
                    }, modifier = Modifier.height(48.dp)) { Icon(Icons.Filled.QrCodeScanner, str(R.string.scan)) }
                }
            }
            if (vm.lines.isEmpty()) item {
                EmptyState(Icons.Filled.LocalShipping, str(R.string.no_items_yet), str(R.string.add_the_products_you_received_stock))
            }
            itemsIndexed(vm.lines, key = { _, l -> l.productId }) { i, line ->
                CartLineCard(line, session.currency, str(R.string.unit_cost), onQty = { vm.setQty(i, it) }, onAmount = { vm.setAmount(i, it) })
            }
        }
    }

    if (picking) ProductPickerSheet(vm.warehouseId, showCost = true, onPick = { vm.add(it, purchase = true); picking = false }, onDismiss = { picking = false })
}

/* ============================================================ Purchase history */

@Composable
fun PurchasesScreen(nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val purchases by container.repo.recentPurchases().collectAsStateWithLifecycle(emptyList())

    ScreenScaffold(
        str(R.string.purchases), onBack = { nav.popBackStack() },
        floatingActionButton = {
            if (session.can(Perm.PURCHASES_CREATE)) androidx.compose.material3.ExtendedFloatingActionButton(onClick = { nav.navigate(Routes.PURCHASE) },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text(str(R.string.add_stock)) })
        },
    ) { pad ->
        if (purchases.isEmpty()) {
            EmptyState(Icons.Filled.LocalShipping, str(R.string.no_purchases_yet), str(R.string.every_time_you_add_stock_it), Modifier.padding(pad))
            return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(purchases, key = { it.id }) { p ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.refNo, style = MaterialTheme.typography.titleSmall)
                            Text(listOfNotNull(p.supplierName, p.warehouseName, plural(R.plurals.n_items, p.itemCount, p.itemCount)).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Fmt.dateTime(p.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(Fmt.money(session.currency, p.total), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
