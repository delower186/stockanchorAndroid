package com.sandalia.stockanchor.data.repo

import androidx.room.withTransaction
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sandalia.stockanchor.data.local.AppDatabase
import com.sandalia.stockanchor.data.local.CategoryEntity
import com.sandalia.stockanchor.data.local.ExpenseEntity
import com.sandalia.stockanchor.data.local.MovementEntity
import com.sandalia.stockanchor.data.local.ProductEntity
import com.sandalia.stockanchor.data.local.PurchaseEntity
import com.sandalia.stockanchor.data.local.PurchaseItemEntity
import com.sandalia.stockanchor.data.local.SaleEntity
import com.sandalia.stockanchor.data.local.SaleItemEntity
import com.sandalia.stockanchor.data.local.SaleStatus
import com.sandalia.stockanchor.data.local.StockEntity
import com.sandalia.stockanchor.data.local.SupplierEntity
import com.sandalia.stockanchor.data.local.TransferEntity
import com.sandalia.stockanchor.data.local.WarehouseEntity
import com.sandalia.stockanchor.data.prefs.SessionStore
import com.sandalia.stockanchor.data.remote.ApiClient
import com.sandalia.stockanchor.util.Dates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps the Room cache in step with the cloud (Premium) and uploads the local database on upgrade.
 * The server is the source of truth: every cloud write is followed by an incremental pull.
 */
class SyncManager(
    private val db: AppDatabase,
    private val api: ApiClient,
    private val session: SessionStore,
) {
    private val mutex = Mutex()
    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    suspend fun pull(full: Boolean = false) = mutex.withLock {
        _syncing.value = true
        try {
            val since = if (full) null else session.current.lastSync
            val r = api.call { sync(since) }
            val catalog = db.catalog()
            val tx = db.tx()
            val m = db.maintenance()

            db.withTransaction {
                if (r.full == true) clearAll()

                r.categories?.let { list -> catalog.upsertCategories(list.map { CategoryEntity(it.id, it.name.orEmpty(), it.description) }) }
                r.warehouses?.let { list ->
                    catalog.upsertWarehouses(list.map { WarehouseEntity(it.id, it.name.orEmpty(), it.location, it.isDefault == true) })
                }
                r.suppliers?.let { list ->
                    catalog.upsertSuppliers(list.map { SupplierEntity(it.id, it.name.orEmpty(), it.phone, it.email, it.address, it.note) })
                }
                r.products?.let { list ->
                    // Guard the FKs: a product pointing at a warehouse/supplier we don't have is kept without one.
                    val warehouseIds = catalog.allWarehouses().mapTo(HashSet()) { it.id }
                    val supplierIds = catalog.allSuppliers().mapTo(HashSet()) { it.id }
                    catalog.upsertProducts(list.map {
                        ProductEntity(
                            id = it.id, name = it.name.orEmpty(), sku = it.sku, barcode = it.barcode, categoryId = it.categoryId,
                            warehouseId = it.warehouseId?.takeIf { id -> id in warehouseIds },
                            supplierId = it.supplierId?.takeIf { id -> id in supplierIds },
                            unit = it.unit ?: "pcs", costPrice = it.costPrice ?: 0.0, salePrice = it.salePrice ?: 0.0,
                            lowStockThreshold = it.lowStockThreshold ?: 0.0, description = it.description,
                            createdAt = Dates.parse(it.createdAt),
                        )
                    })
                }
                r.stocks?.let { list -> catalog.upsertStocks(list.map { StockEntity(it.productId, it.warehouseId, it.quantity ?: 0.0) }) }

                r.purchases?.takeIf { it.isNotEmpty() }?.let { list ->
                    tx.upsertPurchases(list.map {
                        PurchaseEntity(it.id, it.refNo.orEmpty(), it.supplierId, it.warehouseId, Dates.parse(it.date), it.total ?: 0.0, it.note)
                    })
                    tx.deletePurchaseItemsFor(list.map { it.id })
                    tx.upsertPurchaseItems(list.flatMap { p ->
                        p.items.orEmpty().map { PurchaseItemEntity(it.id, p.id, it.productId, it.quantity ?: 0.0, it.unitCost ?: 0.0) }
                    })
                }
                r.sales?.takeIf { it.isNotEmpty() }?.let { list ->
                    tx.upsertSales(list.map {
                        SaleEntity(
                            id = it.id, invoiceNo = it.invoiceNo.orEmpty(), customerName = it.customerName.orEmpty(),
                            customerPhone = it.customerPhone, warehouseId = it.warehouseId, date = Dates.parse(it.date),
                            subtotal = it.subtotal ?: 0.0, discount = it.discount ?: 0.0, total = it.total ?: 0.0,
                            totalCost = it.totalCost ?: 0.0, status = it.status ?: SaleStatus.COMPLETED, note = it.note,
                        )
                    })
                    tx.deleteSaleItemsFor(list.map { it.id })
                    tx.upsertSaleItems(list.flatMap { s ->
                        s.items.orEmpty().map {
                            SaleItemEntity(it.id, s.id, it.productId, it.productName.orEmpty(), it.quantity ?: 0.0, it.unitPrice ?: 0.0, it.unitCost ?: 0.0)
                        }
                    })
                }
                r.transfers?.let { list ->
                    tx.upsertTransfers(list.map {
                        TransferEntity(it.id, it.productId, it.fromWarehouseId, it.toWarehouseId, it.quantity ?: 0.0, Dates.parse(it.date), it.note)
                    })
                }
                r.movements?.let { list ->
                    tx.upsertMovements(list.map {
                        MovementEntity(
                            id = it.id, productId = it.productId, warehouseId = it.warehouseId, type = it.type.orEmpty(),
                            quantity = it.quantity ?: 0.0, balanceAfter = it.balanceAfter ?: 0.0, refType = it.refType,
                            refId = it.refId, note = it.note, userName = it.userName, date = Dates.parse(it.date),
                        )
                    })
                }

                r.expenses?.let { list ->
                    db.expenses().upsertAll(list.map {
                        ExpenseEntity(it.id, it.category ?: "Other", it.amount ?: 0.0, Dates.parse(it.date), it.note, it.userName)
                    })
                }

                r.deleted?.let { d ->
                    d.expenses?.takeIf { it.isNotEmpty() }?.let { db.expenses().deleteIds(it) }
                    d.products?.takeIf { it.isNotEmpty() }?.let { catalog.deleteStocksFor(it); catalog.deleteProducts(it) }
                    d.categories?.takeIf { it.isNotEmpty() }?.let { ids -> ids.forEach { catalog.detachCategory(it) }; m.deleteCategories(ids) }
                    d.warehouses?.takeIf { it.isNotEmpty() }?.let { m.deleteWarehouses(it) }
                    d.suppliers?.takeIf { it.isNotEmpty() }?.let { m.deleteSuppliers(it) }
                }
            }
            session.setLastSync(r.serverTime)
        } finally {
            _syncing.value = false
        }
    }

    /** Upload of the whole Basic database to a freshly upgraded cloud account. */
    suspend fun uploadLocal(merge: Boolean) {
        val gson = api.gson
        val catalog = db.catalog()
        val tx = db.tx()

        val purchaseItems = tx.allPurchaseItems().groupBy { it.purchaseId }
        val saleItems = tx.allSaleItems().groupBy { it.saleId }

        fun <T> arr(list: List<T>, items: (T) -> JsonArray? = { null }) = JsonArray().apply {
            list.forEach { e ->
                val obj = gson.toJsonTree(e).asJsonObject
                items(e)?.let { obj.add("items", it) }
                add(obj)
            }
        }

        val body = JsonObject().apply {
            addProperty("merge", merge)
            add("categories", arr(catalog.allCategories()))
            add("warehouses", arr(catalog.allWarehouses()))
            add("suppliers", arr(catalog.allSuppliers()))
            add("products", arr(catalog.allProducts()))
            add("stocks", arr(catalog.allStocks()))
            add("purchases", arr(tx.allPurchases()) { p -> gson.toJsonTree(purchaseItems[p.id].orEmpty()).asJsonArray })
            add("sales", arr(tx.allSales()) { s -> gson.toJsonTree(saleItems[s.id].orEmpty()).asJsonArray })
            add("transfers", arr(tx.allTransfers()))
            add("movements", arr(tx.allMovements()))
            add("expenses", arr(db.expenses().all()))
        }
        api.call { importLocal(body) }
    }

    suspend fun hasLocalData(): Boolean =
        db.catalog().allProducts().isNotEmpty() || db.tx().saleCount() > 0 || db.expenses().all().isNotEmpty()

    /** Wipes every table (used before a full cloud pull and on sign-out). */
    suspend fun clearAll() {
        val m = db.maintenance()
        m.clearSaleItems(); m.clearSales(); m.clearPurchaseItems(); m.clearPurchases()
        m.clearTransfers(); m.clearMovements(); m.clearStocks(); m.clearProducts()
        m.clearSuppliers(); m.clearCategories(); m.clearWarehouses()
        db.expenses().clear()
    }

    /** Fresh local database with a default warehouse (after signing out of the cloud). */
    suspend fun resetLocal() = db.withTransaction {
        clearAll()
        db.maintenance().insertWarehouse(WarehouseEntity(name = "Main Warehouse", isDefault = true))
    }
}
