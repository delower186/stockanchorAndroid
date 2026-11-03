package com.sandalia.stockanchor.ui.screens

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

    ScreenScaffold("Stock transfer", subtitle = "Move stock between warehouses", onBack = onBack, busy = runner.busy) { pad ->
        if (!session.premiumActive) {
            LockedFeature("Stock transfers", "Move products between your warehouses with a full audit trail.", Modifier.padding(pad)); return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Icon(Icons.Filled.Inventory2, null); Spacer(Modifier.width(8.dp)); Text(product?.name ?: "Choose product")
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        DropdownField("From", sources, sources.firstOrNull { it.warehouseId == fromId },
                            { "${it.warehouseName} (${Fmt.qty(it.quantity)})" }, { fromId = it?.warehouseId }, Modifier.weight(1f),
                            enabled = sources.isNotEmpty())
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, null, tint = MaterialTheme.colorScheme.secondary)
                        val destinations = warehouses.filter { it.id != fromId }
                        DropdownField("To", destinations, destinations.firstOrNull { it.id == toId }, { it.name }, { toId = it?.id }, Modifier.weight(1f),
                            enabled = fromId != null)
                    }
                    if (product != null && sources.isEmpty()) {
                        Text("This product has no stock in any warehouse.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                    }
                    available?.let {
                        Text("Available in source: ${Fmt.qty(it)} ${product?.unit.orEmpty()}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    NumberField(qty, { qty = it }, "Quantity", Modifier.fillMaxWidth(), suffix = product?.unit)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(note, { note = it }, label = { Text("Note") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val p = product ?: return@Button; val f = fromId ?: return@Button; val t = toId ?: return@Button
                            runner.run("Transferred ${qty} ${p.unit} of ${p.name}") {
                                container.repo.transfer(p.id, f, t, qty.toDoubleOrNull() ?: 0.0, note)
                                qty = ""; note = ""
                            }
                        },
                        enabled = product != null && fromId != null && toId != null && (qty.toDoubleOrNull() ?: 0.0) > 0 &&
                            (qty.toDoubleOrNull() ?: 0.0) <= (available ?: 0.0) &&
                            session.can(Perm.TRANSFERS_CREATE) && !runner.busy,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    ) { Text("Transfer stock") }
                }
            }
            item { SectionTitle("Recent transfers") }
            if (transfers.isEmpty()) item { Text("No transfers yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
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

    ScreenScaffold("Stock movements", subtitle = if (productId != null) rows.firstOrNull()?.productName else "All products", onBack = onBack) { pad ->
        if (!session.premiumActive) {
            LockedFeature("Stock movement history", "See every purchase, sale, transfer and adjustment for each product.", Modifier.padding(pad)); return@ScreenScaffold
        }
        Column(Modifier.padding(pad).fillMaxSize()) {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(type == null, onClick = { type = null }, label = { Text("All") })
                movementTypes.forEach { t -> FilterChip(type == t, onClick = { type = t }, label = { Text(Fmt.movementLabel(t)) }) }
            }
            DropdownField("Warehouse", warehouses, warehouses.firstOrNull { it.id == warehouseId }, { it.name }, { warehouseId = it?.id },
                Modifier.fillMaxWidth().padding(16.dp, 8.dp), noneLabel = "All warehouses")
            if (rows.isEmpty()) {
                EmptyState(Icons.Filled.History, "No movements", "Stock changes will be listed here.")
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
                                Text("bal ${Fmt.qty(m.balanceAfter)} ${m.unit}", style = MaterialTheme.typography.labelSmall,
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

    ScreenScaffold("Low stock", subtitle = "${low.size} product(s) at or below minimum", onBack = { nav.popBackStack() }) { pad ->
        if (!session.premiumActive) {
            LockedFeature("Low stock alerts", "Get notified when products drop below their minimum level.", Modifier.padding(pad)); return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NotificationsActive, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Notifications", style = MaterialTheme.typography.titleSmall)
                            Text("Checked twice a day and after each sale", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(session.lowStockAlerts, { container.session.setLowStockAlerts(it) })
                    }
                }
            }
            if (low.isEmpty()) item { EmptyState(Icons.Filled.Inventory2, "All good", "No product is below its minimum level.") }
            items(low, key = { it.id }) { p ->
                AppCard(Modifier.fillMaxWidth(), onClick = { nav.navigate(Routes.product(p.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleSmall)
                            Text("Minimum ${Fmt.qty(p.lowStockThreshold)} ${p.unit}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StockPill(p.quantity, p.lowStockThreshold, p.unit)
                        if (session.can(Perm.PURCHASES_CREATE)) IconButton(onClick = { nav.navigate(Routes.PURCHASE) }) { Icon(Icons.Filled.AddBox, "Add stock") }
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
            runner.toast("Exported $n ${what.title.lowercase()} rows")
        }
        exporting = null
    }
    val openDoc = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runner.run { preview = withContext(Dispatchers.IO) { Csv.read(context, uri) } }
    }
    val saveTemplate = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        if (uri != null) runner.run("Template saved") {
            withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.use { it.write(Csv.IMPORT_TEMPLATE.toByteArray()) } }
        }
    }

    ScreenScaffold("Import & export", subtitle = "CSV files for Excel / Google Sheets", onBack = onBack, busy = runner.busy) { pad ->
        if (!session.premiumActive) {
            LockedFeature("CSV export & import", "Export products, stock, sales and movements, or bulk-load stock from a spreadsheet.", Modifier.padding(pad))
            return@ScreenScaffold
        }
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Export")
            AppCard(Modifier.fillMaxWidth()) {
                CsvExport.entries.forEach { e ->
                    MenuRow(Icons.Filled.Download, e.title, "Save as CSV") {
                        if (!session.can(Perm.DATA_EXPORT)) { runner.toast("You don't have permission to export"); return@MenuRow }
                        exporting = e
                        val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                        createDoc.launch("stock-anchor-${e.fileName}-$stamp.csv")
                    }
                }
            }
            SectionTitle("Import stock")
            AppCard(Modifier.fillMaxWidth()) {
                Text(
                    "Columns: name, sku, barcode, category, warehouse, quantity, unit_cost, sale_price. Products are matched by SKU, " +
                        "then barcode, then name; missing products and categories are created. Quantities are added to stock.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { saveTemplate.launch("stock-anchor-import-template.csv") }, modifier = Modifier.weight(1f)) { Text("Template") }
                    Button(
                        onClick = { openDoc.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "application/vnd.ms-excel")) },
                        enabled = session.can(Perm.DATA_IMPORT), modifier = Modifier.weight(1f),
                    ) { Icon(Icons.Filled.FileUpload, null); Spacer(Modifier.width(6.dp)); Text("Choose CSV") }
                }
            }
        }
    }

    preview?.let { rows ->
        AlertDialog(
            onDismissRequest = { preview = null },
            title = { Text("Import ${rows.size} rows?") },
            text = {
                Column(Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    rows.take(8).forEach { r ->
                        Text("${r["name"] ?: r["sku"] ?: r["barcode"]} · qty ${r["quantity"] ?: "0"} · ${r["warehouse"].orEmpty().ifBlank { "default warehouse" }}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (rows.size > 8) Text("…and ${rows.size - 8} more", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(enabled = rows.isNotEmpty(), onClick = {
                    preview = null
                    runner.run {
                        val r = container.repo.importStock(rows)
                        result = buildString {
                            append("Imported ${r.imported ?: 0} rows, created ${r.createdProducts ?: 0} new products.")
                            r.errors?.takeIf { it.isNotEmpty() }?.let { append("\n\nSkipped:\n" + it.take(15).joinToString("\n")) }
                        }
                    }
                }) { Text("Import") }
            },
            dismissButton = { TextButton(onClick = { preview = null }) { Text("Cancel") } },
        )
    }
    result?.let {
        AlertDialog(onDismissRequest = { result = null }, title = { Text("Import finished") }, text = { Text(it) },
            confirmButton = { TextButton(onClick = { result = null }) { Text("OK") } })
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
        "Team", subtitle = "Accounts & permissions", onBack = onBack, busy = runner.busy,
        floatingActionButton = {
            if (session.premiumActive && canManage) ExtendedFloatingActionButton(onClick = { editing = UserDto(id = 0, role = "staff") },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Member") })
        },
    ) { pad ->
        if (!session.premiumActive) {
            LockedFeature("Team accounts", "Invite staff with their own login, role and permissions.", Modifier.padding(pad)); return@ScreenScaffold
        }
        if (members.isEmpty() && !runner.busy) {
            EmptyState(Icons.Filled.Group, "Just you so far", "Add team members to share this inventory.", Modifier.padding(pad)); return@ScreenScaffold
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
                            IconButton(onClick = { editing = m }) { Icon(Icons.Filled.Edit, "Edit") }
                            IconButton(onClick = { removing = m }) { Icon(Icons.Filled.Delete, "Remove") }
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
            title = { Text(if (isNew) "Add team member" else "Edit ${m.name}") },
            text = {
                Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                    if (isNew) OutlinedTextField(email, { email = it }, label = { Text("Email (login)") }, singleLine = true)
                    OutlinedTextField(password, { password = it }, label = { Text(if (isNew) "Password (min 8)" else "New password (optional)") }, singleLine = true)
                    DropdownField("Role", listOf("admin", "manager", "staff"), role, { it.replaceFirstChar { c -> c.uppercase() } }, {
                        if (it != null) { role = it; perms = roles[it].orEmpty().toSet() }
                    }, Modifier.fillMaxWidth())
                    if (!isNew) Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Account active", Modifier.weight(1f)); Switch(active, { active = it })
                    }
                    Text("Permissions", style = MaterialTheme.typography.titleSmall)
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
                    runner.run(if (isNew) "Member added" else "Member updated") {
                        container.api.call { if (isNew) createMember(body) else updateMember(m.id, body) }
                        reload++
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
        )
    }
    removing?.let { m ->
        ConfirmDialog("Remove ${m.name}?", "They will be signed out on all devices and lose access to this inventory.", confirmLabel = "Remove",
            onConfirm = { runner.run("Member removed") { container.api.call { deleteMember(m.id) }; reload++ } }, onDismiss = { removing = null })
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

    ScreenScaffold("Cloud backup", subtitle = "Automatic nightly · last 30 kept", onBack = onBack, busy = runner.busy) { pad ->
        if (!session.premiumActive) {
            LockedFeature("Cloud backup & recovery", "Nightly automatic backups of your whole inventory, plus one-tap restore.", Modifier.padding(pad))
            return@ScreenScaffold
        }
        if (!session.can(Perm.BACKUPS_MANAGE)) {
            EmptyState(Icons.Filled.Backup, "No access", "Ask your team owner for the backups permission.", Modifier.padding(pad)); return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Text("Back up now", style = MaterialTheme.typography.titleMedium)
                    Text("A snapshot of every product, stock level, sale, purchase and movement.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(note, { note = it }, label = { Text("Label (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        runner.run("Backup created") { container.api.call { createBackup(BackupRequest(note.ifBlank { null })) }; note = ""; reload++ }
                    }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text("Create backup") }
                }
            }
            item { SectionTitle("Backups") }
            if (backups.isEmpty()) item { Text("No backups yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(backups, key = { it.id }) { b ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (b.isAuto == true) Icons.Filled.History else Icons.Filled.Backup, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(Fmt.dateTime(Dates.parse(b.createdAt)), style = MaterialTheme.typography.titleSmall)
                            Text(
                                listOfNotNull(b.note, "${b.counts?.get("products") ?: 0} products", "${b.counts?.get("sales") ?: 0} sales",
                                    "${(b.size ?: 0) / 1024} KB").joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { restoring = b }) { Icon(Icons.Filled.Restore, "Restore") }
                        IconButton(onClick = { deleting = b }) { Icon(Icons.Filled.Delete, "Delete") }
                    }
                }
            }
        }
    }

    restoring?.let { b ->
        ConfirmDialog(
            "Restore this backup?",
            "Your team's current data will be replaced with the snapshot from ${Fmt.dateTime(Dates.parse(b.createdAt))}. " +
                "A safety backup of the current state is made first. All devices re-sync.",
            confirmLabel = "Restore",
            onConfirm = {
                runner.run("Backup restored") {
                    container.api.call { restoreBackup(b.id) }
                    container.sync.pull(full = true)
                    reload++
                }
            },
            onDismiss = { restoring = null },
        )
    }
    deleting?.let { b ->
        ConfirmDialog("Delete backup?", "This snapshot will be permanently removed.",
            onConfirm = { runner.run("Backup deleted") { container.api.call { deleteBackup(b.id) }; reload++ } }, onDismiss = { deleting = null })
    }
}
