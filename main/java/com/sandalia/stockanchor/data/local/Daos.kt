package com.sandalia.stockanchor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    // ---------- Categories ----------
    @Query(
        """SELECT c.id, c.name, c.description,
           (SELECT COUNT(*) FROM products p WHERE p.categoryId = c.id) AS productCount
           FROM categories c ORDER BY c.name COLLATE NOCASE"""
    )
    fun categoriesWithCount(): Flow<List<CategoryWithCount>>

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    fun categories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories") suspend fun allCategories(): List<CategoryEntity>
    @Upsert suspend fun upsertCategory(c: CategoryEntity): Long
    @Upsert suspend fun upsertCategories(c: List<CategoryEntity>)
    @Query("DELETE FROM categories WHERE id = :id") suspend fun deleteCategory(id: Long)
    @Query("UPDATE products SET categoryId = NULL WHERE categoryId = :id") suspend fun detachCategory(id: Long)

    // ---------- Warehouses ----------
    @Query("SELECT * FROM warehouses ORDER BY isDefault DESC, name COLLATE NOCASE")
    fun warehouses(): Flow<List<WarehouseEntity>>

    @Query("SELECT * FROM warehouses ORDER BY isDefault DESC, id") suspend fun allWarehouses(): List<WarehouseEntity>
    @Query("SELECT * FROM warehouses WHERE isDefault = 1 LIMIT 1") suspend fun defaultWarehouse(): WarehouseEntity?
    @Query("SELECT COUNT(*) FROM warehouses") suspend fun warehouseCount(): Int
    @Upsert suspend fun upsertWarehouse(w: WarehouseEntity): Long
    @Upsert suspend fun upsertWarehouses(w: List<WarehouseEntity>)
    @Query("UPDATE warehouses SET isDefault = 0 WHERE id != :keepId") suspend fun clearDefaultExcept(keepId: Long)
    @Query("DELETE FROM warehouses WHERE id = :id") suspend fun deleteWarehouse(id: Long)

    // ---------- Suppliers ----------
    @Query("SELECT * FROM suppliers ORDER BY name COLLATE NOCASE")
    fun suppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers") suspend fun allSuppliers(): List<SupplierEntity>
    @Upsert suspend fun upsertSupplier(s: SupplierEntity): Long
    @Upsert suspend fun upsertSuppliers(s: List<SupplierEntity>)
    @Query("DELETE FROM suppliers WHERE id = :id") suspend fun deleteSupplier(id: Long)

    // ---------- Products ----------
    @Query(
        """SELECT p.id, p.name, p.sku, p.barcode, p.categoryId, c.name AS categoryName, p.warehouseId, w.name AS warehouseName, p.unit,
                  p.costPrice, p.salePrice, p.lowStockThreshold,
                  COALESCE((SELECT SUM(s.quantity) FROM stocks s
                            WHERE s.productId = p.id AND (:warehouseId IS NULL OR s.warehouseId = :warehouseId)), 0) AS quantity,
                  (SELECT GROUP_CONCAT(w3.name, ', ') FROM stocks s3 JOIN warehouses w3 ON w3.id = s3.warehouseId
                    WHERE s3.productId = p.id AND s3.quantity > 0) AS stockLocations
           FROM products p LEFT JOIN categories c ON c.id = p.categoryId LEFT JOIN warehouses w ON w.id = p.warehouseId
           WHERE (:categoryId IS NULL OR p.categoryId = :categoryId)
             AND (:supplierId IS NULL OR p.supplierId = :supplierId)
             AND (:warehouseId IS NULL OR p.warehouseId = :warehouseId
                  OR EXISTS (SELECT 1 FROM stocks s2 WHERE s2.productId = p.id AND s2.warehouseId = :warehouseId AND s2.quantity > 0))
             AND (:query = '' OR p.name LIKE '%' || :query || '%' OR p.sku LIKE '%' || :query || '%' OR p.barcode = :query)
           ORDER BY p.name COLLATE NOCASE"""
    )
    fun products(query: String, categoryId: Long?, warehouseId: Long?, supplierId: Long?): Flow<List<ProductWithStock>>

    @Query(
        """SELECT * FROM (
             SELECT p.id, p.name, p.sku, p.barcode, p.categoryId, c.name AS categoryName, p.warehouseId, w.name AS warehouseName, p.unit,
                    p.costPrice, p.salePrice, p.lowStockThreshold,
                    COALESCE((SELECT SUM(s.quantity) FROM stocks s WHERE s.productId = p.id), 0) AS quantity,
                    NULL AS stockLocations
             FROM products p LEFT JOIN categories c ON c.id = p.categoryId LEFT JOIN warehouses w ON w.id = p.warehouseId)
           WHERE quantity <= lowStockThreshold ORDER BY quantity ASC"""
    )
    fun lowStock(): Flow<List<ProductWithStock>>

    @Query(
        """SELECT * FROM (
             SELECT p.id, p.name, p.sku, p.barcode, p.categoryId, NULL AS categoryName, p.warehouseId, NULL AS warehouseName, p.unit,
                    p.costPrice, p.salePrice, p.lowStockThreshold,
                    COALESCE((SELECT SUM(s.quantity) FROM stocks s WHERE s.productId = p.id), 0) AS quantity,
                    NULL AS stockLocations
             FROM products p)
           WHERE quantity <= lowStockThreshold"""
    )
    suspend fun lowStockNow(): List<ProductWithStock>

    @Query("SELECT * FROM products WHERE id = :id") suspend fun product(id: Long): ProductEntity?
    @Query("SELECT * FROM products WHERE id = :id") fun productFlow(id: Long): Flow<ProductEntity?>
    @Query("SELECT * FROM products WHERE barcode = :code OR sku = :code LIMIT 1") suspend fun productByCode(code: String): ProductEntity?
    @Query("SELECT * FROM products WHERE name = :name COLLATE NOCASE LIMIT 1") suspend fun productByName(name: String): ProductEntity?
    @Query("SELECT * FROM products") suspend fun allProducts(): List<ProductEntity>
    @Upsert suspend fun upsertProduct(p: ProductEntity): Long
    @Upsert suspend fun upsertProducts(p: List<ProductEntity>)
    @Query("DELETE FROM products WHERE id = :id") suspend fun deleteProduct(id: Long)
    @Query("DELETE FROM products WHERE id IN (:ids)") suspend fun deleteProducts(ids: List<Long>)
    @Query("UPDATE products SET costPrice = :cost WHERE id = :id") suspend fun updateCost(id: Long, cost: Double)

    // ---------- Stock ----------
    @Query(
        """SELECT s.productId, s.warehouseId, w.name AS warehouseName, s.quantity
           FROM stocks s JOIN warehouses w ON w.id = s.warehouseId
           WHERE s.productId = :productId ORDER BY w.isDefault DESC, w.name"""
    )
    fun stockForProduct(productId: Long): Flow<List<StockRow>>

    @Query("SELECT * FROM stocks WHERE productId = :productId AND warehouseId = :warehouseId")
    suspend fun stock(productId: Long, warehouseId: Long): StockEntity?

    @Query("SELECT * FROM stocks") suspend fun allStocks(): List<StockEntity>
    @Query("SELECT COALESCE(SUM(quantity),0) FROM stocks WHERE warehouseId = :warehouseId") suspend fun warehouseQuantity(warehouseId: Long): Double
    @Upsert suspend fun upsertStock(s: StockEntity)
    @Upsert suspend fun upsertStocks(s: List<StockEntity>)
    @Query("DELETE FROM stocks WHERE productId IN (:productIds)") suspend fun deleteStocksFor(productIds: List<Long>)
}

@Dao
interface TxDao {
    // ---------- Purchases ----------
    @Insert suspend fun insertPurchase(p: PurchaseEntity): Long
    @Insert suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)
    @Upsert suspend fun upsertPurchases(p: List<PurchaseEntity>)
    @Upsert suspend fun upsertPurchaseItems(items: List<PurchaseItemEntity>)
    @Query("DELETE FROM purchase_items WHERE purchaseId IN (:ids)") suspend fun deletePurchaseItemsFor(ids: List<Long>)
    @Query("SELECT COUNT(*) FROM purchases") suspend fun purchaseCount(): Int
    @Query("SELECT * FROM purchases") suspend fun allPurchases(): List<PurchaseEntity>
    @Query("SELECT * FROM purchase_items") suspend fun allPurchaseItems(): List<PurchaseItemEntity>

    @Query(
        """SELECT pu.id, pu.refNo, su.name AS supplierName, w.name AS warehouseName, pu.date, pu.total,
                  (SELECT COUNT(*) FROM purchase_items i WHERE i.purchaseId = pu.id) AS itemCount
           FROM purchases pu LEFT JOIN suppliers su ON su.id = pu.supplierId
           LEFT JOIN warehouses w ON w.id = pu.warehouseId
           ORDER BY pu.date DESC LIMIT 200"""
    )
    fun recentPurchases(): Flow<List<PurchaseRow>>

    // ---------- Sales ----------
    @Insert suspend fun insertSale(s: SaleEntity): Long
    @Insert suspend fun insertSaleItems(items: List<SaleItemEntity>)
    @Upsert suspend fun upsertSales(s: List<SaleEntity>)
    @Upsert suspend fun upsertSaleItems(items: List<SaleItemEntity>)
    @Query("DELETE FROM sale_items WHERE saleId IN (:ids)") suspend fun deleteSaleItemsFor(ids: List<Long>)
    @Query("UPDATE sales SET status = :status, note = :note WHERE id = :id") suspend fun updateSaleStatus(id: Long, status: String, note: String?)
    @Query("SELECT COUNT(*) FROM sales") suspend fun saleCount(): Int
    @Query("SELECT * FROM sales") suspend fun allSales(): List<SaleEntity>
    @Query("SELECT * FROM sale_items") suspend fun allSaleItems(): List<SaleItemEntity>

    @Query(
        """SELECT * FROM sales
           WHERE (:query = '' OR customerName LIKE '%' || :query || '%' OR invoiceNo LIKE '%' || :query || '%'
                  OR customerPhone LIKE '%' || :query || '%')
             AND date BETWEEN :from AND :to
           ORDER BY date DESC LIMIT :limit"""
    )
    fun sales(query: String, from: Long, to: Long, limit: Int): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id") fun saleFlow(id: Long): Flow<SaleEntity?>
    @Query("SELECT * FROM sales WHERE id = :id") suspend fun sale(id: Long): SaleEntity?
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId") fun saleItemsFlow(saleId: Long): Flow<List<SaleItemEntity>>
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId") suspend fun saleItems(saleId: Long): List<SaleItemEntity>

    // ---------- Transfers ----------
    @Insert suspend fun insertTransfer(t: TransferEntity): Long
    @Upsert suspend fun upsertTransfers(t: List<TransferEntity>)
    @Query("SELECT * FROM transfers") suspend fun allTransfers(): List<TransferEntity>

    @Query(
        """SELECT t.id, p.name AS productName, p.unit, wf.name AS fromName, wt.name AS toName, t.quantity, t.date, t.note
           FROM transfers t JOIN products p ON p.id = t.productId
           JOIN warehouses wf ON wf.id = t.fromWarehouseId JOIN warehouses wt ON wt.id = t.toWarehouseId
           ORDER BY t.date DESC LIMIT 200"""
    )
    fun recentTransfers(): Flow<List<TransferRow>>

    // ---------- Movements ----------
    @Insert suspend fun insertMovement(m: MovementEntity): Long
    @Upsert suspend fun upsertMovements(m: List<MovementEntity>)
    @Query("SELECT * FROM stock_movements") suspend fun allMovements(): List<MovementEntity>

    @Query(
        """SELECT m.id, p.name AS productName, p.unit, w.name AS warehouseName, m.type, m.quantity, m.balanceAfter,
                  m.note, m.userName, m.date
           FROM stock_movements m JOIN products p ON p.id = m.productId JOIN warehouses w ON w.id = m.warehouseId
           WHERE (:productId IS NULL OR m.productId = :productId)
             AND (:warehouseId IS NULL OR m.warehouseId = :warehouseId)
             AND (:type IS NULL OR m.type = :type)
           ORDER BY m.date DESC, m.id DESC LIMIT 500"""
    )
    fun movements(productId: Long?, warehouseId: Long?, type: String?): Flow<List<MovementRow>>
}

@Dao
interface ReportDao {
    @Query(
        """SELECT COUNT(*) AS salesCount, COALESCE(SUM(total),0) AS revenue, COALESCE(SUM(totalCost),0) AS cost
           FROM sales WHERE status = 'completed' AND date BETWEEN :from AND :to"""
    )
    fun periodTotals(from: Long, to: Long): Flow<PeriodTotals>

    @Query("SELECT COALESCE(SUM(total),0) FROM purchases WHERE date BETWEEN :from AND :to")
    fun purchaseTotal(from: Long, to: Long): Flow<Double>

    @Query(
        """SELECT COALESCE(SUM(i.quantity),0) FROM sale_items i JOIN sales s ON s.id = i.saleId
           WHERE s.status = 'completed' AND s.date BETWEEN :from AND :to"""
    )
    fun itemsSold(from: Long, to: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(quantity),0) FROM stock_movements WHERE type IN ('purchase','import') AND date BETWEEN :from AND :to")
    fun stockIn(from: Long, to: Long): Flow<Double>

    @Query(
        """SELECT COALESCE(SUM(s.quantity),0) AS quantity,
                  COALESCE(SUM(s.quantity * p.costPrice),0) AS costValue,
                  COALESCE(SUM(s.quantity * p.salePrice),0) AS retailValue
           FROM stocks s JOIN products p ON p.id = s.productId"""
    )
    fun stockTotals(): Flow<StockTotals>

    /** fmt: '%H' for a day, '%Y-%m-%d' for a month, '%Y-%m' for a year */
    @Query(
        """SELECT strftime(:fmt, date / 1000, 'unixepoch', 'localtime') AS bucket,
                  COALESCE(SUM(total),0) AS revenue, COALESCE(SUM(total - totalCost),0) AS profit
           FROM sales WHERE status = 'completed' AND date BETWEEN :from AND :to
           GROUP BY bucket ORDER BY bucket"""
    )
    fun buckets(fmt: String, from: Long, to: Long): Flow<List<DayBucket>>

    @Query("SELECT COUNT(*) FROM products") fun productCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount),0) FROM expenses WHERE date BETWEEN :from AND :to")
    fun expenseTotal(from: Long, to: Long): Flow<Double>
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE date BETWEEN :from AND :to ORDER BY date DESC, id DESC")
    fun expenses(from: Long, to: Long): Flow<List<ExpenseEntity>>

    @Query(
        """SELECT category, SUM(amount) AS total, COUNT(*) AS count FROM expenses
           WHERE date BETWEEN :from AND :to GROUP BY category ORDER BY total DESC"""
    )
    fun categoryTotals(from: Long, to: Long): Flow<List<ExpenseCategoryTotal>>

    @Query("SELECT DISTINCT category FROM expenses ORDER BY category") fun usedCategories(): Flow<List<String>>
    @Query("SELECT * FROM expenses") suspend fun all(): List<ExpenseEntity>
    @Upsert suspend fun upsert(e: ExpenseEntity): Long
    @Upsert suspend fun upsertAll(e: List<ExpenseEntity>)
    @Query("DELETE FROM expenses WHERE id = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM expenses WHERE id IN (:ids)") suspend fun deleteIds(ids: List<Long>)
    @Query("DELETE FROM expenses") suspend fun clear()
}

@Dao
interface MaintenanceDao {
    @Query("DELETE FROM categories") suspend fun clearCategories()
    @Query("DELETE FROM warehouses") suspend fun clearWarehouses()
    @Query("DELETE FROM suppliers") suspend fun clearSuppliers()
    @Query("DELETE FROM products") suspend fun clearProducts()
    @Query("DELETE FROM stocks") suspend fun clearStocks()
    @Query("DELETE FROM purchases") suspend fun clearPurchases()
    @Query("DELETE FROM purchase_items") suspend fun clearPurchaseItems()
    @Query("DELETE FROM sales") suspend fun clearSales()
    @Query("DELETE FROM sale_items") suspend fun clearSaleItems()
    @Query("DELETE FROM transfers") suspend fun clearTransfers()
    @Query("DELETE FROM stock_movements") suspend fun clearMovements()
    @Query("DELETE FROM categories WHERE id IN (:ids)") suspend fun deleteCategories(ids: List<Long>)
    @Query("DELETE FROM warehouses WHERE id IN (:ids)") suspend fun deleteWarehouses(ids: List<Long>)
    @Query("DELETE FROM suppliers WHERE id IN (:ids)") suspend fun deleteSuppliers(ids: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertWarehouse(w: WarehouseEntity): Long
}
