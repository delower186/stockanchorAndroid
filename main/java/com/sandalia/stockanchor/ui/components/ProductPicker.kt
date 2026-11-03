package com.sandalia.stockanchor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandalia.stockanchor.data.local.ProductWithStock
import com.sandalia.stockanchor.util.BarcodeScanner
import com.sandalia.stockanchor.util.Fmt
import kotlinx.coroutines.launch

/**
 * Bottom sheet to choose a product. Stock shown is for [warehouseId] (or all warehouses).
 * With [onlyWarehouse] the list is limited to products assigned to, or stocked in, [warehouseId].
 * Premium users get a scan button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPickerSheet(
    warehouseId: Long?,
    showCost: Boolean,
    onlyWarehouse: Boolean = false,
    onPick: (ProductWithStock) -> Unit,
    onDismiss: () -> Unit,
) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val products by remember(query, warehouseId, onlyWarehouse) {
        container.db.catalog().products(query.trim(), null, if (onlyWarehouse) warehouseId else null, null)
    }
        .collectAsStateWithLifecycle(emptyList())
    val stockHere by remember(warehouseId) { container.db.catalog().products("", null, warehouseId, null) }
        .collectAsStateWithLifecycle(emptyList())
    val qtyHere = remember(stockHere) { stockHere.associate { it.id to it.quantity } }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val runner = rememberRunner()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(Modifier.fillMaxHeight(0.85f).padding(horizontal = 16.dp)) {
            Text("Choose product", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.padding(4.dp))
            SearchField(query, { query = it }, "Name, SKU or barcode", trailing = {
                if (session.premiumActive) IconButton(onClick = {
                    scope.launch {
                        val code = BarcodeScanner.scan(context) ?: return@launch
                        val found = container.repo.findByCode(code)
                        if (found == null) runner.toast("No product with barcode $code") else query = code
                    }
                }) { Icon(Icons.Filled.QrCodeScanner, "Scan barcode") }
            })
            if (products.isEmpty()) Text(
                if (onlyWarehouse && query.isBlank()) "No products in this warehouse yet." else "No matches.",
                Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(Modifier.fillMaxWidth()) {
                items(products, key = { it.id }) { p ->
                    val here = if (warehouseId == null) p.quantity else (qtyHere[p.id] ?: 0.0)
                    Row(
                        Modifier.fillMaxWidth().clickable { onPick(p.copy(quantity = here)) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        InitialAvatar(p.name, size = 38)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Medium)
                            Text(
                                (if (showCost) "Cost " + Fmt.money(session.currency, p.costPrice) else Fmt.money(session.currency, p.salePrice)) +
                                    (p.sku?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        StockPill(here, p.lowStockThreshold, p.unit)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
