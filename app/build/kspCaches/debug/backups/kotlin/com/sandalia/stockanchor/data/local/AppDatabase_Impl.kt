package com.sandalia.stockanchor.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _catalogDao: Lazy<CatalogDao> = lazy {
    CatalogDao_Impl(this)
  }

  private val _txDao: Lazy<TxDao> = lazy {
    TxDao_Impl(this)
  }

  private val _reportDao: Lazy<ReportDao> = lazy {
    ReportDao_Impl(this)
  }

  private val _maintenanceDao: Lazy<MaintenanceDao> = lazy {
    MaintenanceDao_Impl(this)
  }

  private val _expenseDao: Lazy<ExpenseDao> = lazy {
    ExpenseDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(4,
        "7d2a17f76b0754524b27f6214dad340e", "88158bf82730b4a1c34c492294c920eb") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `warehouses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `location` TEXT, `isDefault` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `suppliers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT, `email` TEXT, `address` TEXT, `note` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `products` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `sku` TEXT, `barcode` TEXT, `categoryId` INTEGER, `warehouseId` INTEGER, `supplierId` INTEGER, `unit` TEXT NOT NULL, `costPrice` REAL NOT NULL, `salePrice` REAL NOT NULL, `lowStockThreshold` REAL NOT NULL, `description` TEXT, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`warehouseId`) REFERENCES `warehouses`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`supplierId`) REFERENCES `suppliers`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_products_categoryId` ON `products` (`categoryId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_products_barcode` ON `products` (`barcode`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_products_sku` ON `products` (`sku`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_products_warehouseId` ON `products` (`warehouseId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_products_supplierId` ON `products` (`supplierId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `stocks` (`productId` INTEGER NOT NULL, `warehouseId` INTEGER NOT NULL, `quantity` REAL NOT NULL, PRIMARY KEY(`productId`, `warehouseId`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_stocks_warehouseId` ON `stocks` (`warehouseId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `purchases` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `refNo` TEXT NOT NULL, `supplierId` INTEGER, `warehouseId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `total` REAL NOT NULL, `note` TEXT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_purchases_date` ON `purchases` (`date`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `purchase_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `purchaseId` INTEGER NOT NULL, `productId` INTEGER NOT NULL, `quantity` REAL NOT NULL, `unitCost` REAL NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_purchase_items_purchaseId` ON `purchase_items` (`purchaseId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sales` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `invoiceNo` TEXT NOT NULL, `customerName` TEXT NOT NULL, `customerPhone` TEXT, `warehouseId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `subtotal` REAL NOT NULL, `discount` REAL NOT NULL, `total` REAL NOT NULL, `totalCost` REAL NOT NULL, `status` TEXT NOT NULL, `note` TEXT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_date` ON `sales` (`date`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_invoiceNo` ON `sales` (`invoiceNo`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sale_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `saleId` INTEGER NOT NULL, `productId` INTEGER NOT NULL, `productName` TEXT NOT NULL, `quantity` REAL NOT NULL, `unitPrice` REAL NOT NULL, `unitCost` REAL NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_items_saleId` ON `sale_items` (`saleId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `transfers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productId` INTEGER NOT NULL, `fromWarehouseId` INTEGER NOT NULL, `toWarehouseId` INTEGER NOT NULL, `quantity` REAL NOT NULL, `date` INTEGER NOT NULL, `note` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `stock_movements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productId` INTEGER NOT NULL, `warehouseId` INTEGER NOT NULL, `type` TEXT NOT NULL, `quantity` REAL NOT NULL, `balanceAfter` REAL NOT NULL, `refType` TEXT, `refId` INTEGER, `note` TEXT, `userName` TEXT, `date` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_productId` ON `stock_movements` (`productId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_date` ON `stock_movements` (`date`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `userName` TEXT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7d2a17f76b0754524b27f6214dad340e')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `categories`")
        connection.execSQL("DROP TABLE IF EXISTS `warehouses`")
        connection.execSQL("DROP TABLE IF EXISTS `suppliers`")
        connection.execSQL("DROP TABLE IF EXISTS `products`")
        connection.execSQL("DROP TABLE IF EXISTS `stocks`")
        connection.execSQL("DROP TABLE IF EXISTS `purchases`")
        connection.execSQL("DROP TABLE IF EXISTS `purchase_items`")
        connection.execSQL("DROP TABLE IF EXISTS `sales`")
        connection.execSQL("DROP TABLE IF EXISTS `sale_items`")
        connection.execSQL("DROP TABLE IF EXISTS `transfers`")
        connection.execSQL("DROP TABLE IF EXISTS `stock_movements`")
        connection.execSQL("DROP TABLE IF EXISTS `expenses`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategories.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("description", TableInfo.Column("description", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCategories: TableInfo = TableInfo("categories", _columnsCategories,
            _foreignKeysCategories, _indicesCategories)
        val _existingCategories: TableInfo = read(connection, "categories")
        if (!_infoCategories.equals(_existingCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |categories(com.sandalia.stockanchor.data.local.CategoryEntity).
              | Expected:
              |""".trimMargin() + _infoCategories + """
              |
              | Found:
              |""".trimMargin() + _existingCategories)
        }
        val _columnsWarehouses: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWarehouses.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWarehouses.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWarehouses.put("location", TableInfo.Column("location", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWarehouses.put("isDefault", TableInfo.Column("isDefault", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWarehouses: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWarehouses: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoWarehouses: TableInfo = TableInfo("warehouses", _columnsWarehouses,
            _foreignKeysWarehouses, _indicesWarehouses)
        val _existingWarehouses: TableInfo = read(connection, "warehouses")
        if (!_infoWarehouses.equals(_existingWarehouses)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |warehouses(com.sandalia.stockanchor.data.local.WarehouseEntity).
              | Expected:
              |""".trimMargin() + _infoWarehouses + """
              |
              | Found:
              |""".trimMargin() + _existingWarehouses)
        }
        val _columnsSuppliers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSuppliers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSuppliers.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSuppliers.put("phone", TableInfo.Column("phone", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSuppliers.put("email", TableInfo.Column("email", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSuppliers.put("address", TableInfo.Column("address", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSuppliers.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSuppliers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSuppliers: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSuppliers: TableInfo = TableInfo("suppliers", _columnsSuppliers,
            _foreignKeysSuppliers, _indicesSuppliers)
        val _existingSuppliers: TableInfo = read(connection, "suppliers")
        if (!_infoSuppliers.equals(_existingSuppliers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |suppliers(com.sandalia.stockanchor.data.local.SupplierEntity).
              | Expected:
              |""".trimMargin() + _infoSuppliers + """
              |
              | Found:
              |""".trimMargin() + _existingSuppliers)
        }
        val _columnsProducts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProducts.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("sku", TableInfo.Column("sku", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("barcode", TableInfo.Column("barcode", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("categoryId", TableInfo.Column("categoryId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("warehouseId", TableInfo.Column("warehouseId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("supplierId", TableInfo.Column("supplierId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("unit", TableInfo.Column("unit", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("costPrice", TableInfo.Column("costPrice", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("salePrice", TableInfo.Column("salePrice", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("lowStockThreshold", TableInfo.Column("lowStockThreshold", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("description", TableInfo.Column("description", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProducts.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProducts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysProducts.add(TableInfo.ForeignKey("warehouses", "SET NULL", "NO ACTION",
            listOf("warehouseId"), listOf("id")))
        _foreignKeysProducts.add(TableInfo.ForeignKey("suppliers", "SET NULL", "NO ACTION",
            listOf("supplierId"), listOf("id")))
        val _indicesProducts: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesProducts.add(TableInfo.Index("index_products_categoryId", false,
            listOf("categoryId"), listOf("ASC")))
        _indicesProducts.add(TableInfo.Index("index_products_barcode", false, listOf("barcode"),
            listOf("ASC")))
        _indicesProducts.add(TableInfo.Index("index_products_sku", false, listOf("sku"),
            listOf("ASC")))
        _indicesProducts.add(TableInfo.Index("index_products_warehouseId", false,
            listOf("warehouseId"), listOf("ASC")))
        _indicesProducts.add(TableInfo.Index("index_products_supplierId", false,
            listOf("supplierId"), listOf("ASC")))
        val _infoProducts: TableInfo = TableInfo("products", _columnsProducts, _foreignKeysProducts,
            _indicesProducts)
        val _existingProducts: TableInfo = read(connection, "products")
        if (!_infoProducts.equals(_existingProducts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |products(com.sandalia.stockanchor.data.local.ProductEntity).
              | Expected:
              |""".trimMargin() + _infoProducts + """
              |
              | Found:
              |""".trimMargin() + _existingProducts)
        }
        val _columnsStocks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsStocks.put("productId", TableInfo.Column("productId", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStocks.put("warehouseId", TableInfo.Column("warehouseId", "INTEGER", true, 2, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStocks.put("quantity", TableInfo.Column("quantity", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStocks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesStocks: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesStocks.add(TableInfo.Index("index_stocks_warehouseId", false, listOf("warehouseId"),
            listOf("ASC")))
        val _infoStocks: TableInfo = TableInfo("stocks", _columnsStocks, _foreignKeysStocks,
            _indicesStocks)
        val _existingStocks: TableInfo = read(connection, "stocks")
        if (!_infoStocks.equals(_existingStocks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |stocks(com.sandalia.stockanchor.data.local.StockEntity).
              | Expected:
              |""".trimMargin() + _infoStocks + """
              |
              | Found:
              |""".trimMargin() + _existingStocks)
        }
        val _columnsPurchases: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPurchases.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchases.put("refNo", TableInfo.Column("refNo", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchases.put("supplierId", TableInfo.Column("supplierId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchases.put("warehouseId", TableInfo.Column("warehouseId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchases.put("date", TableInfo.Column("date", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchases.put("total", TableInfo.Column("total", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchases.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPurchases: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPurchases: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesPurchases.add(TableInfo.Index("index_purchases_date", false, listOf("date"),
            listOf("ASC")))
        val _infoPurchases: TableInfo = TableInfo("purchases", _columnsPurchases,
            _foreignKeysPurchases, _indicesPurchases)
        val _existingPurchases: TableInfo = read(connection, "purchases")
        if (!_infoPurchases.equals(_existingPurchases)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |purchases(com.sandalia.stockanchor.data.local.PurchaseEntity).
              | Expected:
              |""".trimMargin() + _infoPurchases + """
              |
              | Found:
              |""".trimMargin() + _existingPurchases)
        }
        val _columnsPurchaseItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPurchaseItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchaseItems.put("purchaseId", TableInfo.Column("purchaseId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchaseItems.put("productId", TableInfo.Column("productId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchaseItems.put("quantity", TableInfo.Column("quantity", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPurchaseItems.put("unitCost", TableInfo.Column("unitCost", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPurchaseItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPurchaseItems: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesPurchaseItems.add(TableInfo.Index("index_purchase_items_purchaseId", false,
            listOf("purchaseId"), listOf("ASC")))
        val _infoPurchaseItems: TableInfo = TableInfo("purchase_items", _columnsPurchaseItems,
            _foreignKeysPurchaseItems, _indicesPurchaseItems)
        val _existingPurchaseItems: TableInfo = read(connection, "purchase_items")
        if (!_infoPurchaseItems.equals(_existingPurchaseItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |purchase_items(com.sandalia.stockanchor.data.local.PurchaseItemEntity).
              | Expected:
              |""".trimMargin() + _infoPurchaseItems + """
              |
              | Found:
              |""".trimMargin() + _existingPurchaseItems)
        }
        val _columnsSales: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSales.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("invoiceNo", TableInfo.Column("invoiceNo", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("customerName", TableInfo.Column("customerName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("customerPhone", TableInfo.Column("customerPhone", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("warehouseId", TableInfo.Column("warehouseId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("date", TableInfo.Column("date", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("subtotal", TableInfo.Column("subtotal", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("discount", TableInfo.Column("discount", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("total", TableInfo.Column("total", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("totalCost", TableInfo.Column("totalCost", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSales: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSales: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSales.add(TableInfo.Index("index_sales_date", false, listOf("date"), listOf("ASC")))
        _indicesSales.add(TableInfo.Index("index_sales_invoiceNo", false, listOf("invoiceNo"),
            listOf("ASC")))
        val _infoSales: TableInfo = TableInfo("sales", _columnsSales, _foreignKeysSales,
            _indicesSales)
        val _existingSales: TableInfo = read(connection, "sales")
        if (!_infoSales.equals(_existingSales)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sales(com.sandalia.stockanchor.data.local.SaleEntity).
              | Expected:
              |""".trimMargin() + _infoSales + """
              |
              | Found:
              |""".trimMargin() + _existingSales)
        }
        val _columnsSaleItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSaleItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("saleId", TableInfo.Column("saleId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("productId", TableInfo.Column("productId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("productName", TableInfo.Column("productName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("quantity", TableInfo.Column("quantity", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("unitPrice", TableInfo.Column("unitPrice", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("unitCost", TableInfo.Column("unitCost", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSaleItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSaleItems: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSaleItems.add(TableInfo.Index("index_sale_items_saleId", false, listOf("saleId"),
            listOf("ASC")))
        val _infoSaleItems: TableInfo = TableInfo("sale_items", _columnsSaleItems,
            _foreignKeysSaleItems, _indicesSaleItems)
        val _existingSaleItems: TableInfo = read(connection, "sale_items")
        if (!_infoSaleItems.equals(_existingSaleItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sale_items(com.sandalia.stockanchor.data.local.SaleItemEntity).
              | Expected:
              |""".trimMargin() + _infoSaleItems + """
              |
              | Found:
              |""".trimMargin() + _existingSaleItems)
        }
        val _columnsTransfers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTransfers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransfers.put("productId", TableInfo.Column("productId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransfers.put("fromWarehouseId", TableInfo.Column("fromWarehouseId", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransfers.put("toWarehouseId", TableInfo.Column("toWarehouseId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransfers.put("quantity", TableInfo.Column("quantity", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransfers.put("date", TableInfo.Column("date", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransfers.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTransfers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTransfers: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTransfers: TableInfo = TableInfo("transfers", _columnsTransfers,
            _foreignKeysTransfers, _indicesTransfers)
        val _existingTransfers: TableInfo = read(connection, "transfers")
        if (!_infoTransfers.equals(_existingTransfers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |transfers(com.sandalia.stockanchor.data.local.TransferEntity).
              | Expected:
              |""".trimMargin() + _infoTransfers + """
              |
              | Found:
              |""".trimMargin() + _existingTransfers)
        }
        val _columnsStockMovements: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsStockMovements.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("productId", TableInfo.Column("productId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("warehouseId", TableInfo.Column("warehouseId", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("quantity", TableInfo.Column("quantity", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("balanceAfter", TableInfo.Column("balanceAfter", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("refType", TableInfo.Column("refType", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("refId", TableInfo.Column("refId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("userName", TableInfo.Column("userName", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStockMovements.put("date", TableInfo.Column("date", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStockMovements: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesStockMovements: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesStockMovements.add(TableInfo.Index("index_stock_movements_productId", false,
            listOf("productId"), listOf("ASC")))
        _indicesStockMovements.add(TableInfo.Index("index_stock_movements_date", false,
            listOf("date"), listOf("ASC")))
        val _infoStockMovements: TableInfo = TableInfo("stock_movements", _columnsStockMovements,
            _foreignKeysStockMovements, _indicesStockMovements)
        val _existingStockMovements: TableInfo = read(connection, "stock_movements")
        if (!_infoStockMovements.equals(_existingStockMovements)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |stock_movements(com.sandalia.stockanchor.data.local.MovementEntity).
              | Expected:
              |""".trimMargin() + _infoStockMovements + """
              |
              | Found:
              |""".trimMargin() + _existingStockMovements)
        }
        val _columnsExpenses: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExpenses.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("category", TableInfo.Column("category", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("amount", TableInfo.Column("amount", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("date", TableInfo.Column("date", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("userName", TableInfo.Column("userName", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExpenses: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExpenses: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesExpenses.add(TableInfo.Index("index_expenses_date", false, listOf("date"),
            listOf("ASC")))
        val _infoExpenses: TableInfo = TableInfo("expenses", _columnsExpenses, _foreignKeysExpenses,
            _indicesExpenses)
        val _existingExpenses: TableInfo = read(connection, "expenses")
        if (!_infoExpenses.equals(_existingExpenses)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |expenses(com.sandalia.stockanchor.data.local.ExpenseEntity).
              | Expected:
              |""".trimMargin() + _infoExpenses + """
              |
              | Found:
              |""".trimMargin() + _existingExpenses)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "categories", "warehouses",
        "suppliers", "products", "stocks", "purchases", "purchase_items", "sales", "sale_items",
        "transfers", "stock_movements", "expenses")
  }

  public override fun clearAllTables() {
    super.performClear(true, "categories", "warehouses", "suppliers", "products", "stocks",
        "purchases", "purchase_items", "sales", "sale_items", "transfers", "stock_movements",
        "expenses")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(CatalogDao::class, CatalogDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TxDao::class, TxDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ReportDao::class, ReportDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MaintenanceDao::class, MaintenanceDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ExpenseDao::class, ExpenseDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun catalog(): CatalogDao = _catalogDao.value

  public override fun tx(): TxDao = _txDao.value

  public override fun reports(): ReportDao = _reportDao.value

  public override fun maintenance(): MaintenanceDao = _maintenanceDao.value

  public override fun expenses(): ExpenseDao = _expenseDao.value
}
