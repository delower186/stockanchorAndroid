package com.sandalia.stockanchor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CategoryEntity::class, WarehouseEntity::class, SupplierEntity::class, ProductEntity::class, StockEntity::class,
        PurchaseEntity::class, PurchaseItemEntity::class, SaleEntity::class, SaleItemEntity::class,
        TransferEntity::class, MovementEntity::class, ExpenseEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalog(): CatalogDao
    abstract fun tx(): TxDao
    abstract fun reports(): ReportDao
    abstract fun maintenance(): MaintenanceDao
    abstract fun expenses(): ExpenseDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "stock_anchor.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Every install starts with one default warehouse so Basic mode just works.
                        db.execSQL("INSERT INTO warehouses (name, location, isDefault) VALUES ('Main Warehouse', NULL, 1)")
                    }
                })
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()

        /** Adds products.warehouseId (FK -> warehouses). SQLite can't add a FK in place, so the table is rebuilt. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `products_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                        "`sku` TEXT, `barcode` TEXT, `categoryId` INTEGER, `warehouseId` INTEGER, `unit` TEXT NOT NULL, " +
                        "`costPrice` REAL NOT NULL, `salePrice` REAL NOT NULL, `lowStockThreshold` REAL NOT NULL, `description` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`warehouseId`) REFERENCES `warehouses`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )"
                )
                // Existing products go to the warehouse holding most of their stock, otherwise the default warehouse.
                db.execSQL(
                    """INSERT INTO products_new (id, name, sku, barcode, categoryId, warehouseId, unit, costPrice, salePrice,
                                                lowStockThreshold, description, createdAt)
                       SELECT p.id, p.name, p.sku, p.barcode, p.categoryId,
                              COALESCE(
                                (SELECT s.warehouseId FROM stocks s WHERE s.productId = p.id ORDER BY s.quantity DESC LIMIT 1),
                                (SELECT w.id FROM warehouses w ORDER BY w.isDefault DESC, w.id LIMIT 1)),
                              p.unit, p.costPrice, p.salePrice, p.lowStockThreshold, p.description, p.createdAt
                       FROM products p"""
                )
                db.execSQL("DROP TABLE products")
                db.execSQL("ALTER TABLE products_new RENAME TO products")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_categoryId` ON `products` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_barcode` ON `products` (`barcode`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_sku` ON `products` (`sku`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_warehouseId` ON `products` (`warehouseId`)")
            }
        }

        /** Adds products.supplierId (FK -> suppliers), rebuilding the table like [MIGRATION_1_2]. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `products_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                        "`sku` TEXT, `barcode` TEXT, `categoryId` INTEGER, `warehouseId` INTEGER, `supplierId` INTEGER, `unit` TEXT NOT NULL, " +
                        "`costPrice` REAL NOT NULL, `salePrice` REAL NOT NULL, `lowStockThreshold` REAL NOT NULL, `description` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`warehouseId`) REFERENCES `warehouses`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , " +
                        "FOREIGN KEY(`supplierId`) REFERENCES `suppliers`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )"
                )
                db.execSQL(
                    """INSERT INTO products_new (id, name, sku, barcode, categoryId, warehouseId, supplierId, unit, costPrice, salePrice,
                                                lowStockThreshold, description, createdAt)
                       SELECT id, name, sku, barcode, categoryId, warehouseId, NULL, unit, costPrice, salePrice,
                              lowStockThreshold, description, createdAt
                       FROM products"""
                )
                db.execSQL("DROP TABLE products")
                db.execSQL("ALTER TABLE products_new RENAME TO products")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_categoryId` ON `products` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_barcode` ON `products` (`barcode`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_sku` ON `products` (`sku`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_warehouseId` ON `products` (`warehouseId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_supplierId` ON `products` (`supplierId`)")
            }
        }

        /** Adds the expenses table (net profit). */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, " +
                        "`amount` REAL NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `userName` TEXT)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)")
            }
        }
    }
}
