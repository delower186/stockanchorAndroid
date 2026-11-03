package com.sandalia.stockanchor.ui.screens

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
        "Categories", onBack = onBack, busy = runner.busy,
        floatingActionButton = {
            if (canEdit) ExtendedFloatingActionButton(onClick = { editing = Triple(null, "", "") },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Category") })
        },
    ) { pad ->
        if (categories.isEmpty()) {
            EmptyState(Icons.Filled.Category, "No categories", "Group products (e.g. Electronics, Groceries) to filter them quickly.", Modifier.padding(pad))
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
                            Text("${c.productCount} product${if (c.productCount == 1) "" else "s"}" + (c.description?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        if (canEdit) {
                            IconButton(onClick = { editing = Triple(c.id, c.name, c.description.orEmpty()) }) { Icon(Icons.Filled.Edit, "Edit") }
                            IconButton(onClick = { deleting = c.id to c.name }) { Icon(Icons.Filled.Delete, "Delete") }
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
            title = { Text(if (id == null) "New category" else "Edit category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(desc, { desc = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    editing = null
                    runner.run("Category saved") { container.repo.saveCategory(id, name, desc) }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
        )
    }
    deleting?.let { (id, name) ->
        ConfirmDialog("Delete “$name”?", "Products in this category will become uncategorised.",
            onConfirm = { runner.run("Category deleted") { container.repo.deleteCategory(id) } }, onDismiss = { deleting = null })
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
        "Warehouses", onBack = onBack, busy = runner.busy,
        subtitle = if (session.premiumActive) "Unlimited on Premium" else "${warehouses.size} of ${Limits.warehouses()} on Basic",
        floatingActionButton = {
            if (canEdit) ExtendedFloatingActionButton(
                onClick = { if (atLimit) openPaywall() else editing = WarehouseEntity(id = 0, name = "") },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Warehouse") },
            )
        },
    ) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (atLimit) item {
                AppCard(Modifier.fillMaxWidth(), onClick = openPaywall) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Need more locations? Premium has unlimited warehouses and stock transfers.", Modifier.weight(1f),
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
                                if (w.isDefault) { Spacer(Modifier.width(6.dp)); Icon(Icons.Filled.Star, "Default", tint = Amber, modifier = Modifier.padding(0.dp)) }
                            }
                            Text(w.location ?: "No address", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (canEdit) {
                            IconButton(onClick = { editing = w }) { Icon(Icons.Filled.Edit, "Edit") }
                            IconButton(onClick = { deleting = w }) { Icon(Icons.Filled.Delete, "Delete") }
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
            title = { Text(if (w.id == 0L) "New warehouse" else "Edit warehouse") },
            icon = { Icon(Icons.Filled.Warehouse, null) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(location, { location = it }, label = { Text("Location / address") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(isDefault, { isDefault = it }); Text("Default for sales & purchases")
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    editing = null
                    runner.run("Warehouse saved") { container.repo.saveWarehouse(w.id.takeIf { it != 0L }, name, location, isDefault) }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
        )
    }
    deleting?.let { w ->
        ConfirmDialog("Delete “${w.name}”?", "Only empty warehouses can be deleted.",
            onConfirm = { runner.run("Warehouse deleted") { container.repo.deleteWarehouse(w.id) } }, onDismiss = { deleting = null })
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
        "Suppliers", onBack = onBack, busy = runner.busy,
        floatingActionButton = {
            if (session.premiumActive && canEdit) ExtendedFloatingActionButton(onClick = { editing = SupplierEntity(name = "") },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Supplier") })
        },
    ) { pad ->
        if (!session.premiumActive) {
            LockedFeature("Supplier management", "Keep your suppliers' contacts and link them to every purchase.", Modifier.padding(pad))
            return@ScreenScaffold
        }
        if (suppliers.isEmpty()) {
            EmptyState(Icons.Filled.LocalShipping, "No suppliers yet", "Add the businesses you buy stock from.", Modifier.padding(pad))
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
                            Text(listOfNotNull(s.phone, s.email).joinToString(" · ").ifBlank { "No contact details" },
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            s.address?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        if (canEdit) {
                            IconButton(onClick = { editing = s }) { Icon(Icons.Filled.Edit, "Edit") }
                            IconButton(onClick = { deleting = s }) { Icon(Icons.Filled.Delete, "Delete") }
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
            title = { Text(if (s.id == 0L) "New supplier" else "Edit supplier") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Name *") }, singleLine = true)
                    OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, singleLine = true)
                    OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(address, { address = it }, label = { Text("Address") })
                    OutlinedTextField(note, { note = it }, label = { Text("Note") })
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    editing = null
                    runner.run("Supplier saved") { container.repo.saveSupplier(s.id.takeIf { it != 0L }, name, phone, email, address, note) }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
        )
    }
    deleting?.let { s ->
        ConfirmDialog("Delete “${s.name}”?", "Past purchases keep their records.",
            onConfirm = { runner.run("Supplier deleted") { container.repo.deleteSupplier(s.id) } }, onDismiss = { deleting = null })
    }
}
