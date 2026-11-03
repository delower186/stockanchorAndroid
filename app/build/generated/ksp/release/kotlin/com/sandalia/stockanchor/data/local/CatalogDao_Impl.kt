package com.sandalia.stockanchor.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CatalogDao_Impl(
  __db: RoomDatabase,
) : CatalogDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfCategoryEntity: EntityUpsertAdapter<CategoryEntity>

  private val __upsertAdapterOfWarehouseEntity: EntityUpsertAdapter<WarehouseEntity>

  private val __upsertAdapterOfSupplierEntity: EntityUpsertAdapter<SupplierEntity>

  private val __upsertAdapterOfProductEntity: EntityUpsertAdapter<ProductEntity>

  private val __upsertAdapterOfStockEntity: EntityUpsertAdapter<StockEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfCategoryEntity = EntityUpsertAdapter<CategoryEntity>(object :
        EntityInsertAdapter<CategoryEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `categories` (`id`,`name`,`description`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpDescription)
        }
      }
    }, object : EntityDeleteOrUpdateAdapter<CategoryEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `categories` SET `id` = ?,`name` = ?,`description` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpDescription)
        }
        statement.bindLong(4, entity.id)
      }
    })
    this.__upsertAdapterOfWarehouseEntity = EntityUpsertAdapter<WarehouseEntity>(object :
        EntityInsertAdapter<WarehouseEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `warehouses` (`id`,`name`,`location`,`isDefault`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WarehouseEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpLocation: String? = entity.location
        if (_tmpLocation == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpLocation)
        }
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(4, _tmp.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<WarehouseEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `warehouses` SET `id` = ?,`name` = ?,`location` = ?,`isDefault` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: WarehouseEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpLocation: String? = entity.location
        if (_tmpLocation == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpLocation)
        }
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        statement.bindLong(5, entity.id)
      }
    })
    this.__upsertAdapterOfSupplierEntity = EntityUpsertAdapter<SupplierEntity>(object :
        EntityInsertAdapter<SupplierEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `suppliers` (`id`,`name`,`phone`,`email`,`address`,`note`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SupplierEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpPhone: String? = entity.phone
        if (_tmpPhone == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpPhone)
        }
        val _tmpEmail: String? = entity.email
        if (_tmpEmail == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpEmail)
        }
        val _tmpAddress: String? = entity.address
        if (_tmpAddress == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpAddress)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpNote)
        }
      }
    }, object : EntityDeleteOrUpdateAdapter<SupplierEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `suppliers` SET `id` = ?,`name` = ?,`phone` = ?,`email` = ?,`address` = ?,`note` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SupplierEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpPhone: String? = entity.phone
        if (_tmpPhone == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpPhone)
        }
        val _tmpEmail: String? = entity.email
        if (_tmpEmail == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpEmail)
        }
        val _tmpAddress: String? = entity.address
        if (_tmpAddress == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpAddress)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpNote)
        }
        statement.bindLong(7, entity.id)
      }
    })
    this.__upsertAdapterOfProductEntity = EntityUpsertAdapter<ProductEntity>(object :
        EntityInsertAdapter<ProductEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `products` (`id`,`name`,`sku`,`barcode`,`categoryId`,`warehouseId`,`supplierId`,`unit`,`costPrice`,`salePrice`,`lowStockThreshold`,`description`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProductEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpSku: String? = entity.sku
        if (_tmpSku == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpSku)
        }
        val _tmpBarcode: String? = entity.barcode
        if (_tmpBarcode == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpBarcode)
        }
        val _tmpCategoryId: Long? = entity.categoryId
        if (_tmpCategoryId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpCategoryId)
        }
        val _tmpWarehouseId: Long? = entity.warehouseId
        if (_tmpWarehouseId == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpWarehouseId)
        }
        val _tmpSupplierId: Long? = entity.supplierId
        if (_tmpSupplierId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpSupplierId)
        }
        statement.bindText(8, entity.unit)
        statement.bindDouble(9, entity.costPrice)
        statement.bindDouble(10, entity.salePrice)
        statement.bindDouble(11, entity.lowStockThreshold)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpDescription)
        }
        statement.bindLong(13, entity.createdAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<ProductEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `products` SET `id` = ?,`name` = ?,`sku` = ?,`barcode` = ?,`categoryId` = ?,`warehouseId` = ?,`supplierId` = ?,`unit` = ?,`costPrice` = ?,`salePrice` = ?,`lowStockThreshold` = ?,`description` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ProductEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpSku: String? = entity.sku
        if (_tmpSku == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpSku)
        }
        val _tmpBarcode: String? = entity.barcode
        if (_tmpBarcode == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpBarcode)
        }
        val _tmpCategoryId: Long? = entity.categoryId
        if (_tmpCategoryId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpCategoryId)
        }
        val _tmpWarehouseId: Long? = entity.warehouseId
        if (_tmpWarehouseId == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpWarehouseId)
        }
        val _tmpSupplierId: Long? = entity.supplierId
        if (_tmpSupplierId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpSupplierId)
        }
        statement.bindText(8, entity.unit)
        statement.bindDouble(9, entity.costPrice)
        statement.bindDouble(10, entity.salePrice)
        statement.bindDouble(11, entity.lowStockThreshold)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpDescription)
        }
        statement.bindLong(13, entity.createdAt)
        statement.bindLong(14, entity.id)
      }
    })
    this.__upsertAdapterOfStockEntity = EntityUpsertAdapter<StockEntity>(object :
        EntityInsertAdapter<StockEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `stocks` (`productId`,`warehouseId`,`quantity`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: StockEntity) {
        statement.bindLong(1, entity.productId)
        statement.bindLong(2, entity.warehouseId)
        statement.bindDouble(3, entity.quantity)
      }
    }, object : EntityDeleteOrUpdateAdapter<StockEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `stocks` SET `productId` = ?,`warehouseId` = ?,`quantity` = ? WHERE `productId` = ? AND `warehouseId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: StockEntity) {
        statement.bindLong(1, entity.productId)
        statement.bindLong(2, entity.warehouseId)
        statement.bindDouble(3, entity.quantity)
        statement.bindLong(4, entity.productId)
        statement.bindLong(5, entity.warehouseId)
      }
    })
  }

  public override suspend fun upsertCategory(c: CategoryEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __upsertAdapterOfCategoryEntity.upsertAndReturnId(_connection, c)
    _result
  }

  public override suspend fun upsertCategories(c: List<CategoryEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfCategoryEntity.upsert(_connection, c)
  }

  public override suspend fun upsertWarehouse(w: WarehouseEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __upsertAdapterOfWarehouseEntity.upsertAndReturnId(_connection, w)
    _result
  }

  public override suspend fun upsertWarehouses(w: List<WarehouseEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfWarehouseEntity.upsert(_connection, w)
  }

  public override suspend fun upsertSupplier(s: SupplierEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __upsertAdapterOfSupplierEntity.upsertAndReturnId(_connection, s)
    _result
  }

  public override suspend fun upsertSuppliers(s: List<SupplierEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfSupplierEntity.upsert(_connection, s)
  }

  public override suspend fun upsertProduct(p: ProductEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __upsertAdapterOfProductEntity.upsertAndReturnId(_connection, p)
    _result
  }

  public override suspend fun upsertProducts(p: List<ProductEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __upsertAdapterOfProductEntity.upsert(_connection, p)
  }

  public override suspend fun upsertStock(s: StockEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __upsertAdapterOfStockEntity.upsert(_connection, s)
  }

  public override suspend fun upsertStocks(s: List<StockEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __upsertAdapterOfStockEntity.upsert(_connection, s)
  }

  public override fun categoriesWithCount(): Flow<List<CategoryWithCount>> {
    val _sql: String = """
        |SELECT c.id, c.name, c.description,
        |           (SELECT COUNT(*) FROM products p WHERE p.categoryId = c.id) AS productCount
        |           FROM categories c ORDER BY c.name COLLATE NOCASE
        """.trimMargin()
    return createFlow(__db, false, arrayOf("products", "categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfName: Int = 1
        val _columnIndexOfDescription: Int = 2
        val _columnIndexOfProductCount: Int = 3
        val _result: MutableList<CategoryWithCount> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryWithCount
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpProductCount: Int
          _tmpProductCount = _stmt.getLong(_columnIndexOfProductCount).toInt()
          _item = CategoryWithCount(_tmpId,_tmpName,_tmpDescription,_tmpProductCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun categories(): Flow<List<CategoryEntity>> {
    val _sql: String = "SELECT * FROM categories ORDER BY name COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _result: MutableList<CategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          _item = CategoryEntity(_tmpId,_tmpName,_tmpDescription)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allCategories(): List<CategoryEntity> {
    val _sql: String = "SELECT * FROM categories"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _result: MutableList<CategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          _item = CategoryEntity(_tmpId,_tmpName,_tmpDescription)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun warehouses(): Flow<List<WarehouseEntity>> {
    val _sql: String = "SELECT * FROM warehouses ORDER BY isDefault DESC, name COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("warehouses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfLocation: Int = getColumnIndexOrThrow(_stmt, "location")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _result: MutableList<WarehouseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WarehouseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpLocation: String?
          if (_stmt.isNull(_columnIndexOfLocation)) {
            _tmpLocation = null
          } else {
            _tmpLocation = _stmt.getText(_columnIndexOfLocation)
          }
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          _item = WarehouseEntity(_tmpId,_tmpName,_tmpLocation,_tmpIsDefault)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allWarehouses(): List<WarehouseEntity> {
    val _sql: String = "SELECT * FROM warehouses ORDER BY isDefault DESC, id"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfLocation: Int = getColumnIndexOrThrow(_stmt, "location")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _result: MutableList<WarehouseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WarehouseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpLocation: String?
          if (_stmt.isNull(_columnIndexOfLocation)) {
            _tmpLocation = null
          } else {
            _tmpLocation = _stmt.getText(_columnIndexOfLocation)
          }
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          _item = WarehouseEntity(_tmpId,_tmpName,_tmpLocation,_tmpIsDefault)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun defaultWarehouse(): WarehouseEntity? {
    val _sql: String = "SELECT * FROM warehouses WHERE isDefault = 1 LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfLocation: Int = getColumnIndexOrThrow(_stmt, "location")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _result: WarehouseEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpLocation: String?
          if (_stmt.isNull(_columnIndexOfLocation)) {
            _tmpLocation = null
          } else {
            _tmpLocation = _stmt.getText(_columnIndexOfLocation)
          }
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          _result = WarehouseEntity(_tmpId,_tmpName,_tmpLocation,_tmpIsDefault)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun warehouseCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM warehouses"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun suppliers(): Flow<List<SupplierEntity>> {
    val _sql: String = "SELECT * FROM suppliers ORDER BY name COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("suppliers")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfAddress: Int = getColumnIndexOrThrow(_stmt, "address")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<SupplierEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SupplierEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPhone: String?
          if (_stmt.isNull(_columnIndexOfPhone)) {
            _tmpPhone = null
          } else {
            _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          }
          val _tmpEmail: String?
          if (_stmt.isNull(_columnIndexOfEmail)) {
            _tmpEmail = null
          } else {
            _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          }
          val _tmpAddress: String?
          if (_stmt.isNull(_columnIndexOfAddress)) {
            _tmpAddress = null
          } else {
            _tmpAddress = _stmt.getText(_columnIndexOfAddress)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _item = SupplierEntity(_tmpId,_tmpName,_tmpPhone,_tmpEmail,_tmpAddress,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allSuppliers(): List<SupplierEntity> {
    val _sql: String = "SELECT * FROM suppliers"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfAddress: Int = getColumnIndexOrThrow(_stmt, "address")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<SupplierEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SupplierEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPhone: String?
          if (_stmt.isNull(_columnIndexOfPhone)) {
            _tmpPhone = null
          } else {
            _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          }
          val _tmpEmail: String?
          if (_stmt.isNull(_columnIndexOfEmail)) {
            _tmpEmail = null
          } else {
            _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          }
          val _tmpAddress: String?
          if (_stmt.isNull(_columnIndexOfAddress)) {
            _tmpAddress = null
          } else {
            _tmpAddress = _stmt.getText(_columnIndexOfAddress)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _item = SupplierEntity(_tmpId,_tmpName,_tmpPhone,_tmpEmail,_tmpAddress,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun products(
    query: String,
    categoryId: Long?,
    warehouseId: Long?,
    supplierId: Long?,
  ): Flow<List<ProductWithStock>> {
    val _sql: String = """
        |SELECT p.id, p.name, p.sku, p.barcode, p.categoryId, c.name AS categoryName, p.warehouseId, w.name AS warehouseName, p.unit,
        |                  p.costPrice, p.salePrice, p.lowStockThreshold,
        |                  COALESCE((SELECT SUM(s.quantity) FROM stocks s
        |                            WHERE s.productId = p.id AND (? IS NULL OR s.warehouseId = ?)), 0) AS quantity,
        |                  (SELECT GROUP_CONCAT(w3.name, ', ') FROM stocks s3 JOIN warehouses w3 ON w3.id = s3.warehouseId
        |                    WHERE s3.productId = p.id AND s3.quantity > 0) AS stockLocations
        |           FROM products p LEFT JOIN categories c ON c.id = p.categoryId LEFT JOIN warehouses w ON w.id = p.warehouseId
        |           WHERE (? IS NULL OR p.categoryId = ?)
        |             AND (? IS NULL OR p.supplierId = ?)
        |             AND (? IS NULL OR p.warehouseId = ?
        |                  OR EXISTS (SELECT 1 FROM stocks s2 WHERE s2.productId = p.id AND s2.warehouseId = ? AND s2.quantity > 0))
        |             AND (? = '' OR p.name LIKE '%' || ? || '%' OR p.sku LIKE '%' || ? || '%' OR p.barcode = ?)
        |           ORDER BY p.name COLLATE NOCASE
        """.trimMargin()
    return createFlow(__db, false, arrayOf("stocks", "warehouses", "products", "categories")) {
        _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (warehouseId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, warehouseId)
        }
        _argIndex = 2
        if (warehouseId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, warehouseId)
        }
        _argIndex = 3
        if (categoryId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, categoryId)
        }
        _argIndex = 4
        if (categoryId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, categoryId)
        }
        _argIndex = 5
        if (supplierId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, supplierId)
        }
        _argIndex = 6
        if (supplierId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, supplierId)
        }
        _argIndex = 7
        if (warehouseId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, warehouseId)
        }
        _argIndex = 8
        if (warehouseId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, warehouseId)
        }
        _argIndex = 9
        if (warehouseId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, warehouseId)
        }
        _argIndex = 10
        _stmt.bindText(_argIndex, query)
        _argIndex = 11
        _stmt.bindText(_argIndex, query)
        _argIndex = 12
        _stmt.bindText(_argIndex, query)
        _argIndex = 13
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = 0
        val _columnIndexOfName: Int = 1
        val _columnIndexOfSku: Int = 2
        val _columnIndexOfBarcode: Int = 3
        val _columnIndexOfCategoryId: Int = 4
        val _columnIndexOfCategoryName: Int = 5
        val _columnIndexOfWarehouseId: Int = 6
        val _columnIndexOfWarehouseName: Int = 7
        val _columnIndexOfUnit: Int = 8
        val _columnIndexOfCostPrice: Int = 9
        val _columnIndexOfSalePrice: Int = 10
        val _columnIndexOfLowStockThreshold: Int = 11
        val _columnIndexOfQuantity: Int = 12
        val _columnIndexOfStockLocations: Int = 13
        val _result: MutableList<ProductWithStock> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProductWithStock
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSku: String?
          if (_stmt.isNull(_columnIndexOfSku)) {
            _tmpSku = null
          } else {
            _tmpSku = _stmt.getText(_columnIndexOfSku)
          }
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpCategoryName: String?
          if (_stmt.isNull(_columnIndexOfCategoryName)) {
            _tmpCategoryName = null
          } else {
            _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          }
          val _tmpWarehouseId: Long?
          if (_stmt.isNull(_columnIndexOfWarehouseId)) {
            _tmpWarehouseId = null
          } else {
            _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          }
          val _tmpWarehouseName: String?
          if (_stmt.isNull(_columnIndexOfWarehouseName)) {
            _tmpWarehouseName = null
          } else {
            _tmpWarehouseName = _stmt.getText(_columnIndexOfWarehouseName)
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpCostPrice: Double
          _tmpCostPrice = _stmt.getDouble(_columnIndexOfCostPrice)
          val _tmpSalePrice: Double
          _tmpSalePrice = _stmt.getDouble(_columnIndexOfSalePrice)
          val _tmpLowStockThreshold: Double
          _tmpLowStockThreshold = _stmt.getDouble(_columnIndexOfLowStockThreshold)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpStockLocations: String?
          if (_stmt.isNull(_columnIndexOfStockLocations)) {
            _tmpStockLocations = null
          } else {
            _tmpStockLocations = _stmt.getText(_columnIndexOfStockLocations)
          }
          _item =
              ProductWithStock(_tmpId,_tmpName,_tmpSku,_tmpBarcode,_tmpCategoryId,_tmpCategoryName,_tmpWarehouseId,_tmpWarehouseName,_tmpUnit,_tmpCostPrice,_tmpSalePrice,_tmpLowStockThreshold,_tmpQuantity,_tmpStockLocations)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun lowStock(): Flow<List<ProductWithStock>> {
    val _sql: String = """
        |SELECT * FROM (
        |             SELECT p.id, p.name, p.sku, p.barcode, p.categoryId, c.name AS categoryName, p.warehouseId, w.name AS warehouseName, p.unit,
        |                    p.costPrice, p.salePrice, p.lowStockThreshold,
        |                    COALESCE((SELECT SUM(s.quantity) FROM stocks s WHERE s.productId = p.id), 0) AS quantity,
        |                    NULL AS stockLocations
        |             FROM products p LEFT JOIN categories c ON c.id = p.categoryId LEFT JOIN warehouses w ON w.id = p.warehouseId)
        |           WHERE quantity <= lowStockThreshold ORDER BY quantity ASC
        """.trimMargin()
    return createFlow(__db, false, arrayOf("stocks", "products", "categories", "warehouses")) {
        _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSku: Int = getColumnIndexOrThrow(_stmt, "sku")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfWarehouseName: Int = getColumnIndexOrThrow(_stmt, "warehouseName")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfCostPrice: Int = getColumnIndexOrThrow(_stmt, "costPrice")
        val _columnIndexOfSalePrice: Int = getColumnIndexOrThrow(_stmt, "salePrice")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfStockLocations: Int = getColumnIndexOrThrow(_stmt, "stockLocations")
        val _result: MutableList<ProductWithStock> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProductWithStock
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSku: String?
          if (_stmt.isNull(_columnIndexOfSku)) {
            _tmpSku = null
          } else {
            _tmpSku = _stmt.getText(_columnIndexOfSku)
          }
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpCategoryName: String?
          if (_stmt.isNull(_columnIndexOfCategoryName)) {
            _tmpCategoryName = null
          } else {
            _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          }
          val _tmpWarehouseId: Long?
          if (_stmt.isNull(_columnIndexOfWarehouseId)) {
            _tmpWarehouseId = null
          } else {
            _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          }
          val _tmpWarehouseName: String?
          if (_stmt.isNull(_columnIndexOfWarehouseName)) {
            _tmpWarehouseName = null
          } else {
            _tmpWarehouseName = _stmt.getText(_columnIndexOfWarehouseName)
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpCostPrice: Double
          _tmpCostPrice = _stmt.getDouble(_columnIndexOfCostPrice)
          val _tmpSalePrice: Double
          _tmpSalePrice = _stmt.getDouble(_columnIndexOfSalePrice)
          val _tmpLowStockThreshold: Double
          _tmpLowStockThreshold = _stmt.getDouble(_columnIndexOfLowStockThreshold)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpStockLocations: String?
          if (_stmt.isNull(_columnIndexOfStockLocations)) {
            _tmpStockLocations = null
          } else {
            _tmpStockLocations = _stmt.getText(_columnIndexOfStockLocations)
          }
          _item =
              ProductWithStock(_tmpId,_tmpName,_tmpSku,_tmpBarcode,_tmpCategoryId,_tmpCategoryName,_tmpWarehouseId,_tmpWarehouseName,_tmpUnit,_tmpCostPrice,_tmpSalePrice,_tmpLowStockThreshold,_tmpQuantity,_tmpStockLocations)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun lowStockNow(): List<ProductWithStock> {
    val _sql: String = """
        |SELECT * FROM (
        |             SELECT p.id, p.name, p.sku, p.barcode, p.categoryId, NULL AS categoryName, p.warehouseId, NULL AS warehouseName, p.unit,
        |                    p.costPrice, p.salePrice, p.lowStockThreshold,
        |                    COALESCE((SELECT SUM(s.quantity) FROM stocks s WHERE s.productId = p.id), 0) AS quantity,
        |                    NULL AS stockLocations
        |             FROM products p)
        |           WHERE quantity <= lowStockThreshold
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSku: Int = getColumnIndexOrThrow(_stmt, "sku")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfWarehouseName: Int = getColumnIndexOrThrow(_stmt, "warehouseName")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfCostPrice: Int = getColumnIndexOrThrow(_stmt, "costPrice")
        val _columnIndexOfSalePrice: Int = getColumnIndexOrThrow(_stmt, "salePrice")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfStockLocations: Int = getColumnIndexOrThrow(_stmt, "stockLocations")
        val _result: MutableList<ProductWithStock> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProductWithStock
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSku: String?
          if (_stmt.isNull(_columnIndexOfSku)) {
            _tmpSku = null
          } else {
            _tmpSku = _stmt.getText(_columnIndexOfSku)
          }
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpCategoryName: String?
          if (_stmt.isNull(_columnIndexOfCategoryName)) {
            _tmpCategoryName = null
          } else {
            _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          }
          val _tmpWarehouseId: Long?
          if (_stmt.isNull(_columnIndexOfWarehouseId)) {
            _tmpWarehouseId = null
          } else {
            _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          }
          val _tmpWarehouseName: String?
          if (_stmt.isNull(_columnIndexOfWarehouseName)) {
            _tmpWarehouseName = null
          } else {
            _tmpWarehouseName = _stmt.getText(_columnIndexOfWarehouseName)
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpCostPrice: Double
          _tmpCostPrice = _stmt.getDouble(_columnIndexOfCostPrice)
          val _tmpSalePrice: Double
          _tmpSalePrice = _stmt.getDouble(_columnIndexOfSalePrice)
          val _tmpLowStockThreshold: Double
          _tmpLowStockThreshold = _stmt.getDouble(_columnIndexOfLowStockThreshold)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpStockLocations: String?
          if (_stmt.isNull(_columnIndexOfStockLocations)) {
            _tmpStockLocations = null
          } else {
            _tmpStockLocations = _stmt.getText(_columnIndexOfStockLocations)
          }
          _item =
              ProductWithStock(_tmpId,_tmpName,_tmpSku,_tmpBarcode,_tmpCategoryId,_tmpCategoryName,_tmpWarehouseId,_tmpWarehouseName,_tmpUnit,_tmpCostPrice,_tmpSalePrice,_tmpLowStockThreshold,_tmpQuantity,_tmpStockLocations)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun product(id: Long): ProductEntity? {
    val _sql: String = "SELECT * FROM products WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSku: Int = getColumnIndexOrThrow(_stmt, "sku")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfSupplierId: Int = getColumnIndexOrThrow(_stmt, "supplierId")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfCostPrice: Int = getColumnIndexOrThrow(_stmt, "costPrice")
        val _columnIndexOfSalePrice: Int = getColumnIndexOrThrow(_stmt, "salePrice")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ProductEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSku: String?
          if (_stmt.isNull(_columnIndexOfSku)) {
            _tmpSku = null
          } else {
            _tmpSku = _stmt.getText(_columnIndexOfSku)
          }
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpWarehouseId: Long?
          if (_stmt.isNull(_columnIndexOfWarehouseId)) {
            _tmpWarehouseId = null
          } else {
            _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          }
          val _tmpSupplierId: Long?
          if (_stmt.isNull(_columnIndexOfSupplierId)) {
            _tmpSupplierId = null
          } else {
            _tmpSupplierId = _stmt.getLong(_columnIndexOfSupplierId)
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpCostPrice: Double
          _tmpCostPrice = _stmt.getDouble(_columnIndexOfCostPrice)
          val _tmpSalePrice: Double
          _tmpSalePrice = _stmt.getDouble(_columnIndexOfSalePrice)
          val _tmpLowStockThreshold: Double
          _tmpLowStockThreshold = _stmt.getDouble(_columnIndexOfLowStockThreshold)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              ProductEntity(_tmpId,_tmpName,_tmpSku,_tmpBarcode,_tmpCategoryId,_tmpWarehouseId,_tmpSupplierId,_tmpUnit,_tmpCostPrice,_tmpSalePrice,_tmpLowStockThreshold,_tmpDescription,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun productFlow(id: Long): Flow<ProductEntity?> {
    val _sql: String = "SELECT * FROM products WHERE id = ?"
    return createFlow(__db, false, arrayOf("products")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSku: Int = getColumnIndexOrThrow(_stmt, "sku")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfSupplierId: Int = getColumnIndexOrThrow(_stmt, "supplierId")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfCostPrice: Int = getColumnIndexOrThrow(_stmt, "costPrice")
        val _columnIndexOfSalePrice: Int = getColumnIndexOrThrow(_stmt, "salePrice")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ProductEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSku: String?
          if (_stmt.isNull(_columnIndexOfSku)) {
            _tmpSku = null
          } else {
            _tmpSku = _stmt.getText(_columnIndexOfSku)
          }
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpWarehouseId: Long?
          if (_stmt.isNull(_columnIndexOfWarehouseId)) {
            _tmpWarehouseId = null
          } else {
            _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          }
          val _tmpSupplierId: Long?
          if (_stmt.isNull(_columnIndexOfSupplierId)) {
            _tmpSupplierId = null
          } else {
            _tmpSupplierId = _stmt.getLong(_columnIndexOfSupplierId)
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpCostPrice: Double
          _tmpCostPrice = _stmt.getDouble(_columnIndexOfCostPrice)
          val _tmpSalePrice: Double
          _tmpSalePrice = _stmt.getDouble(_columnIndexOfSalePrice)
          val _tmpLowStockThreshold: Double
          _tmpLowStockThreshold = _stmt.getDouble(_columnIndexOfLowStockThreshold)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              ProductEntity(_tmpId,_tmpName,_tmpSku,_tmpBarcode,_tmpCategoryId,_tmpWarehouseId,_tmpSupplierId,_tmpUnit,_tmpCostPrice,_tmpSalePrice,_tmpLowStockThreshold,_tmpDescription,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun productByCode(code: String): ProductEntity? {
    val _sql: String = "SELECT * FROM products WHERE barcode = ? OR sku = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, code)
        _argIndex = 2
        _stmt.bindText(_argIndex, code)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSku: Int = getColumnIndexOrThrow(_stmt, "sku")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfSupplierId: Int = getColumnIndexOrThrow(_stmt, "supplierId")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfCostPrice: Int = getColumnIndexOrThrow(_stmt, "costPrice")
        val _columnIndexOfSalePrice: Int = getColumnIndexOrThrow(_stmt, "salePrice")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ProductEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSku: String?
          if (_stmt.isNull(_columnIndexOfSku)) {
            _tmpSku = null
          } else {
            _tmpSku = _stmt.getText(_columnIndexOfSku)
          }
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpWarehouseId: Long?
          if (_stmt.isNull(_columnIndexOfWarehouseId)) {
            _tmpWarehouseId = null
          } else {
            _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          }
          val _tmpSupplierId: Long?
          if (_stmt.isNull(_columnIndexOfSupplierId)) {
            _tmpSupplierId = null
          } else {
            _tmpSupplierId = _stmt.getLong(_columnIndexOfSupplierId)
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpCostPrice: Double
          _tmpCostPrice = _stmt.getDouble(_columnIndexOfCostPrice)
          val _tmpSalePrice: Double
          _tmpSalePrice = _stmt.getDouble(_columnIndexOfSalePrice)
          val _tmpLowStockThreshold: Double
          _tmpLowStockThreshold = _stmt.getDouble(_columnIndexOfLowStockThreshold)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              ProductEntity(_tmpId,_tmpName,_tmpSku,_tmpBarcode,_tmpCategoryId,_tmpWarehouseId,_tmpSupplierId,_tmpUnit,_tmpCostPrice,_tmpSalePrice,_tmpLowStockThreshold,_tmpDescription,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun productByName(name: String): ProductEntity? {
    val _sql: String = "SELECT * FROM products WHERE name = ? COLLATE NOCASE LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSku: Int = getColumnIndexOrThrow(_stmt, "sku")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfSupplierId: Int = getColumnIndexOrThrow(_stmt, "supplierId")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfCostPrice: Int = getColumnIndexOrThrow(_stmt, "costPrice")
        val _columnIndexOfSalePrice: Int = getColumnIndexOrThrow(_stmt, "salePrice")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ProductEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSku: String?
          if (_stmt.isNull(_columnIndexOfSku)) {
            _tmpSku = null
          } else {
            _tmpSku = _stmt.getText(_columnIndexOfSku)
          }
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpWarehouseId: Long?
          if (_stmt.isNull(_columnIndexOfWarehouseId)) {
            _tmpWarehouseId = null
          } else {
            _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          }
          val _tmpSupplierId: Long?
          if (_stmt.isNull(_columnIndexOfSupplierId)) {
            _tmpSupplierId = null
          } else {
            _tmpSupplierId = _stmt.getLong(_columnIndexOfSupplierId)
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpCostPrice: Double
          _tmpCostPrice = _stmt.getDouble(_columnIndexOfCostPrice)
          val _tmpSalePrice: Double
          _tmpSalePrice = _stmt.getDouble(_columnIndexOfSalePrice)
          val _tmpLowStockThreshold: Double
          _tmpLowStockThreshold = _stmt.getDouble(_columnIndexOfLowStockThreshold)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              ProductEntity(_tmpId,_tmpName,_tmpSku,_tmpBarcode,_tmpCategoryId,_tmpWarehouseId,_tmpSupplierId,_tmpUnit,_tmpCostPrice,_tmpSalePrice,_tmpLowStockThreshold,_tmpDescription,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allProducts(): List<ProductEntity> {
    val _sql: String = "SELECT * FROM products"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSku: Int = getColumnIndexOrThrow(_stmt, "sku")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfSupplierId: Int = getColumnIndexOrThrow(_stmt, "supplierId")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfCostPrice: Int = getColumnIndexOrThrow(_stmt, "costPrice")
        val _columnIndexOfSalePrice: Int = getColumnIndexOrThrow(_stmt, "salePrice")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ProductEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProductEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSku: String?
          if (_stmt.isNull(_columnIndexOfSku)) {
            _tmpSku = null
          } else {
            _tmpSku = _stmt.getText(_columnIndexOfSku)
          }
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpWarehouseId: Long?
          if (_stmt.isNull(_columnIndexOfWarehouseId)) {
            _tmpWarehouseId = null
          } else {
            _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          }
          val _tmpSupplierId: Long?
          if (_stmt.isNull(_columnIndexOfSupplierId)) {
            _tmpSupplierId = null
          } else {
            _tmpSupplierId = _stmt.getLong(_columnIndexOfSupplierId)
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpCostPrice: Double
          _tmpCostPrice = _stmt.getDouble(_columnIndexOfCostPrice)
          val _tmpSalePrice: Double
          _tmpSalePrice = _stmt.getDouble(_columnIndexOfSalePrice)
          val _tmpLowStockThreshold: Double
          _tmpLowStockThreshold = _stmt.getDouble(_columnIndexOfLowStockThreshold)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              ProductEntity(_tmpId,_tmpName,_tmpSku,_tmpBarcode,_tmpCategoryId,_tmpWarehouseId,_tmpSupplierId,_tmpUnit,_tmpCostPrice,_tmpSalePrice,_tmpLowStockThreshold,_tmpDescription,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun stockForProduct(productId: Long): Flow<List<StockRow>> {
    val _sql: String = """
        |SELECT s.productId, s.warehouseId, w.name AS warehouseName, s.quantity
        |           FROM stocks s JOIN warehouses w ON w.id = s.warehouseId
        |           WHERE s.productId = ? ORDER BY w.isDefault DESC, w.name
        """.trimMargin()
    return createFlow(__db, false, arrayOf("stocks", "warehouses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, productId)
        val _columnIndexOfProductId: Int = 0
        val _columnIndexOfWarehouseId: Int = 1
        val _columnIndexOfWarehouseName: Int = 2
        val _columnIndexOfQuantity: Int = 3
        val _result: MutableList<StockRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: StockRow
          val _tmpProductId: Long
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId)
          val _tmpWarehouseId: Long
          _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          val _tmpWarehouseName: String
          _tmpWarehouseName = _stmt.getText(_columnIndexOfWarehouseName)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          _item = StockRow(_tmpProductId,_tmpWarehouseId,_tmpWarehouseName,_tmpQuantity)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun stock(productId: Long, warehouseId: Long): StockEntity? {
    val _sql: String = "SELECT * FROM stocks WHERE productId = ? AND warehouseId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, productId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, warehouseId)
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _result: StockEntity?
        if (_stmt.step()) {
          val _tmpProductId: Long
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId)
          val _tmpWarehouseId: Long
          _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          _result = StockEntity(_tmpProductId,_tmpWarehouseId,_tmpQuantity)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allStocks(): List<StockEntity> {
    val _sql: String = "SELECT * FROM stocks"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _result: MutableList<StockEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: StockEntity
          val _tmpProductId: Long
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId)
          val _tmpWarehouseId: Long
          _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          _item = StockEntity(_tmpProductId,_tmpWarehouseId,_tmpQuantity)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun warehouseQuantity(warehouseId: Long): Double {
    val _sql: String = "SELECT COALESCE(SUM(quantity),0) FROM stocks WHERE warehouseId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, warehouseId)
        val _result: Double
        if (_stmt.step()) {
          val _tmp: Double
          _tmp = _stmt.getDouble(0)
          _result = _tmp
        } else {
          _result = 0.0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCategory(id: Long) {
    val _sql: String = "DELETE FROM categories WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun detachCategory(id: Long) {
    val _sql: String = "UPDATE products SET categoryId = NULL WHERE categoryId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearDefaultExcept(keepId: Long) {
    val _sql: String = "UPDATE warehouses SET isDefault = 0 WHERE id != ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, keepId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteWarehouse(id: Long) {
    val _sql: String = "DELETE FROM warehouses WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSupplier(id: Long) {
    val _sql: String = "DELETE FROM suppliers WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteProduct(id: Long) {
    val _sql: String = "DELETE FROM products WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteProducts(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM products WHERE id IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Long in ids) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateCost(id: Long, cost: Double) {
    val _sql: String = "UPDATE products SET costPrice = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, cost)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteStocksFor(productIds: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM stocks WHERE productId IN (")
    val _inputSize: Int = productIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Long in productIds) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
