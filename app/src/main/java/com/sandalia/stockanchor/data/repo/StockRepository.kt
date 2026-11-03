package com.sandalia.stockanchor.data.repo

import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.str
import androidx.room.withTransaction
import com.sandalia.stockanchor.data.remote.ExpenseRequest
import com.sandalia.stockanchor.data.local.ExpenseEntity
import com.sandalia.stockanchor.data.local.AppDatabase
import com.sandalia.stockanchor.data.local.CategoryEntity
import com.sandalia.stockanchor.data.local.MovementEntity
import com.sandalia.stockanchor.data.local.ProductEntity
import com.sandalia.stockanchor.data.local.PurchaseEntity
import com.sandalia.stockanchor.data.local.PurchaseItemEntity
import com.sandalia.stockanchor.data.local.SaleEntity
import com.sandalia.stockanchor.data.local.SaleItemEntity
import com.sandalia.stockanchor.data.local.StockEntity
import com.sandalia.stockanchor.data.local.WarehouseEntity
import com.sandalia.stockanchor.data.prefs.SessionStore
import com.sandalia.stockanchor.data.remote.AdjustRequest
import com.sandalia.stockanchor.data.remote.ApiClient
import com.sandalia.stockanchor.data.remote.CancelRequest
import com.sandalia.stockanchor.data.remote.CategoryRequest
import com.sandalia.stockanchor.data.remote.ImportResult
import com.sandalia.stockanchor.data.remote.ImportRowsRequest
import com.sandalia.stockanchor.data.remote.LineRequest
import com.sandalia.stockanchor.data.remote.ProductRequest
import com.sandalia.stockanchor.data.remote.PurchaseRequest
import com.sandalia.stockanchor.data.remote.SaleRequest
import com.sandalia.stockanchor.data.remote.SupplierRequest
import com.sandalia.stockanchor.data.remote.TransferRequest
import com.sandalia.stockanchor.data.remote.WarehouseRequest
import com.sandalia.stockanchor.domain.Limits
import com.sandalia.stockanchor.util.Fmt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** A line in the purchase or sale cart. */
data class CartLine(
    val productId: Long,
    val name: String,
    val unit: String,
    val quantity: Double,
    /** unit cost for purchases, unit price for sales */
    val unitAmount: Double,
    val unitCost: Double = 0.0,
    val available: Double = 0.0,
) {
    val lineTotal get() = quantity * unitAmount
}

class UserFacingException(message: String) : Exception(message)

/**
 * Single repository for the whole app.
 *  - Basic (LOCAL): every operation is a Room transaction.
 *  - Premium (CLOUD): the API performs the operation, then [SyncManager.pull] refreshes the Room cache.
 * The UI only ever observes Room, so screens are identical in both modes.
 */
class StockRepository(
    private val db: AppDatabase,
    private val api: ApiClient,
    private val session: SessionStore,
    private val sync: SyncManager,
) {
    private val catalog = db.catalog()
    private val tx = db.tx()
    private val cloud get() = session.current.isCloud

    // ------------------------------------------------------------------ reads
    fun categoriesWithCount() = catalog.categoriesWithCount()
    fun categories() = catalog.categories()
    fun warehouses() = catalog.warehouses()
    fun suppliers() = catalog.suppliers()
    fun products(query: String, categoryId: Long?, warehouseId: Long?, supplierId: Long? = null) =
        catalog.products(query.trim(), categoryId, warehouseId, supplierId)
    fun lowStock() = catalog.lowStock()
    fun product(id: Long) = catalog.productFlow(id)
    fun stockForProduct(id: Long) = catalog.stockForProduct(id)
    fun sales(query: String, from: Long, to: Long, limit: Int = 300) = tx.sales(query.trim(), from, to, limit)
    fun sale(id: Long) = tx.saleFlow(id)
    fun saleItems(id: Long) = tx.saleItemsFlow(id)
    fun recentPurchases() = tx.recentPurchases()
    fun recentTransfers() = tx.recentTransfers()
    fun movements(productId: Long?, warehouseId: Long?, type: String?) = tx.movements(productId, warehouseId, type)
    suspend fun findByCode(code: String): ProductEntity? = catalog.productByCode(code.trim())
    suspend fun productOnce(id: Long) = catalog.product(id)
    suspend fun stockOf(productId: Long, warehouseId: Long) = catalog.stock(productId, warehouseId)?.quantity ?: 0.0
    suspend fun defaultWarehouseId(): Long? = catalog.defaultWarehouse()?.id ?: catalog.allWarehouses().firstOrNull()?.id

    // ------------------------------------------------------------------ expenses
    fun expenses(from: Long, to: Long) = db.expenses().expenses(from, to)
    fun expenseCategoryTotals(from: Long, to: Long) = db.expenses().categoryTotals(from, to)
    fun usedExpenseCategories() = db.expenses().usedCategories()

    suspend fun saveExpense(e: ExpenseEntity) {
        if (!session.current.premiumActive) throw UserFacingException(str(R.string.expenses_are_a_premium_feature))
        require(e.category.isNotBlank()) { str(R.string.choose_a_category) }
        if (e.amount <= 0) throw UserFacingException(str(R.string.enter_an_amount_greater_than_zero))
        val clean = e.copy(category = e.category.trim(), note = e.note.nb())
        if (cloud) {
            val iso = java.time.Instant.ofEpochMilli(clean.date).atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime().toString()
            val body = ExpenseRequest(clean.category, clean.amount, iso, clean.note)
            api.call { if (e.id == 0L) createExpense(body) else updateExpense(e.id, body) }
            sync.pull()
        } else {
            db.expenses().upsert(clean.copy(userName = clean.userName ?: session.current.userName.ifBlank { null }))
        }
    }

    suspend fun deleteExpense(id: Long) {
        if (cloud) {
            api.call { deleteExpense(id) }; sync.pull()
        } else db.expenses().delete(id)
    }

    // ------------------------------------------------------------------ categories
    suspend fun saveCategory(id: Long?, name: String, description: String?) {
        require(name.isNotBlank()) { str(R.string.category_name_is_required) }
        if (cloud) {
            val body = CategoryRequest(name.trim(), description?.trim()?.ifBlank { null })
            api.call { if (id == null) createCategory(body) else updateCategory(id, body) }
            sync.pull()
        } else {
            catalog.upsertCategory(CategoryEntity(id ?: 0, name.trim(), description?.trim()?.ifBlank { null }))
        }
    }

    suspend fun deleteCategory(id: Long) {
        if (cloud) {
            api.call { deleteCategory(id) }; sync.pull()
        } else db.withTransaction {
            catalog.detachCategory(id); catalog.deleteCategory(id)
        }
    }

    // ------------------------------------------------------------------ warehouses
    suspend fun saveWarehouse(id: Long?, name: String, location: String?, isDefault: Boolean) {
        require(name.isNotBlank()) { str(R.string.warehouse_name_is_required) }
        if (cloud) {
            val body = WarehouseRequest(name.trim(), location?.trim()?.ifBlank { null }, isDefault)
            api.call { if (id == null) createWarehouse(body) else updateWarehouse(id, body) }
            sync.pull()
            return
        }
        db.withTransaction {
            if (id == null && catalog.warehouseCount() >= Limits.BASIC_WAREHOUSES) {
                throw UserFacingException(str(R.string.the_basic_plan_includes_x_upgrade, Limits.warehouses()))
            }
            val newId = catalog.upsertWarehouse(WarehouseEntity(id ?: 0, name.trim(), location?.trim()?.ifBlank { null }, isDefault))
            val savedId = if (id == null || id == 0L) newId else id
            if (isDefault) catalog.clearDefaultExcept(savedId)
        }
    }

    suspend fun deleteWarehouse(id: Long) {
        if (cloud) {
            api.call { deleteWarehouse(id) }; sync.pull(); return
        }
        db.withTransaction {
            if (catalog.warehouseCount() <= 1) throw UserFacingException(str(R.string.you_need_at_least_one_warehouse))
            if (catalog.warehouseQuantity(id) > 0) throw UserFacingException(str(R.string.this_warehouse_still_holds_stock_move))
            catalog.deleteWarehouse(id)
            if (catalog.defaultWarehouse() == null) catalog.allWarehouses().firstOrNull()?.let { catalog.upsertWarehouse(it.copy(isDefault = true)) }
        }
    }

    // ------------------------------------------------------------------ suppliers (Premium)
    suspend fun saveSupplier(id: Long?, name: String, phone: String?, email: String?, address: String?, note: String?) {
        require(name.isNotBlank()) { str(R.string.supplier_name_is_required) }
        val body = SupplierRequest(name.trim(), phone.nb(), email.nb(), address.nb(), note.nb())
        api.call { if (id == null) createSupplier(body) else updateSupplier(id, body) }
        sync.pull()
    }

    suspend fun deleteSupplier(id: Long) {
        api.call { deleteSupplier(id) }; sync.pull()
    }

    // ------------------------------------------------------------------ products
    suspend fun saveProduct(p: ProductEntity) {
        require(p.name.isNotBlank()) { str(R.string.product_name_is_required) }
        val clean = p.copy(name = p.name.trim(), sku = p.sku.nb(), barcode = p.barcode.nb(), description = p.description.nb())
        if (cloud) {
            val body = ProductRequest(
                clean.name, clean.sku, clean.barcode, clean.categoryId, clean.warehouseId, clean.supplierId, clean.unit.ifBlank { "pcs" },
                clean.costPrice, clean.salePrice, clean.lowStockThreshold, clean.description,
            )
            api.call { if (p.id == 0L) createProduct(body) else updateProduct(p.id, body) }
            sync.pull()
            return
        }
        clean.barcode?.let { code ->
            val other = catalog.productByCode(code)
            if (other != null && other.id != p.id) throw UserFacingException(str(R.string.barcode_is_already_used_by_x, other.name))
        }
        catalog.upsertProduct(clean)
    }

    suspend fun deleteProduct(id: Long) {
        if (cloud) {
            api.call { deleteProduct(id) }; sync.pull(); return
        }
        db.withTransaction {
            catalog.deleteStocksFor(listOf(id)); catalog.deleteProduct(id)
        }
    }

    // ------------------------------------------------------------------ purchase (stock in)
    suspend fun purchase(warehouseId: Long, supplierId: Long?, note: String?, lines: List<CartLine>, updateCost: Boolean = true) {
        if (lines.isEmpty()) throw UserFacingException(str(R.string.add_at_least_one_product))
        if (lines.any { it.quantity <= 0 }) throw UserFacingException(str(R.string.quantities_must_be_greater_than_zero))
        if (cloud) {
            api.call {
                createPurchase(PurchaseRequest(warehouseId, supplierId, note.nb(), updateCost,
                    lines.map { LineRequest(it.productId, it.quantity, unitCost = it.unitAmount) }))
            }
            sync.pull()
            return
        }
        db.withTransaction {
            val now = System.currentTimeMillis()
            val ref = "PO-${stamp()}-${(tx.purchaseCount() + 1).toString().padStart(5, '0')}"
            val purchaseId = tx.insertPurchase(
                PurchaseEntity(refNo = ref, supplierId = supplierId, warehouseId = warehouseId, date = now,
                    total = lines.sumOf { it.lineTotal }, note = note.nb())
            )
            tx.insertPurchaseItems(lines.map { PurchaseItemEntity(purchaseId = purchaseId, productId = it.productId, quantity = it.quantity, unitCost = it.unitAmount) })
            lines.forEach {
                applyLocal(it.productId, warehouseId, it.quantity, "purchase", "purchase", purchaseId, ref, now)
                if (updateCost) catalog.updateCost(it.productId, it.unitAmount)
            }
        }
    }

    // ------------------------------------------------------------------ sale
    /** Returns the id of the new sale (for the invoice screen). */
    suspend fun sale(
        customerName: String, customerPhone: String?, warehouseId: Long, discount: Double, note: String?, lines: List<CartLine>,
    ): Long {
        if (customerName.isBlank()) throw UserFacingException(str(R.string.customer_name_is_required))
        if (lines.isEmpty()) throw UserFacingException(str(R.string.add_at_least_one_product))
        if (cloud) {
            val created = api.call {
                createSale(SaleRequest(customerName.trim(), customerPhone.nb(), warehouseId, discount, note.nb(),
                    lines.map { LineRequest(it.productId, it.quantity, unitPrice = it.unitAmount) }))
            }
            sync.pull()
            return created.id
        }
        return db.withTransaction {
            val now = System.currentTimeMillis()
            val invoice = "INV-${stamp()}-${(tx.saleCount() + 1).toString().padStart(5, '0')}"
            val subtotal = lines.sumOf { it.lineTotal }
            val disc = discount.coerceIn(0.0, subtotal)
            val products = lines.associate { it.productId to catalog.product(it.productId) }
            val saleId = tx.insertSale(
                SaleEntity(
                    invoiceNo = invoice, customerName = customerName.trim(), customerPhone = customerPhone.nb(),
                    warehouseId = warehouseId, date = now, subtotal = subtotal, discount = disc, total = subtotal - disc,
                    totalCost = lines.sumOf { it.quantity * (products[it.productId]?.costPrice ?: 0.0) }, note = note.nb(),
                )
            )
            tx.insertSaleItems(lines.map {
                SaleItemEntity(saleId = saleId, productId = it.productId, productName = it.name, quantity = it.quantity,
                    unitPrice = it.unitAmount, unitCost = products[it.productId]?.costPrice ?: 0.0)
            })
            lines.forEach { applyLocal(it.productId, warehouseId, -it.quantity, "sale", "sale", saleId, invoice, now) }
            saleId
        }
    }

    /** Sales management (Premium). Returns goods to stock. */
    suspend fun cancelSale(id: Long, reason: String?) {
        api.call { cancelSale(id, CancelRequest(reason.nb())) }
        sync.pull()
    }

    // ------------------------------------------------------------------ transfers (Premium)
    suspend fun transfer(productId: Long, fromId: Long, toId: Long, quantity: Double, note: String?) {
        if (fromId == toId) throw UserFacingException(str(R.string.choose_two_different_warehouses))
        if (quantity <= 0) throw UserFacingException(str(R.string.quantity_must_be_greater_than_zero))
        api.call { createTransfer(TransferRequest(productId, fromId, toId, quantity, note.nb())) }
        sync.pull()
    }

    // ------------------------------------------------------------------ stock count correction
    suspend fun adjust(productId: Long, warehouseId: Long, setTo: Double, note: String?) {
        if (setTo < 0) throw UserFacingException(str(R.string.stock_can_t_be_negative))
        if (cloud) {
            api.call { adjustStock(AdjustRequest(productId, warehouseId, setTo, note.nb())) }
            sync.pull()
            return
        }
        db.withTransaction {
            val current = catalog.stock(productId, warehouseId)?.quantity ?: 0.0
            val delta = setTo - current
            if (delta != 0.0) applyLocal(productId, warehouseId, delta, "adjustment", null, null, note.nb() ?: "Stock count", System.currentTimeMillis())
        }
    }

    // ------------------------------------------------------------------ CSV import (Premium)
    suspend fun importStock(rows: List<Map<String, String>>): ImportResult {
        val result = api.call { importStock(ImportRowsRequest(rows)) }
        sync.pull()
        return result
    }

    // ------------------------------------------------------------------ helpers
    private suspend fun applyLocal(
        productId: Long, warehouseId: Long, delta: Double, type: String, refType: String?, refId: Long?, note: String?, date: Long,
    ) {
        val current = catalog.stock(productId, warehouseId)?.quantity ?: 0.0
        val next = Math.round((current + delta) * 1000.0) / 1000.0
        if (next < 0) {
            val name = catalog.product(productId)?.name ?: "product"
            throw UserFacingException(str(R.string.not_enough_stock_for_x_available, name, Fmt.qty(current), Fmt.qty(-delta)))
        }
        catalog.upsertStock(StockEntity(productId, warehouseId, next))
        tx.insertMovement(
            MovementEntity(productId = productId, warehouseId = warehouseId, type = type, quantity = delta, balanceAfter = next,
                refType = refType, refId = refId, note = note, userName = session.current.userName.ifBlank { null }, date = date)
        )
    }

    private fun stamp() = SimpleDateFormat("yyMMdd", Locale.US).format(Date())
    private fun String?.nb(): String? = this?.trim()?.ifBlank { null }
}
