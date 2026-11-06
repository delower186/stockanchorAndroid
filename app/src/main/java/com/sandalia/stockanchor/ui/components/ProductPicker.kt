package com.sandalia.stockanchor.ui.components

import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.str
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
    supplierId: Long? = null,
    onPick: (ProductWithStock) -> Unit,
    onDismiss: () -> Unit,
) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val found by remember(query, warehouseId, onlyWarehouse, supplierId) {
        container.db.catalog().products(query.trim(), null, if (onlyWarehouse) warehouseId else null, supplierId)
    }
        .collectAsStateWithLifecycle(emptyList())
    // A product's home warehouse doesn't mean it is there: selling offers only what this warehouse actually holds.
    val products = if (onlyWarehouse) found.filter { it.quantity > 0 } else found
    val stockHere by remember(warehouseId) { container.db.catalog().products("", null, warehouseId, null) }
        .collectAsStateWithLifecycle(emptyList())
    val qtyHere = remember(stockHere) { stockHere.associate { it.id to it.quantity } }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val runner = rememberRunner()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(Modifier.fillMaxHeight(0.85f).padding(horizontal = 16.dp)) {
            Text(str(R.string.choose_product), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.padding(4.dp))
            SearchField(query, { query = it }, str(R.string.name_sku_or_barcode), trailing = {
                if (session.premiumActive) IconButton(onClick = {
                    scope.launch {
                        val code = BarcodeScanner.scan(context) ?: return@launch
                        val found = container.repo.findByCode(code)
                        if (found == null) runner.toast(str(R.string.no_product_with_barcode_x, code)) else query = code
                    }
                }) { Icon(Icons.Filled.QrCodeScanner, str(R.string.scan_barcode)) }
            })
            if (products.isEmpty()) Text(
                when {
                    query.isNotBlank() -> str(R.string.no_matches_2)
                    onlyWarehouse -> str(R.string.no_products_in_this_warehouse_yet)
                    supplierId != null -> str(R.string.no_products_from_this_supplier)
                    else -> str(R.string.no_matches_2)
                },
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
                                (if (showCost) str(R.string.cost_x, Fmt.money(session.currency, p.costPrice)) else Fmt.money(session.currency, p.salePrice)) +
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
