package com.sandalia.stockanchor.ui.screens

import com.sandalia.stockanchor.util.plural
import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.str
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandalia.stockanchor.data.local.SupplierEntity
import com.sandalia.stockanchor.data.local.WarehouseEntity
import com.sandalia.stockanchor.domain.Limits
import com.sandalia.stockanchor.domain.Perm
import com.sandalia.stockanchor.ui.components.AppCard
import com.sandalia.stockanchor.ui.components.ConfirmDialog
import com.sandalia.stockanchor.ui.components.EmptyState
import com.sandalia.stockanchor.ui.components.InitialAvatar
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.LocalOpenPaywall
import com.sandalia.stockanchor.ui.components.LockedFeature
import com.sandalia.stockanchor.ui.components.PremiumChip
import com.sandalia.stockanchor.ui.components.PhoneField
import com.sandalia.stockanchor.ui.components.ScreenScaffold
import com.sandalia.stockanchor.ui.components.rememberRunner
import com.sandalia.stockanchor.ui.theme.Amber

/* ============================================================ Categories */

@Composable
fun CategoriesScreen(onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val categories by container.repo.categoriesWithCount().collectAsStateWithLifecycle(emptyList())
    val runner = rememberRunner()
    var editing by remember { mutableStateOf<Triple<Long?, String, String>?>(null) }
    var deleting by remember { mutableStateOf<Pair<Long, String>?>(null) }
    val canEdit = session.can(Perm.CATALOG_MANAGE)

    ScreenScaffold(
        str(R.string.categories), onBack = onBack, busy = runner.busy,
        floatingActionButton = {
            if (canEdit) ExtendedFloatingActionButton(onClick = { editing = Triple(null, "", "") },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text(str(R.string.category)) })
        },
    ) { pad ->
        if (categories.isEmpty()) {
            EmptyState(Icons.Filled.Category, str(R.string.no_categories), str(R.string.group_products_e_g_electronics_groceries), Modifier.padding(pad))
            return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories, key = { it.id }) { c ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InitialAvatar(c.name, MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, style = MaterialTheme.typography.titleSmall)
                            Text(plural(R.plurals.n_products, c.productCount, c.productCount) + (c.description?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        if (canEdit) {
                            IconButton(onClick = { editing = Triple(c.id, c.name, c.description.orEmpty()) }) { Icon(Icons.Filled.Edit, str(R.string.edit)) }
                            IconButton(onClick = { deleting = c.id to c.name }) { Icon(Icons.Filled.Delete, str(R.string.delete)) }
                        }
                    }
                }
            }
        }
    }

    editing?.let { (id, n, d) ->
        var name by remember(id) { mutableStateOf(n) }
        var desc by remember(id) { mutableStateOf(d) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (id == null) str(R.string.new_category) else str(R.string.edit_category)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text(str(R.string.name)) }, singleLine = true)
                    OutlinedTextField(desc, { desc = it }, label = { Text(str(R.string.description)) })
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    editing = null
                    runner.run(str(R.string.category_saved)) { container.repo.saveCategory(id, name, desc) }
                }) { Text(str(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(str(R.string.cancel)) } },
        )
    }
    deleting?.let { (id, name) ->
        ConfirmDialog(str(R.string.delete_x, name), str(R.string.products_in_this_category_will_become),
            onConfirm = { runner.run(str(R.string.category_deleted)) { container.repo.deleteCategory(id) } }, onDismiss = { deleting = null })
    }
}

/* ============================================================ Warehouses */

@Composable
fun WarehousesScreen(onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val warehouses by container.repo.warehouses().collectAsStateWithLifecycle(emptyList())
    val runner = rememberRunner()
    val openPaywall = LocalOpenPaywall.current
    var editing by remember { mutableStateOf<WarehouseEntity?>(null) }
    var deleting by remember { mutableStateOf<WarehouseEntity?>(null) }
    val canEdit = session.can(Perm.WAREHOUSES_MANAGE)
    val atLimit = !session.premiumActive && warehouses.size >= Limits.BASIC_WAREHOUSES

    ScreenScaffold(
        str(R.string.warehouses), onBack = onBack, busy = runner.busy,
        subtitle = if (session.premiumActive) str(R.string.unlimited_on_premium) else str(R.string.x_of_x_on_basic, warehouses.size, Limits.warehouses()),
        floatingActionButton = {
            if (canEdit) ExtendedFloatingActionButton(
                onClick = { if (atLimit) openPaywall() else editing = WarehouseEntity(id = 0, name = "") },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text(str(R.string.warehouse)) },
            )
        },
    ) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (atLimit) item {
                AppCard(Modifier.fillMaxWidth(), onClick = openPaywall) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(str(R.string.need_more_locations_premium_has_unlimited), Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(8.dp)); PremiumChip()
                    }
                }
            }
            items(warehouses, key = { it.id }) { w ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InitialAvatar(w.name)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(w.name, style = MaterialTheme.typography.titleSmall)
                                if (w.isDefault) { Spacer(Modifier.width(6.dp)); Icon(Icons.Filled.Star, str(R.string.default_label), tint = Amber, modifier = Modifier.padding(0.dp)) }
                            }
                            Text(w.location ?: str(R.string.no_address), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (canEdit) {
                            IconButton(onClick = { editing = w }) { Icon(Icons.Filled.Edit, str(R.string.edit)) }
                            IconButton(onClick = { deleting = w }) { Icon(Icons.Filled.Delete, str(R.string.delete)) }
                        }
                    }
                }
            }
        }
    }

    editing?.let { w ->
        var name by remember(w.id) { mutableStateOf(w.name) }
        var location by remember(w.id) { mutableStateOf(w.location.orEmpty()) }
        var isDefault by remember(w.id) { mutableStateOf(w.isDefault) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (w.id == 0L) str(R.string.new_warehouse) else str(R.string.edit_warehouse)) },
            icon = { Icon(Icons.Filled.Warehouse, null) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text(str(R.string.name)) }, singleLine = true)
                    OutlinedTextField(location, { location = it }, label = { Text(str(R.string.location_address)) })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(isDefault, { isDefault = it }); Text(str(R.string.default_for_sales_purchases))
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    editing = null
                    runner.run(str(R.string.warehouse_saved)) { container.repo.saveWarehouse(w.id.takeIf { it != 0L }, name, location, isDefault) }
                }) { Text(str(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(str(R.string.cancel)) } },
        )
    }
    deleting?.let { w ->
        ConfirmDialog(str(R.string.delete_x, w.name), str(R.string.only_empty_warehouses_can_be_deleted),
            onConfirm = { runner.run(str(R.string.warehouse_deleted)) { container.repo.deleteWarehouse(w.id) } }, onDismiss = { deleting = null })
    }
}

/* ============================================================ Suppliers (Premium) */

@Composable
fun SuppliersScreen(onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val suppliers by container.repo.suppliers().collectAsStateWithLifecycle(emptyList())
    val runner = rememberRunner()
    var editing by remember { mutableStateOf<SupplierEntity?>(null) }
    var deleting by remember { mutableStateOf<SupplierEntity?>(null) }
    val canEdit = session.can(Perm.SUPPLIERS_MANAGE)

    ScreenScaffold(
        str(R.string.suppliers), onBack = onBack, busy = runner.busy,
        floatingActionButton = {
            if (session.premiumActive && canEdit) ExtendedFloatingActionButton(onClick = { editing = SupplierEntity(name = "") },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text(str(R.string.supplier)) })
        },
    ) { pad ->
        if (!session.premiumActive) {
            LockedFeature(str(R.string.supplier_management), str(R.string.keep_your_suppliers_contacts_and_link), Modifier.padding(pad))
            return@ScreenScaffold
        }
        if (suppliers.isEmpty()) {
            EmptyState(Icons.Filled.LocalShipping, str(R.string.no_suppliers_yet), str(R.string.add_the_businesses_you_buy_stock), Modifier.padding(pad))
            return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suppliers, key = { it.id }) { s ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InitialAvatar(s.name, MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.name, style = MaterialTheme.typography.titleSmall)
                            Text(listOfNotNull(s.phone, s.email).joinToString(" · ").ifBlank { str(R.string.no_contact_details) },
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            s.address?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        if (canEdit) {
                            IconButton(onClick = { editing = s }) { Icon(Icons.Filled.Edit, str(R.string.edit)) }
                            IconButton(onClick = { deleting = s }) { Icon(Icons.Filled.Delete, str(R.string.delete)) }
                        }
                    }
                }
            }
        }
    }

    editing?.let { s ->
        var name by remember(s.id) { mutableStateOf(s.name) }
        var phone by remember(s.id) { mutableStateOf(s.phone.orEmpty()) }
        var email by remember(s.id) { mutableStateOf(s.email.orEmpty()) }
        var address by remember(s.id) { mutableStateOf(s.address.orEmpty()) }
        var note by remember(s.id) { mutableStateOf(s.note.orEmpty()) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (s.id == 0L) str(R.string.new_supplier) else str(R.string.edit_supplier)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text(str(R.string.name_2)) }, singleLine = true)
                    PhoneField(phone, { phone = it })
                    OutlinedTextField(email, { email = it }, label = { Text(str(R.string.email)) }, singleLine = true)
                    OutlinedTextField(address, { address = it }, label = { Text(str(R.string.address)) })
                    OutlinedTextField(note, { note = it }, label = { Text(str(R.string.note)) })
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    editing = null
                    runner.run(str(R.string.supplier_saved)) { container.repo.saveSupplier(s.id.takeIf { it != 0L }, name, phone, email, address, note) }
                }) { Text(str(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(str(R.string.cancel)) } },
        )
    }
    deleting?.let { s ->
        ConfirmDialog(str(R.string.delete_x, s.name), str(R.string.past_purchases_keep_their_records),
            onConfirm = { runner.run(str(R.string.supplier_deleted)) { container.repo.deleteSupplier(s.id) } }, onDismiss = { deleting = null })
    }
}
