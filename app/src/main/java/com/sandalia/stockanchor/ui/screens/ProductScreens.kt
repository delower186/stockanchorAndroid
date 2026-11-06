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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.sandalia.stockanchor.data.local.CategoryEntity
import com.sandalia.stockanchor.data.local.ProductEntity
import com.sandalia.stockanchor.data.local.StockRow
import com.sandalia.stockanchor.data.local.SupplierEntity
import com.sandalia.stockanchor.data.local.WarehouseEntity
import com.sandalia.stockanchor.domain.Perm
import com.sandalia.stockanchor.ui.components.AppCard
import com.sandalia.stockanchor.ui.components.ConfirmDialog
import com.sandalia.stockanchor.ui.components.DropdownField
import com.sandalia.stockanchor.ui.components.EmptyState
import com.sandalia.stockanchor.ui.components.InitialAvatar
import com.sandalia.stockanchor.ui.components.KeyValue
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.LocalOpenPaywall
import com.sandalia.stockanchor.ui.components.NumberField
import com.sandalia.stockanchor.ui.components.PremiumChip
import com.sandalia.stockanchor.ui.components.PhoneField
import com.sandalia.stockanchor.ui.components.ScreenScaffold
import com.sandalia.stockanchor.ui.components.SearchField
import com.sandalia.stockanchor.ui.components.SectionTitle
import com.sandalia.stockanchor.ui.components.StockPill
import com.sandalia.stockanchor.ui.components.rememberRunner
import com.sandalia.stockanchor.ui.navigation.Routes
import com.sandalia.stockanchor.util.BarcodeScanner
import com.sandalia.stockanchor.util.Fmt
import kotlinx.coroutines.launch

/* ============================================================ Product list */

@Composable
fun ProductsScreen(nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var warehouseId by rememberSaveable { mutableStateOf<Long?>(null) }
    var supplierId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showFilters by rememberSaveable { mutableStateOf(false) }

    val categories by container.repo.categories().collectAsStateWithLifecycle(emptyList())
    val warehouses by container.repo.warehouses().collectAsStateWithLifecycle(emptyList())
    val suppliers by container.repo.suppliers().collectAsStateWithLifecycle(emptyList())
    // Suppliers are Premium: the filter only applies (and shows) while Premium is active.
    val supplierFilter = supplierId.takeIf { session.premiumActive }
    val products by remember(query, categoryId, warehouseId, supplierFilter) { container.repo.products(query, categoryId, warehouseId, supplierFilter) }
        .collectAsStateWithLifecycle(emptyList())
    val filtered = categoryId != null || warehouseId != null || supplierFilter != null
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val runner = rememberRunner()
    val openPaywall = LocalOpenPaywall.current

    ScreenScaffold(
        title = str(R.string.products),
        subtitle = plural(R.plurals.n_items, products.size, products.size),
        actions = {
            IconButton(onClick = { nav.navigate(Routes.CATEGORIES) }) { Icon(Icons.Filled.Tune, str(R.string.categories)) }
        },
        floatingActionButton = {
            if (session.can(Perm.CATALOG_MANAGE)) ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.productEdit()) },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text(str(R.string.product)) },
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                SearchField(query, { query = it }, str(R.string.search_name_sku_or_barcode), trailing = {
                    IconButton(onClick = {
                        if (!session.premiumActive) openPaywall() else scope.launch {
                            val code = BarcodeScanner.scan(context) ?: return@launch
                            val p = container.repo.findByCode(code)
                            if (p != null) nav.navigate(Routes.product(p.id)) else { query = code; runner.toast(str(R.string.no_product_with_barcode_x, code)) }
                        }
                    }) { Icon(Icons.Filled.QrCodeScanner, str(R.string.scan)) }
                })
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = filtered,
                        onClick = { showFilters = !showFilters },
                        label = { Text(if (filtered) str(R.string.filtered) else str(R.string.filter)) },
                        leadingIcon = { Icon(Icons.Filled.Tune, null) },
                    )
                    categoryId?.let { id ->
                        FilterChip(true, onClick = { categoryId = null }, label = { Text(categories.firstOrNull { it.id == id }?.name ?: str(R.string.category)) })
                    }
                    warehouseId?.let { id ->
                        FilterChip(true, onClick = { warehouseId = null }, label = { Text(warehouses.firstOrNull { it.id == id }?.name ?: str(R.string.warehouse)) })
                    }
                    supplierFilter?.let { id ->
                        FilterChip(true, onClick = { supplierId = null }, label = { Text(suppliers.firstOrNull { it.id == id }?.name ?: str(R.string.supplier)) })
                    }
                }
                if (showFilters) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownField(str(R.string.category), categories, categories.firstOrNull { it.id == categoryId }, { it.name },
                            { categoryId = it?.id }, Modifier.weight(1f), noneLabel = str(R.string.all_categories))
                        DropdownField(str(R.string.warehouse), warehouses, warehouses.firstOrNull { it.id == warehouseId }, { it.name },
                            { warehouseId = it?.id }, Modifier.weight(1f), noneLabel = str(R.string.all_warehouses))
                    }
                    if (session.premiumActive) {
                        Spacer(Modifier.height(8.dp))
                        DropdownField(str(R.string.supplier), suppliers, suppliers.firstOrNull { it.id == supplierId }, { it.name },
                            { supplierId = it?.id }, Modifier.fillMaxWidth(), noneLabel = str(R.string.all_suppliers))
                    }
                }
            }
            if (products.isEmpty()) {
                EmptyState(Icons.Filled.Inventory2, if (query.isBlank() && !filtered) str(R.string.no_products_yet) else str(R.string.no_matches),
                    str(R.string.add_your_first_product_then_record))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(products, key = { it.id }) { p ->
                        AppCard(Modifier.fillMaxWidth(), onClick = { nav.navigate(Routes.product(p.id)) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                InitialAvatar(p.name)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(p.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        // Filtered: the chosen warehouse (quantity is for it). Otherwise where the stock is, else the home warehouse.
                                        listOfNotNull(
                                            p.categoryName ?: str(R.string.uncategorised),
                                            warehouseId?.let { id -> warehouses.firstOrNull { it.id == id }?.name } ?: p.stockLocations ?: p.warehouseName,
                                            p.sku,
                                        ).joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(Fmt.money(session.currency, p.salePrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                                StockPill(p.quantity, p.lowStockThreshold, p.unit)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================ Create / edit */

@Composable
fun ProductEditScreen(productId: Long, onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val categories by container.repo.categories().collectAsStateWithLifecycle(emptyList())
    val warehouses by container.repo.warehouses().collectAsStateWithLifecycle(emptyList())
    val suppliers by container.repo.suppliers().collectAsStateWithLifecycle(emptyList())
    val runner = rememberRunner()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val openPaywall = LocalOpenPaywall.current

    var loaded by remember { mutableStateOf(productId == 0L) }
    var name by rememberSaveable { mutableStateOf("") }
    var sku by rememberSaveable { mutableStateOf("") }
    var barcode by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var warehouseId by rememberSaveable { mutableStateOf<Long?>(null) }
    var supplierId by rememberSaveable { mutableStateOf<Long?>(null) }
    var unit by rememberSaveable { mutableStateOf("pcs") }
    var cost by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var threshold by rememberSaveable { mutableStateOf("5") }
    var description by rememberSaveable { mutableStateOf("") }
    var createdAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var confirmDelete by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf<String?>(null) }
    var newSupplier by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        if (productId != 0L && !loaded) {
            container.repo.productOnce(productId)?.let { p ->
                name = p.name; sku = p.sku.orEmpty(); barcode = p.barcode.orEmpty(); categoryId = p.categoryId; warehouseId = p.warehouseId; supplierId = p.supplierId; unit = p.unit
                cost = Fmt.qty(p.costPrice).replace(",", ""); price = Fmt.qty(p.salePrice).replace(",", "")
                threshold = Fmt.qty(p.lowStockThreshold).replace(",", ""); description = p.description.orEmpty(); createdAt = p.createdAt
            }
            loaded = true
        }
        if (productId == 0L && warehouseId == null) warehouseId = container.repo.defaultWarehouseId()
    }

    ScreenScaffold(
        title = if (productId == 0L) str(R.string.new_product) else str(R.string.edit_product),
        onBack = onBack,
        busy = runner.busy,
        actions = {
            if (productId != 0L) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, str(R.string.delete)) }
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(name, { name = it }, label = { Text(str(R.string.product_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                DropdownField(str(R.string.category), categories, categories.firstOrNull { it.id == categoryId }, CategoryEntity::name,
                    { categoryId = it?.id }, Modifier.weight(1f), noneLabel = str(R.string.no_category))
                TextButton(onClick = { newCategory = "" }) { Text(str(R.string.new_label)) }
            }
            DropdownField(str(R.string.warehouse), warehouses, warehouses.firstOrNull { it.id == warehouseId }, WarehouseEntity::name,
                { warehouseId = it?.id }, Modifier.fillMaxWidth(), noneLabel = str(R.string.no_warehouse))
            if (session.premiumActive) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                DropdownField(str(R.string.supplier), suppliers, suppliers.firstOrNull { it.id == supplierId }, SupplierEntity::name,
                    { supplierId = it?.id }, Modifier.weight(1f), noneLabel = str(R.string.no_supplier))
                if (session.can(Perm.SUPPLIERS_MANAGE)) TextButton(onClick = { newSupplier = "" }) { Text(str(R.string.new_label)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(sku, { sku = it }, label = { Text("SKU") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(unit, { unit = it }, label = { Text(str(R.string.unit)) }, singleLine = true, modifier = Modifier.weight(0.6f))
            }
            OutlinedTextField(
                barcode, { barcode = it }, label = { Text(str(R.string.barcode)) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (!session.premiumActive) openPaywall()
                        else scope.launch { BarcodeScanner.scan(context)?.let { barcode = it } }
                    }) { Icon(Icons.Filled.QrCodeScanner, str(R.string.scan_barcode)) }
                },
                supportingText = { if (!session.premiumActive) Row { Text(str(R.string.scanning_is_a_premium_feature)); Spacer(Modifier.width(6.dp)); PremiumChip() } },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(cost, { cost = it }, str(R.string.cost_price), Modifier.weight(1f), prefix = session.currency)
                NumberField(price, { price = it }, str(R.string.sale_price), Modifier.weight(1f), prefix = session.currency)
            }
            val c = cost.toDoubleOrNull() ?: 0.0
            val pr = price.toDoubleOrNull() ?: 0.0
            if (pr > 0) {
                val margin = (pr - c) / pr * 100
                Text(
                    str(R.string.margin_x_x_per_x, "%.1f".format(margin), Fmt.money(session.currency, pr - c), unit.ifBlank { "unit" }),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (pr >= c) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                )
            }
            NumberField(threshold, { threshold = it }, str(R.string.low_stock_alert_at), Modifier.fillMaxWidth(), suffix = unit)
            OutlinedTextField(description, { description = it }, label = { Text(str(R.string.description)) }, minLines = 3, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    runner.run(success = str(R.string.product_saved), onDone = onBack) {
                        container.repo.saveProduct(
                            ProductEntity(
                                id = productId, name = name, sku = sku, barcode = barcode, categoryId = categoryId, warehouseId = warehouseId, supplierId = supplierId,
                                unit = unit.ifBlank { "pcs" },
                                costPrice = c, salePrice = pr, lowStockThreshold = threshold.toDoubleOrNull() ?: 0.0,
                                description = description, createdAt = createdAt,
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && !runner.busy && session.can(Perm.CATALOG_MANAGE),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(str(R.string.save_product)) }
            if (productId == 0L) Text(
                str(R.string.tip_new_products_start_with_zero),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (confirmDelete) ConfirmDialog(
        str(R.string.delete_product), str(R.string.x_will_be_removed_with_its, name),
        onConfirm = { runner.run(str(R.string.product_deleted), onDone = onBack) { container.repo.deleteProduct(productId) } },
        onDismiss = { confirmDelete = false },
    )

    newCategory?.let { text ->
        AlertDialog(
            onDismissRequest = { newCategory = null },
            title = { Text(str(R.string.new_category)) },
            text = { OutlinedTextField(text, { newCategory = it }, label = { Text(str(R.string.name)) }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    val n = text
                    newCategory = null
                    runner.run(str(R.string.category_added)) {
                        container.repo.saveCategory(null, n, null)
                        // pick the new category
                        container.db.catalog().allCategories().firstOrNull { it.name.equals(n.trim(), true) }?.let { categoryId = it.id }
                    }
                }, enabled = text.isNotBlank()) { Text(str(R.string.add)) }
            },
            dismissButton = { TextButton(onClick = { newCategory = null }) { Text(str(R.string.cancel)) } },
        )
    }

    newSupplier?.let { text ->
        var phone by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { newSupplier = null },
            title = { Text(str(R.string.new_supplier)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(text, { newSupplier = it }, label = { Text(str(R.string.name_2)) }, singleLine = true)
                    PhoneField(phone, { phone = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val n = text
                    val ph = phone
                    newSupplier = null
                    runner.run(str(R.string.supplier_added)) {
                        container.repo.saveSupplier(null, n, ph, null, null, null)
                        // pick the new supplier
                        container.db.catalog().allSuppliers().filter { it.name.equals(n.trim(), true) }.maxByOrNull { it.id }?.let { supplierId = it.id }
                    }
                }, enabled = text.isNotBlank()) { Text(str(R.string.add)) }
            },
            dismissButton = { TextButton(onClick = { newSupplier = null }) { Text(str(R.string.cancel)) } },
        )
    }
}

/* ============================================================ Detail */

@Composable
fun ProductDetailScreen(productId: Long, nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val product by remember(productId) { container.repo.product(productId) }.collectAsStateWithLifecycle(null)
    val stock by remember(productId) { container.repo.stockForProduct(productId) }.collectAsStateWithLifecycle(emptyList())
    val warehouses by container.repo.warehouses().collectAsStateWithLifecycle(emptyList())
    val categories by container.repo.categories().collectAsStateWithLifecycle(emptyList())
    val suppliers by container.repo.suppliers().collectAsStateWithLifecycle(emptyList())
    val runner = rememberRunner()
    val openPaywall = LocalOpenPaywall.current
    var adjusting by remember { mutableStateOf<WarehouseEntity?>(null) }

    val p = product
    ScreenScaffold(
        title = p?.name ?: str(R.string.product),
        onBack = { nav.popBackStack() },
        busy = runner.busy,
        actions = {
            if (session.can(Perm.CATALOG_MANAGE)) IconButton(onClick = { nav.navigate(Routes.productEdit(productId)) }) { Icon(Icons.Filled.Edit, str(R.string.edit)) }
        },
    ) { pad ->
        if (p == null) return@ScreenScaffold
        val total = stock.sumOf { it.quantity }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InitialAvatar(p.name, size = 52)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleLarge)
                            Text(categories.firstOrNull { it.id == p.categoryId }?.name ?: str(R.string.uncategorised),
                                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        StockPill(total, p.lowStockThreshold, p.unit)
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    KeyValue(str(R.string.sale_price), Fmt.money(session.currency, p.salePrice), strong = true)
                    KeyValue(str(R.string.cost_price), Fmt.money(session.currency, p.costPrice))
                    KeyValue(str(R.string.stock_value_cost), Fmt.money(session.currency, total * p.costPrice))
                    KeyValue(str(R.string.warehouse), warehouses.firstOrNull { it.id == p.warehouseId }?.name ?: "—")
                    if (session.premiumActive) KeyValue(str(R.string.supplier), suppliers.firstOrNull { it.id == p.supplierId }?.name ?: "—")
                    KeyValue("SKU", p.sku ?: "—")
                    KeyValue(str(R.string.barcode), p.barcode ?: "—")
                    KeyValue(str(R.string.low_stock_alert_at), "${Fmt.qty(p.lowStockThreshold)} ${p.unit}")
                    p.description?.let { Spacer(Modifier.height(6.dp)); Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (session.can(Perm.PURCHASES_CREATE)) DetailAction(str(R.string.add_stock), Icons.Filled.AddBox, Modifier.weight(1f)) { nav.navigate(Routes.PURCHASE) }
                    if (session.can(Perm.TRANSFERS_CREATE)) DetailAction(str(R.string.transfer), Icons.AutoMirrored.Filled.CompareArrows, Modifier.weight(1f)) {
                        if (session.premiumActive) nav.navigate(Routes.TRANSFERS) else openPaywall()
                    }
                    DetailAction(str(R.string.history), Icons.Filled.History, Modifier.weight(1f)) {
                        if (session.premiumActive) nav.navigate(Routes.movements(productId)) else openPaywall()
                    }
                }
            }
            item { SectionTitle(str(R.string.stock_by_warehouse)) }
            items(warehouses, key = { it.id }) { w ->
                val row: StockRow? = stock.firstOrNull { it.warehouseId == w.id }
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(w.name, style = MaterialTheme.typography.titleSmall)
                            w.location?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        StockPill(row?.quantity ?: 0.0, p.lowStockThreshold, p.unit)
                        if (session.can(Perm.STOCK_ADJUST)) TextButton(onClick = { adjusting = w }) { Text(str(R.string.count)) }
                    }
                }
            }
        }
    }

    adjusting?.let { w ->
        var value by remember { mutableStateOf(Fmt.qty(stock.firstOrNull { it.warehouseId == w.id }?.quantity ?: 0.0).replace(",", "")) }
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { adjusting = null },
            title = { Text(str(R.string.stock_count_x, w.name)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(str(R.string.enter_the_quantity_you_physically_counted),
                        style = MaterialTheme.typography.bodySmall)
                    NumberField(value, { value = it }, str(R.string.counted_quantity), Modifier.fillMaxWidth(), suffix = p?.unit)
                    OutlinedTextField(note, { note = it }, label = { Text(str(R.string.reason_optional)) }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = value.toDoubleOrNull() ?: return@TextButton
                    adjusting = null
                    runner.run(str(R.string.stock_updated)) { container.repo.adjust(productId, w.id, target, note) }
                }) { Text(str(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { adjusting = null }) { Text(str(R.string.cancel)) } },
        )
    }
}

/** Icon-over-label button so three fit side by side on narrow screens without wrapping. */
@Composable
private fun DetailAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(64.dp), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
        }
    }
}
