package com.sandalia.stockanchor.ui.screens

import com.sandalia.stockanchor.util.plural
import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.str
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.sandalia.stockanchor.data.local.ProductWithStock
import com.sandalia.stockanchor.data.remote.BackupDto
import com.sandalia.stockanchor.data.remote.BackupRequest
import com.sandalia.stockanchor.data.remote.MemberRequest
import com.sandalia.stockanchor.data.remote.UserDto
import com.sandalia.stockanchor.domain.Perm
import com.sandalia.stockanchor.ui.components.AppCard
import com.sandalia.stockanchor.ui.components.ConfirmDialog
import com.sandalia.stockanchor.ui.components.DropdownField
import com.sandalia.stockanchor.ui.components.EmptyState
import com.sandalia.stockanchor.ui.components.InitialAvatar
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.LockedFeature
import com.sandalia.stockanchor.ui.components.MenuRow
import com.sandalia.stockanchor.ui.components.NumberField
import com.sandalia.stockanchor.ui.components.ProductPickerSheet
import com.sandalia.stockanchor.ui.components.ScreenScaffold
import com.sandalia.stockanchor.ui.components.SectionTitle
import com.sandalia.stockanchor.ui.components.StockPill
import com.sandalia.stockanchor.ui.components.rememberRunner
import com.sandalia.stockanchor.ui.navigation.Routes
import com.sandalia.stockanchor.ui.theme.Coral
import com.sandalia.stockanchor.ui.theme.Positive
import com.sandalia.stockanchor.util.Csv
import com.sandalia.stockanchor.util.CsvExport
import com.sandalia.stockanchor.util.Dates
import com.sandalia.stockanchor.util.Fmt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ============================================================ Transfers */

@Composable
fun TransfersScreen(onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val warehouses by container.repo.warehouses().collectAsStateWithLifecycle(emptyList())
    val transfers by container.repo.recentTransfers().collectAsStateWithLifecycle(emptyList())
    val runner = rememberRunner()
    var product by remember { mutableStateOf<ProductWithStock?>(null) }
    var fromId by rememberSaveable { mutableStateOf<Long?>(null) }
    var toId by rememberSaveable { mutableStateOf<Long?>(null) }
    var qty by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var picking by remember { mutableStateOf(false) }

    // "From" offers only the warehouses that actually hold the chosen product.
    val productStock by remember(product?.id) {
        product?.let { container.repo.stockForProduct(it.id) } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())
    val sources = productStock.filter { it.quantity > 0 }
    val available = sources.firstOrNull { it.warehouseId == fromId }?.quantity

    LaunchedEffect(sources) {
        if (sources.none { it.warehouseId == fromId }) fromId = sources.firstOrNull()?.warehouseId
    }
    LaunchedEffect(fromId) {
        if (toId == fromId) toId = null
    }

    ScreenScaffold(str(R.string.stock_transfer), subtitle = str(R.string.move_stock_between_warehouses), onBack = onBack, busy = runner.busy) { pad ->
        if (!session.premiumActive) {
            LockedFeature(str(R.string.stock_transfers), str(R.string.move_products_between_your_warehouses_with), Modifier.padding(pad)); return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Icon(Icons.Filled.Inventory2, null); Spacer(Modifier.width(8.dp)); Text(product?.name ?: str(R.string.choose_product))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        DropdownField(str(R.string.from), sources, sources.firstOrNull { it.warehouseId == fromId },
                            { "${it.warehouseName} (${Fmt.qty(it.quantity)})" }, { fromId = it?.warehouseId }, Modifier.weight(1f),
                            enabled = sources.isNotEmpty())
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, null, tint = MaterialTheme.colorScheme.secondary)
                        val destinations = warehouses.filter { it.id != fromId }
                        DropdownField(str(R.string.to), destinations, destinations.firstOrNull { it.id == toId }, { it.name }, { toId = it?.id }, Modifier.weight(1f),
                            enabled = fromId != null)
                    }
                    if (product != null && sources.isEmpty()) {
                        Text(str(R.string.this_product_has_no_stock_in), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                    }
                    available?.let {
                        Text(str(R.string.available_in_source_x_x, Fmt.qty(it), product?.unit.orEmpty()), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    NumberField(qty, { qty = it }, str(R.string.quantity), Modifier.fillMaxWidth(), suffix = product?.unit)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(note, { note = it }, label = { Text(str(R.string.note)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val p = product ?: return@Button; val f = fromId ?: return@Button; val t = toId ?: return@Button
                            runner.run(str(R.string.transferred_x_x_of_x, qty, p.unit, p.name)) {
                                container.repo.transfer(p.id, f, t, qty.toDoubleOrNull() ?: 0.0, note)
                                qty = ""; note = ""
                            }
                        },
                        enabled = product != null && fromId != null && toId != null && (qty.toDoubleOrNull() ?: 0.0) > 0 &&
                            (qty.toDoubleOrNull() ?: 0.0) <= (available ?: 0.0) &&
                            session.can(Perm.TRANSFERS_CREATE) && !runner.busy,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    ) { Text(str(R.string.transfer_stock)) }
                }
            }
            item { SectionTitle(str(R.string.recent_transfers)) }
            if (transfers.isEmpty()) item { Text(str(R.string.no_transfers_yet), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(transfers, key = { it.id }) { t ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.productName, style = MaterialTheme.typography.titleSmall)
                            Text("${t.fromName} → ${t.toName}", style = MaterialTheme.typography.bodySmall)
                            Text(Fmt.dateTime(t.date) + (t.note?.let { " · $it" } ?: ""), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${Fmt.qty(t.quantity)} ${t.unit}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (picking) ProductPickerSheet(fromId, showCost = false, onPick = { product = it; picking = false }, onDismiss = { picking = false })
}

/* ============================================================ Movement history */

private val movementTypes = listOf("purchase", "sale", "sale_cancel", "transfer_in", "transfer_out", "adjustment", "import")

@Composable
fun MovementsScreen(productId: Long?, onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val warehouses by container.repo.warehouses().collectAsStateWithLifecycle(emptyList())
    var type by rememberSaveable { mutableStateOf<String?>(null) }
    var warehouseId by rememberSaveable { mutableStateOf<Long?>(null) }
    val rows by remember(productId, warehouseId, type) { container.repo.movements(productId, warehouseId, type) }.collectAsStateWithLifecycle(emptyList())

    ScreenScaffold(str(R.string.stock_movements), subtitle = if (productId != null) rows.firstOrNull()?.productName else str(R.string.all_products), onBack = onBack) { pad ->
        if (!session.premiumActive) {
            LockedFeature(str(R.string.stock_movement_history), str(R.string.see_every_purchase_sale_transfer_and), Modifier.padding(pad)); return@ScreenScaffold
        }
        Column(Modifier.padding(pad).fillMaxSize()) {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(type == null, onClick = { type = null }, label = { Text(str(R.string.all)) })
                movementTypes.forEach { t -> FilterChip(type == t, onClick = { type = t }, label = { Text(Fmt.movementLabel(t)) }) }
            }
            DropdownField(str(R.string.warehouse), warehouses, warehouses.firstOrNull { it.id == warehouseId }, { it.name }, { warehouseId = it?.id },
                Modifier.fillMaxWidth().padding(16.dp, 8.dp), noneLabel = str(R.string.all_warehouses))
            if (rows.isEmpty()) {
                EmptyState(Icons.Filled.History, str(R.string.no_movements), str(R.string.stock_changes_will_be_listed_here))
            } else LazyColumn(contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(rows, key = { it.id }) { m ->
                    val inbound = m.quantity >= 0
                    AppCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(m.productName, style = MaterialTheme.typography.titleSmall)
                                Text("${Fmt.movementLabel(m.type)} · ${m.warehouseName}", style = MaterialTheme.typography.bodySmall)
                                Text(listOfNotNull(Fmt.dateTime(m.date), m.userName, m.note).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text((if (inbound) "+" else "") + Fmt.qty(m.quantity), color = if (inbound) Positive else Coral, fontWeight = FontWeight.Bold)
                                Text(str(R.string.balance_x_x, Fmt.qty(m.balanceAfter), m.unit), style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================ Low stock */

@Composable
fun LowStockScreen(nav: NavHostController) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val low by container.repo.lowStock().collectAsStateWithLifecycle(emptyList())

    ScreenScaffold(str(R.string.low_stock), subtitle = str(R.string.x_product_s_at_or_below, low.size), onBack = { nav.popBackStack() }) { pad ->
        if (!session.premiumActive) {
            LockedFeature(str(R.string.low_stock_channel), str(R.string.get_notified_when_products_drop_below), Modifier.padding(pad)); return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NotificationsActive, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(str(R.string.notifications), style = MaterialTheme.typography.titleSmall)
                            Text(str(R.string.checked_twice_a_day_and_after), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(session.lowStockAlerts, { container.session.setLowStockAlerts(it) })
                    }
                }
            }
            if (low.isEmpty()) item { EmptyState(Icons.Filled.Inventory2, str(R.string.all_good), str(R.string.no_product_is_below_its_minimum)) }
            items(low, key = { it.id }) { p ->
                AppCard(Modifier.fillMaxWidth(), onClick = { nav.navigate(Routes.product(p.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleSmall)
                            Text(str(R.string.minimum_x_x, Fmt.qty(p.lowStockThreshold), p.unit), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StockPill(p.quantity, p.lowStockThreshold, p.unit)
                        if (session.can(Perm.PURCHASES_CREATE)) IconButton(onClick = { nav.navigate(Routes.PURCHASE) }) { Icon(Icons.Filled.AddBox, str(R.string.add_stock)) }
                    }
                }
            }
        }
    }
}

/* ============================================================ CSV export / import */

@Composable
fun DataToolsScreen(onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val runner = rememberRunner()
    var exporting by remember { mutableStateOf<CsvExport?>(null) }
    var preview by remember { mutableStateOf<List<Map<String, String>>?>(null) }
    var result by remember { mutableStateOf<String?>(null) }

    val createDoc = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        val what = exporting ?: return@rememberLauncherForActivityResult
        if (uri != null) runner.run {
            val n = withContext(Dispatchers.IO) { Csv.export(context, container.db, what, uri) }
            runner.toast(str(R.string.exported_x_x_rows, n, what.title.lowercase()))
        }
        exporting = null
    }
    val openDoc = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runner.run { preview = withContext(Dispatchers.IO) { Csv.read(context, uri) } }
    }
    val saveTemplate = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        if (uri != null) runner.run(str(R.string.template_saved)) {
            withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.use { it.write(Csv.IMPORT_TEMPLATE.toByteArray()) } }
        }
    }

    ScreenScaffold(str(R.string.import_export), subtitle = str(R.string.csv_files_for_excel_google_sheets), onBack = onBack, busy = runner.busy) { pad ->
        if (!session.premiumActive) {
            LockedFeature(str(R.string.csv_export_import), str(R.string.export_products_stock_sales_and_movements), Modifier.padding(pad))
            return@ScreenScaffold
        }
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(str(R.string.export))
            AppCard(Modifier.fillMaxWidth()) {
                CsvExport.entries.forEach { e ->
                    MenuRow(Icons.Filled.Download, e.title, str(R.string.save_as_csv)) {
                        if (!session.can(Perm.DATA_EXPORT)) { runner.toast(str(R.string.you_don_t_have_permission_to)); return@MenuRow }
                        exporting = e
                        val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                        createDoc.launch("stock-anchor-${e.fileName}-$stamp.csv")
                    }
                }
            }
            SectionTitle(str(R.string.import_stock))
            AppCard(Modifier.fillMaxWidth()) {
                Text(
                    str(R.string.columns_name_sku_barcode_category_warehouse),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { saveTemplate.launch("stock-anchor-import-template.csv") }, modifier = Modifier.weight(1f)) { Text(str(R.string.template)) }
                    Button(
                        onClick = { openDoc.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "application/vnd.ms-excel")) },
                        enabled = session.can(Perm.DATA_IMPORT), modifier = Modifier.weight(1f),
                    ) { Icon(Icons.Filled.FileUpload, null); Spacer(Modifier.width(6.dp)); Text(str(R.string.choose_csv)) }
                }
            }
        }
    }

    preview?.let { rows ->
        AlertDialog(
            onDismissRequest = { preview = null },
            title = { Text(str(R.string.import_x_rows, rows.size)) },
            text = {
                Column(Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    rows.take(8).forEach { r ->
                        Text(str(R.string.x_qty_x_x, r["name"] ?: r["sku"] ?: r["barcode"], r["quantity"] ?: "0", r["warehouse"].orEmpty().ifBlank { str(R.string.default_warehouse_lower) }),
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (rows.size > 8) Text(str(R.string.and_x_more, rows.size - 8), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(enabled = rows.isNotEmpty(), onClick = {
                    preview = null
                    runner.run {
                        val r = container.repo.importStock(rows)
                        result = buildString {
                            append(str(R.string.imported_x_rows_created_x_new, r.imported ?: 0, r.createdProducts ?: 0))
                            r.errors?.takeIf { it.isNotEmpty() }?.let { append("\n\n" + str(R.string.skipped_rows) + "\n" + it.take(15).joinToString("\n")) }
                        }
                    }
                }) { Text(str(R.string.import_label)) }
            },
            dismissButton = { TextButton(onClick = { preview = null }) { Text(str(R.string.cancel)) } },
        )
    }
    result?.let {
        AlertDialog(onDismissRequest = { result = null }, title = { Text(str(R.string.import_finished)) }, text = { Text(it) },
            confirmButton = { TextButton(onClick = { result = null }) { Text(str(R.string.ok)) } })
    }
}

/* ============================================================ Team accounts & permissions */

@Composable
fun TeamScreen(onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val runner = rememberRunner()
    var members by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var roles by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var reload by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<UserDto?>(null) }
    var removing by remember { mutableStateOf<UserDto?>(null) }
    val canManage = session.can(Perm.TEAM_MANAGE)

    LaunchedEffect(reload, session.premiumActive) {
        if (session.premiumActive) runner.run {
            members = container.api.call { members() }
            if (roles.isEmpty()) roles = container.api.call { permissions() }.roles.orEmpty()
        }
    }

    ScreenScaffold(
        str(R.string.team), subtitle = str(R.string.accounts_permissions), onBack = onBack, busy = runner.busy,
        floatingActionButton = {
            if (session.premiumActive && canManage) ExtendedFloatingActionButton(onClick = { editing = UserDto(id = 0, role = "staff") },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text(str(R.string.member)) })
        },
    ) { pad ->
        if (!session.premiumActive) {
            LockedFeature(str(R.string.team_accounts), str(R.string.invite_staff_with_their_own_login), Modifier.padding(pad)); return@ScreenScaffold
        }
        if (members.isEmpty() && !runner.busy) {
            EmptyState(Icons.Filled.Group, str(R.string.just_you_so_far), str(R.string.add_team_members_to_share_this), Modifier.padding(pad)); return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(members, key = { it.id }) { m ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InitialAvatar(m.name.orEmpty())
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(m.name.orEmpty(), style = MaterialTheme.typography.titleSmall)
                            Text(m.email.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                RoleBadge(m.role.orEmpty())
                                if (m.isActive == false) RoleBadge("disabled", Coral)
                            }
                        }
                        if (canManage && m.role != "owner") {
                            IconButton(onClick = { editing = m }) { Icon(Icons.Filled.Edit, str(R.string.edit)) }
                            IconButton(onClick = { removing = m }) { Icon(Icons.Filled.Delete, str(R.string.remove)) }
                        }
                    }
                }
            }
        }
    }

    editing?.let { m ->
        val isNew = m.id == 0L
        var name by remember(m.id) { mutableStateOf(m.name.orEmpty()) }
        var email by remember(m.id) { mutableStateOf(m.email.orEmpty()) }
        var password by remember(m.id) { mutableStateOf("") }
        var role by remember(m.id) { mutableStateOf(m.role ?: "staff") }
        var active by remember(m.id) { mutableStateOf(m.isActive != false) }
        var perms by remember(m.id) { mutableStateOf((m.effectivePermissions ?: roles[role] ?: emptyList()).toSet()) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (isNew) str(R.string.add_team_member) else str(R.string.edit_x, m.name)) },
            text = {
                Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text(str(R.string.name)) }, singleLine = true)
                    if (isNew) OutlinedTextField(email, { email = it }, label = { Text(str(R.string.email_login)) }, singleLine = true)
                    OutlinedTextField(password, { password = it }, label = { Text(if (isNew) str(R.string.password_min_8) else str(R.string.new_password_optional)) }, singleLine = true)
                    DropdownField(str(R.string.role), listOf("admin", "manager", "staff"), role, { it.replaceFirstChar { c -> c.uppercase() } }, {
                        if (it != null) { role = it; perms = roles[it].orEmpty().toSet() }
                    }, Modifier.fillMaxWidth())
                    if (!isNew) Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(str(R.string.account_active), Modifier.weight(1f)); Switch(active, { active = it })
                    }
                    Text(str(R.string.permissions), style = MaterialTheme.typography.titleSmall)
                    Perm.ALL.forEach { p ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(p in perms, { checked -> perms = if (checked) perms + p else perms - p })
                            Text(Perm.label(p), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank() && (!isNew || (email.isNotBlank() && password.length >= 8)), onClick = {
                    editing = null
                    val body = MemberRequest(name, if (isNew) email else null, password.ifBlank { null }, role, perms.toList(), active)
                    runner.run(if (isNew) str(R.string.member_added) else str(R.string.member_updated)) {
                        container.api.call { if (isNew) createMember(body) else updateMember(m.id, body) }
                        reload++
                    }
                }) { Text(str(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(str(R.string.cancel)) } },
        )
    }
    removing?.let { m ->
        ConfirmDialog(str(R.string.remove_x, m.name), str(R.string.they_will_be_signed_out_on), confirmLabel = str(R.string.remove),
            onConfirm = { runner.run(str(R.string.member_removed)) { container.api.call { deleteMember(m.id) }; reload++ } }, onDismiss = { removing = null })
    }
}

@Composable
private fun RoleBadge(text: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondary) {
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.12f)) {
        Text(text.uppercase(), Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/* ============================================================ Cloud backup & recovery */

@Composable
fun BackupScreen(onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val runner = rememberRunner()
    var backups by remember { mutableStateOf<List<BackupDto>>(emptyList()) }
    var reload by remember { mutableIntStateOf(0) }
    var restoring by remember { mutableStateOf<BackupDto?>(null) }
    var deleting by remember { mutableStateOf<BackupDto?>(null) }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(reload, session.premiumActive) {
        if (session.premiumActive && session.can(Perm.BACKUPS_MANAGE)) runner.run { backups = container.api.call { backups() } }
    }

    ScreenScaffold(str(R.string.cloud_backup), subtitle = str(R.string.automatic_nightly_last_30_kept), onBack = onBack, busy = runner.busy) { pad ->
        if (!session.premiumActive) {
            LockedFeature(str(R.string.cloud_backup_recovery), str(R.string.nightly_automatic_backups_of_your_whole), Modifier.padding(pad))
            return@ScreenScaffold
        }
        if (!session.can(Perm.BACKUPS_MANAGE)) {
            EmptyState(Icons.Filled.Backup, str(R.string.no_access), str(R.string.ask_your_team_owner_for_the), Modifier.padding(pad)); return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Text(str(R.string.back_up_now), style = MaterialTheme.typography.titleMedium)
                    Text(str(R.string.a_snapshot_of_every_product_stock), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(note, { note = it }, label = { Text(str(R.string.label_optional)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        runner.run(str(R.string.backup_created)) { container.api.call { createBackup(BackupRequest(note.ifBlank { null })) }; note = ""; reload++ }
                    }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text(str(R.string.create_backup)) }
                }
            }
            item { SectionTitle(str(R.string.backups)) }
            if (backups.isEmpty()) item { Text(str(R.string.no_backups_yet), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(backups, key = { it.id }) { b ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (b.isAuto == true) Icons.Filled.History else Icons.Filled.Backup, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(Fmt.dateTime(Dates.parse(b.createdAt)), style = MaterialTheme.typography.titleSmall)
                            Text(
                                listOfNotNull(b.note, (b.counts?.get("products") ?: 0).let { plural(R.plurals.n_products, it, it) }, (b.counts?.get("sales") ?: 0).let { plural(R.plurals.n_sales, it, it) },
                                    "${(b.size ?: 0) / 1024} KB").joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { restoring = b }) { Icon(Icons.Filled.Restore, str(R.string.restore)) }
                        IconButton(onClick = { deleting = b }) { Icon(Icons.Filled.Delete, str(R.string.delete)) }
                    }
                }
            }
        }
    }

    restoring?.let { b ->
        ConfirmDialog(
            str(R.string.restore_this_backup),
            str(R.string.your_team_s_current_data_will, Fmt.dateTime(Dates.parse(b.createdAt))),
            confirmLabel = str(R.string.restore),
            onConfirm = {
                runner.run(str(R.string.backup_restored)) {
                    container.api.call { restoreBackup(b.id) }
                    container.sync.pull(full = true)
                    reload++
                }
            },
            onDismiss = { restoring = null },
        )
    }
    deleting?.let { b ->
        ConfirmDialog(str(R.string.delete_backup), str(R.string.this_snapshot_will_be_permanently_removed),
            onConfirm = { runner.run(str(R.string.backup_deleted)) { container.api.call { deleteBackup(b.id) }; reload++ } }, onDismiss = { deleting = null })
    }
}
