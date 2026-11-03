package com.sandalia.stockanchor.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Room schema. In Basic mode this IS the database. In Premium (cloud) mode the same tables act as an
 * offline cache of the Laravel API and the ids are the server ids.
 */

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
)

@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String? = null,
    val isDefault: Boolean = false,
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val note: String? = null,
)

@Entity(
    tableName = "products",
    indices = [Index("categoryId"), Index("barcode"), Index("sku"), Index("warehouseId"), Index("supplierId")],
    foreignKeys = [
        ForeignKey(entity = WarehouseEntity::class, parentColumns = ["id"], childColumns = ["warehouseId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = SupplierEntity::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.SET_NULL),
    ],
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String? = null,
    val barcode: String? = null,
    val categoryId: Long? = null,
    /** home warehouse of the product; stock itself may live in several warehouses (see [StockEntity]) */
    val warehouseId: Long? = null,
    /** usual supplier (Premium) */
    val supplierId: Long? = null,
    val unit: String = "pcs",
    val costPrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val lowStockThreshold: Double = 5.0,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "stocks", primaryKeys = ["productId", "warehouseId"], indices = [Index("warehouseId")])
data class StockEntity(
    val productId: Long,
    val warehouseId: Long,
    val quantity: Double,
)

@Entity(tableName = "purchases", indices = [Index("date")])
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val refNo: String,
    val supplierId: Long? = null,
    val warehouseId: Long,
    val date: Long,
    val total: Double,
    val note: String? = null,
)

@Entity(tableName = "purchase_items", indices = [Index("purchaseId")])
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val quantity: Double,
    val unitCost: Double,
)

@Entity(tableName = "sales", indices = [Index("date"), Index("invoiceNo")])
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNo: String,
    val customerName: String,
    val customerPhone: String? = null,
    val warehouseId: Long,
    val date: Long,
    val subtotal: Double,
    val discount: Double,
    val total: Double,
    val totalCost: Double,
    val status: String = SaleStatus.COMPLETED,
    val note: String? = null,
)

object SaleStatus {
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"
}

@Entity(tableName = "sale_items", indices = [Index("saleId")])
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val unitCost: Double,
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val fromWarehouseId: Long,
    val toWarehouseId: Long,
    val quantity: Double,
    val date: Long,
    val note: String? = null,
)

@Entity(tableName = "stock_movements", indices = [Index("productId"), Index("date")])
data class MovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val warehouseId: Long,
    /** purchase, sale, sale_cancel, transfer_in, transfer_out, adjustment, import */
    val type: String,
    /** signed: + into stock, - out of stock */
    val quantity: Double,
    val balanceAfter: Double = 0.0,
    val refType: String? = null,
    val refId: Long? = null,
    val note: String? = null,
    val userName: String? = null,
    val date: Long,
)

/* ---------- Query projections ---------- */

data class ProductWithStock(
    val id: Long,
    val name: String,
    val sku: String?,
    val barcode: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val warehouseId: Long?,
    val warehouseName: String?,
    val unit: String,
    val costPrice: Double,
    val salePrice: Double,
    val lowStockThreshold: Double,
    /** quantity in the selected warehouse, or across all warehouses when no filter is set */
    val quantity: Double,
    /** warehouses currently holding stock of this product, e.g. "Main, Bogura" (product list only) */
    val stockLocations: String? = null,
)

data class StockRow(
    val productId: Long,
    val warehouseId: Long,
    val warehouseName: String,
    val quantity: Double,
)

data class MovementRow(
    val id: Long,
    val productName: String,
    val unit: String,
    val warehouseName: String,
    val type: String,
    val quantity: Double,
    val balanceAfter: Double,
    val note: String?,
    val userName: String?,
    val date: Long,
)

data class TransferRow(
    val id: Long,
    val productName: String,
    val unit: String,
    val fromName: String,
    val toName: String,
    val quantity: Double,
    val date: Long,
    val note: String?,
)

data class PurchaseRow(
    val id: Long,
    val refNo: String,
    val supplierName: String?,
    val warehouseName: String,
    val date: Long,
    val total: Double,
    val itemCount: Int,
)

data class PeriodTotals(
    val salesCount: Int,
    val revenue: Double,
    val cost: Double,
)

data class StockTotals(
    val quantity: Double,
    val costValue: Double,
    val retailValue: Double,
)

data class DayBucket(
    val bucket: String,
    val revenue: Double,
    val profit: Double,
)

data class CategoryWithCount(
    val id: Long,
    val name: String,
    val description: String?,
    val productCount: Int,
)

/** Operating cost (rent, salaries...). Net profit = gross profit - expenses. */
@Entity(tableName = "expenses", indices = [Index("date")])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amount: Double,
    val date: Long,
    val note: String? = null,
    val userName: String? = null,
)

data class ExpenseCategoryTotal(val category: String, val total: Double, val count: Int)
