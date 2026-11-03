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
public class TxDao_Impl(
  __db: RoomDatabase,
) : TxDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPurchaseEntity: EntityInsertAdapter<PurchaseEntity>

  private val __insertAdapterOfPurchaseItemEntity: EntityInsertAdapter<PurchaseItemEntity>

  private val __insertAdapterOfSaleEntity: EntityInsertAdapter<SaleEntity>

  private val __insertAdapterOfSaleItemEntity: EntityInsertAdapter<SaleItemEntity>

  private val __insertAdapterOfTransferEntity: EntityInsertAdapter<TransferEntity>

  private val __insertAdapterOfMovementEntity: EntityInsertAdapter<MovementEntity>

  private val __upsertAdapterOfPurchaseEntity: EntityUpsertAdapter<PurchaseEntity>

  private val __upsertAdapterOfPurchaseItemEntity: EntityUpsertAdapter<PurchaseItemEntity>

  private val __upsertAdapterOfSaleEntity: EntityUpsertAdapter<SaleEntity>

  private val __upsertAdapterOfSaleItemEntity: EntityUpsertAdapter<SaleItemEntity>

  private val __upsertAdapterOfTransferEntity: EntityUpsertAdapter<TransferEntity>

  private val __upsertAdapterOfMovementEntity: EntityUpsertAdapter<MovementEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPurchaseEntity = object : EntityInsertAdapter<PurchaseEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `purchases` (`id`,`refNo`,`supplierId`,`warehouseId`,`date`,`total`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PurchaseEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.refNo)
        val _tmpSupplierId: Long? = entity.supplierId
        if (_tmpSupplierId == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpSupplierId)
        }
        statement.bindLong(4, entity.warehouseId)
        statement.bindLong(5, entity.date)
        statement.bindDouble(6, entity.total)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNote)
        }
      }
    }
    this.__insertAdapterOfPurchaseItemEntity = object : EntityInsertAdapter<PurchaseItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `purchase_items` (`id`,`purchaseId`,`productId`,`quantity`,`unitCost`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PurchaseItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.purchaseId)
        statement.bindLong(3, entity.productId)
        statement.bindDouble(4, entity.quantity)
        statement.bindDouble(5, entity.unitCost)
      }
    }
    this.__insertAdapterOfSaleEntity = object : EntityInsertAdapter<SaleEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `sales` (`id`,`invoiceNo`,`customerName`,`customerPhone`,`warehouseId`,`date`,`subtotal`,`discount`,`total`,`totalCost`,`status`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SaleEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.invoiceNo)
        statement.bindText(3, entity.customerName)
        val _tmpCustomerPhone: String? = entity.customerPhone
        if (_tmpCustomerPhone == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpCustomerPhone)
        }
        statement.bindLong(5, entity.warehouseId)
        statement.bindLong(6, entity.date)
        statement.bindDouble(7, entity.subtotal)
        statement.bindDouble(8, entity.discount)
        statement.bindDouble(9, entity.total)
        statement.bindDouble(10, entity.totalCost)
        statement.bindText(11, entity.status)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpNote)
        }
      }
    }
    this.__insertAdapterOfSaleItemEntity = object : EntityInsertAdapter<SaleItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `sale_items` (`id`,`saleId`,`productId`,`productName`,`quantity`,`unitPrice`,`unitCost`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SaleItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.saleId)
        statement.bindLong(3, entity.productId)
        statement.bindText(4, entity.productName)
        statement.bindDouble(5, entity.quantity)
        statement.bindDouble(6, entity.unitPrice)
        statement.bindDouble(7, entity.unitCost)
      }
    }
    this.__insertAdapterOfTransferEntity = object : EntityInsertAdapter<TransferEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `transfers` (`id`,`productId`,`fromWarehouseId`,`toWarehouseId`,`quantity`,`date`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TransferEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.productId)
        statement.bindLong(3, entity.fromWarehouseId)
        statement.bindLong(4, entity.toWarehouseId)
        statement.bindDouble(5, entity.quantity)
        statement.bindLong(6, entity.date)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNote)
        }
      }
    }
    this.__insertAdapterOfMovementEntity = object : EntityInsertAdapter<MovementEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `stock_movements` (`id`,`productId`,`warehouseId`,`type`,`quantity`,`balanceAfter`,`refType`,`refId`,`note`,`userName`,`date`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MovementEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.productId)
        statement.bindLong(3, entity.warehouseId)
        statement.bindText(4, entity.type)
        statement.bindDouble(5, entity.quantity)
        statement.bindDouble(6, entity.balanceAfter)
        val _tmpRefType: String? = entity.refType
        if (_tmpRefType == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpRefType)
        }
        val _tmpRefId: Long? = entity.refId
        if (_tmpRefId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpRefId)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpNote)
        }
        val _tmpUserName: String? = entity.userName
        if (_tmpUserName == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpUserName)
        }
        statement.bindLong(11, entity.date)
      }
    }
    this.__upsertAdapterOfPurchaseEntity = EntityUpsertAdapter<PurchaseEntity>(object :
        EntityInsertAdapter<PurchaseEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `purchases` (`id`,`refNo`,`supplierId`,`warehouseId`,`date`,`total`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PurchaseEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.refNo)
        val _tmpSupplierId: Long? = entity.supplierId
        if (_tmpSupplierId == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpSupplierId)
        }
        statement.bindLong(4, entity.warehouseId)
        statement.bindLong(5, entity.date)
        statement.bindDouble(6, entity.total)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNote)
        }
      }
    }, object : EntityDeleteOrUpdateAdapter<PurchaseEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `purchases` SET `id` = ?,`refNo` = ?,`supplierId` = ?,`warehouseId` = ?,`date` = ?,`total` = ?,`note` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PurchaseEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.refNo)
        val _tmpSupplierId: Long? = entity.supplierId
        if (_tmpSupplierId == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpSupplierId)
        }
        statement.bindLong(4, entity.warehouseId)
        statement.bindLong(5, entity.date)
        statement.bindDouble(6, entity.total)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNote)
        }
        statement.bindLong(8, entity.id)
      }
    })
    this.__upsertAdapterOfPurchaseItemEntity = EntityUpsertAdapter<PurchaseItemEntity>(object :
        EntityInsertAdapter<PurchaseItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `purchase_items` (`id`,`purchaseId`,`productId`,`quantity`,`unitCost`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PurchaseItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.purchaseId)
        statement.bindLong(3, entity.productId)
        statement.bindDouble(4, entity.quantity)
        statement.bindDouble(5, entity.unitCost)
      }
    }, object : EntityDeleteOrUpdateAdapter<PurchaseItemEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `purchase_items` SET `id` = ?,`purchaseId` = ?,`productId` = ?,`quantity` = ?,`unitCost` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PurchaseItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.purchaseId)
        statement.bindLong(3, entity.productId)
        statement.bindDouble(4, entity.quantity)
        statement.bindDouble(5, entity.unitCost)
        statement.bindLong(6, entity.id)
      }
    })
    this.__upsertAdapterOfSaleEntity = EntityUpsertAdapter<SaleEntity>(object :
        EntityInsertAdapter<SaleEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `sales` (`id`,`invoiceNo`,`customerName`,`customerPhone`,`warehouseId`,`date`,`subtotal`,`discount`,`total`,`totalCost`,`status`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SaleEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.invoiceNo)
        statement.bindText(3, entity.customerName)
        val _tmpCustomerPhone: String? = entity.customerPhone
        if (_tmpCustomerPhone == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpCustomerPhone)
        }
        statement.bindLong(5, entity.warehouseId)
        statement.bindLong(6, entity.date)
        statement.bindDouble(7, entity.subtotal)
        statement.bindDouble(8, entity.discount)
        statement.bindDouble(9, entity.total)
        statement.bindDouble(10, entity.totalCost)
        statement.bindText(11, entity.status)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpNote)
        }
      }
    }, object : EntityDeleteOrUpdateAdapter<SaleEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `sales` SET `id` = ?,`invoiceNo` = ?,`customerName` = ?,`customerPhone` = ?,`warehouseId` = ?,`date` = ?,`subtotal` = ?,`discount` = ?,`total` = ?,`totalCost` = ?,`status` = ?,`note` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SaleEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.invoiceNo)
        statement.bindText(3, entity.customerName)
        val _tmpCustomerPhone: String? = entity.customerPhone
        if (_tmpCustomerPhone == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpCustomerPhone)
        }
        statement.bindLong(5, entity.warehouseId)
        statement.bindLong(6, entity.date)
        statement.bindDouble(7, entity.subtotal)
        statement.bindDouble(8, entity.discount)
        statement.bindDouble(9, entity.total)
        statement.bindDouble(10, entity.totalCost)
        statement.bindText(11, entity.status)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpNote)
        }
        statement.bindLong(13, entity.id)
      }
    })
    this.__upsertAdapterOfSaleItemEntity = EntityUpsertAdapter<SaleItemEntity>(object :
        EntityInsertAdapter<SaleItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `sale_items` (`id`,`saleId`,`productId`,`productName`,`quantity`,`unitPrice`,`unitCost`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SaleItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.saleId)
        statement.bindLong(3, entity.productId)
        statement.bindText(4, entity.productName)
        statement.bindDouble(5, entity.quantity)
        statement.bindDouble(6, entity.unitPrice)
        statement.bindDouble(7, entity.unitCost)
      }
    }, object : EntityDeleteOrUpdateAdapter<SaleItemEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `sale_items` SET `id` = ?,`saleId` = ?,`productId` = ?,`productName` = ?,`quantity` = ?,`unitPrice` = ?,`unitCost` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SaleItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.saleId)
        statement.bindLong(3, entity.productId)
        statement.bindText(4, entity.productName)
        statement.bindDouble(5, entity.quantity)
        statement.bindDouble(6, entity.unitPrice)
        statement.bindDouble(7, entity.unitCost)
        statement.bindLong(8, entity.id)
      }
    })
    this.__upsertAdapterOfTransferEntity = EntityUpsertAdapter<TransferEntity>(object :
        EntityInsertAdapter<TransferEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `transfers` (`id`,`productId`,`fromWarehouseId`,`toWarehouseId`,`quantity`,`date`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TransferEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.productId)
        statement.bindLong(3, entity.fromWarehouseId)
        statement.bindLong(4, entity.toWarehouseId)
        statement.bindDouble(5, entity.quantity)
        statement.bindLong(6, entity.date)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNote)
        }
      }
    }, object : EntityDeleteOrUpdateAdapter<TransferEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `transfers` SET `id` = ?,`productId` = ?,`fromWarehouseId` = ?,`toWarehouseId` = ?,`quantity` = ?,`date` = ?,`note` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TransferEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.productId)
        statement.bindLong(3, entity.fromWarehouseId)
        statement.bindLong(4, entity.toWarehouseId)
        statement.bindDouble(5, entity.quantity)
        statement.bindLong(6, entity.date)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNote)
        }
        statement.bindLong(8, entity.id)
      }
    })
    this.__upsertAdapterOfMovementEntity = EntityUpsertAdapter<MovementEntity>(object :
        EntityInsertAdapter<MovementEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `stock_movements` (`id`,`productId`,`warehouseId`,`type`,`quantity`,`balanceAfter`,`refType`,`refId`,`note`,`userName`,`date`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MovementEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.productId)
        statement.bindLong(3, entity.warehouseId)
        statement.bindText(4, entity.type)
        statement.bindDouble(5, entity.quantity)
        statement.bindDouble(6, entity.balanceAfter)
        val _tmpRefType: String? = entity.refType
        if (_tmpRefType == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpRefType)
        }
        val _tmpRefId: Long? = entity.refId
        if (_tmpRefId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpRefId)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpNote)
        }
        val _tmpUserName: String? = entity.userName
        if (_tmpUserName == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpUserName)
        }
        statement.bindLong(11, entity.date)
      }
    }, object : EntityDeleteOrUpdateAdapter<MovementEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `stock_movements` SET `id` = ?,`productId` = ?,`warehouseId` = ?,`type` = ?,`quantity` = ?,`balanceAfter` = ?,`refType` = ?,`refId` = ?,`note` = ?,`userName` = ?,`date` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MovementEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.productId)
        statement.bindLong(3, entity.warehouseId)
        statement.bindText(4, entity.type)
        statement.bindDouble(5, entity.quantity)
        statement.bindDouble(6, entity.balanceAfter)
        val _tmpRefType: String? = entity.refType
        if (_tmpRefType == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpRefType)
        }
        val _tmpRefId: Long? = entity.refId
        if (_tmpRefId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpRefId)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpNote)
        }
        val _tmpUserName: String? = entity.userName
        if (_tmpUserName == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpUserName)
        }
        statement.bindLong(11, entity.date)
        statement.bindLong(12, entity.id)
      }
    })
  }

  public override suspend fun insertPurchase(p: PurchaseEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfPurchaseEntity.insertAndReturnId(_connection, p)
    _result
  }

  public override suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPurchaseItemEntity.insert(_connection, items)
  }

  public override suspend fun insertSale(s: SaleEntity): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __insertAdapterOfSaleEntity.insertAndReturnId(_connection, s)
    _result
  }

  public override suspend fun insertSaleItems(items: List<SaleItemEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSaleItemEntity.insert(_connection, items)
  }

  public override suspend fun insertTransfer(t: TransferEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfTransferEntity.insertAndReturnId(_connection, t)
    _result
  }

  public override suspend fun insertMovement(m: MovementEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfMovementEntity.insertAndReturnId(_connection, m)
    _result
  }

  public override suspend fun upsertPurchases(p: List<PurchaseEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfPurchaseEntity.upsert(_connection, p)
  }

  public override suspend fun upsertPurchaseItems(items: List<PurchaseItemEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfPurchaseItemEntity.upsert(_connection, items)
  }

  public override suspend fun upsertSales(s: List<SaleEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __upsertAdapterOfSaleEntity.upsert(_connection, s)
  }

  public override suspend fun upsertSaleItems(items: List<SaleItemEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfSaleItemEntity.upsert(_connection, items)
  }

  public override suspend fun upsertTransfers(t: List<TransferEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfTransferEntity.upsert(_connection, t)
  }

  public override suspend fun upsertMovements(m: List<MovementEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfMovementEntity.upsert(_connection, m)
  }

  public override suspend fun purchaseCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM purchases"
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

  public override suspend fun allPurchases(): List<PurchaseEntity> {
    val _sql: String = "SELECT * FROM purchases"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRefNo: Int = getColumnIndexOrThrow(_stmt, "refNo")
        val _columnIndexOfSupplierId: Int = getColumnIndexOrThrow(_stmt, "supplierId")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<PurchaseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PurchaseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRefNo: String
          _tmpRefNo = _stmt.getText(_columnIndexOfRefNo)
          val _tmpSupplierId: Long?
          if (_stmt.isNull(_columnIndexOfSupplierId)) {
            _tmpSupplierId = null
          } else {
            _tmpSupplierId = _stmt.getLong(_columnIndexOfSupplierId)
          }
          val _tmpWarehouseId: Long
          _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _item =
              PurchaseEntity(_tmpId,_tmpRefNo,_tmpSupplierId,_tmpWarehouseId,_tmpDate,_tmpTotal,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allPurchaseItems(): List<PurchaseItemEntity> {
    val _sql: String = "SELECT * FROM purchase_items"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPurchaseId: Int = getColumnIndexOrThrow(_stmt, "purchaseId")
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfUnitCost: Int = getColumnIndexOrThrow(_stmt, "unitCost")
        val _result: MutableList<PurchaseItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PurchaseItemEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpPurchaseId: Long
          _tmpPurchaseId = _stmt.getLong(_columnIndexOfPurchaseId)
          val _tmpProductId: Long
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpUnitCost: Double
          _tmpUnitCost = _stmt.getDouble(_columnIndexOfUnitCost)
          _item = PurchaseItemEntity(_tmpId,_tmpPurchaseId,_tmpProductId,_tmpQuantity,_tmpUnitCost)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun recentPurchases(): Flow<List<PurchaseRow>> {
    val _sql: String = """
        |SELECT pu.id, pu.refNo, su.name AS supplierName, w.name AS warehouseName, pu.date, pu.total,
        |                  (SELECT COUNT(*) FROM purchase_items i WHERE i.purchaseId = pu.id) AS itemCount
        |           FROM purchases pu LEFT JOIN suppliers su ON su.id = pu.supplierId
        |           LEFT JOIN warehouses w ON w.id = pu.warehouseId
        |           ORDER BY pu.date DESC LIMIT 200
        """.trimMargin()
    return createFlow(__db, false, arrayOf("purchase_items", "purchases", "suppliers",
        "warehouses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfRefNo: Int = 1
        val _columnIndexOfSupplierName: Int = 2
        val _columnIndexOfWarehouseName: Int = 3
        val _columnIndexOfDate: Int = 4
        val _columnIndexOfTotal: Int = 5
        val _columnIndexOfItemCount: Int = 6
        val _result: MutableList<PurchaseRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: PurchaseRow
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRefNo: String
          _tmpRefNo = _stmt.getText(_columnIndexOfRefNo)
          val _tmpSupplierName: String?
          if (_stmt.isNull(_columnIndexOfSupplierName)) {
            _tmpSupplierName = null
          } else {
            _tmpSupplierName = _stmt.getText(_columnIndexOfSupplierName)
          }
          val _tmpWarehouseName: String
          _tmpWarehouseName = _stmt.getText(_columnIndexOfWarehouseName)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpItemCount: Int
          _tmpItemCount = _stmt.getLong(_columnIndexOfItemCount).toInt()
          _item =
              PurchaseRow(_tmpId,_tmpRefNo,_tmpSupplierName,_tmpWarehouseName,_tmpDate,_tmpTotal,_tmpItemCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun saleCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM sales"
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

  public override suspend fun allSales(): List<SaleEntity> {
    val _sql: String = "SELECT * FROM sales"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfInvoiceNo: Int = getColumnIndexOrThrow(_stmt, "invoiceNo")
        val _columnIndexOfCustomerName: Int = getColumnIndexOrThrow(_stmt, "customerName")
        val _columnIndexOfCustomerPhone: Int = getColumnIndexOrThrow(_stmt, "customerPhone")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSubtotal: Int = getColumnIndexOrThrow(_stmt, "subtotal")
        val _columnIndexOfDiscount: Int = getColumnIndexOrThrow(_stmt, "discount")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfTotalCost: Int = getColumnIndexOrThrow(_stmt, "totalCost")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<SaleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SaleEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpInvoiceNo: String
          _tmpInvoiceNo = _stmt.getText(_columnIndexOfInvoiceNo)
          val _tmpCustomerName: String
          _tmpCustomerName = _stmt.getText(_columnIndexOfCustomerName)
          val _tmpCustomerPhone: String?
          if (_stmt.isNull(_columnIndexOfCustomerPhone)) {
            _tmpCustomerPhone = null
          } else {
            _tmpCustomerPhone = _stmt.getText(_columnIndexOfCustomerPhone)
          }
          val _tmpWarehouseId: Long
          _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpSubtotal: Double
          _tmpSubtotal = _stmt.getDouble(_columnIndexOfSubtotal)
          val _tmpDiscount: Double
          _tmpDiscount = _stmt.getDouble(_columnIndexOfDiscount)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpTotalCost: Double
          _tmpTotalCost = _stmt.getDouble(_columnIndexOfTotalCost)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _item =
              SaleEntity(_tmpId,_tmpInvoiceNo,_tmpCustomerName,_tmpCustomerPhone,_tmpWarehouseId,_tmpDate,_tmpSubtotal,_tmpDiscount,_tmpTotal,_tmpTotalCost,_tmpStatus,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allSaleItems(): List<SaleItemEntity> {
    val _sql: String = "SELECT * FROM sale_items"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSaleId: Int = getColumnIndexOrThrow(_stmt, "saleId")
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfProductName: Int = getColumnIndexOrThrow(_stmt, "productName")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfUnitPrice: Int = getColumnIndexOrThrow(_stmt, "unitPrice")
        val _columnIndexOfUnitCost: Int = getColumnIndexOrThrow(_stmt, "unitCost")
        val _result: MutableList<SaleItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SaleItemEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSaleId: Long
          _tmpSaleId = _stmt.getLong(_columnIndexOfSaleId)
          val _tmpProductId: Long
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId)
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpUnitPrice: Double
          _tmpUnitPrice = _stmt.getDouble(_columnIndexOfUnitPrice)
          val _tmpUnitCost: Double
          _tmpUnitCost = _stmt.getDouble(_columnIndexOfUnitCost)
          _item =
              SaleItemEntity(_tmpId,_tmpSaleId,_tmpProductId,_tmpProductName,_tmpQuantity,_tmpUnitPrice,_tmpUnitCost)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun sales(
    query: String,
    from: Long,
    to: Long,
    limit: Int,
  ): Flow<List<SaleEntity>> {
    val _sql: String = """
        |SELECT * FROM sales
        |           WHERE (? = '' OR customerName LIKE '%' || ? || '%' OR invoiceNo LIKE '%' || ? || '%'
        |                  OR customerPhone LIKE '%' || ? || '%')
        |             AND date BETWEEN ? AND ?
        |           ORDER BY date DESC LIMIT ?
        """.trimMargin()
    return createFlow(__db, false, arrayOf("sales")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        _argIndex = 2
        _stmt.bindText(_argIndex, query)
        _argIndex = 3
        _stmt.bindText(_argIndex, query)
        _argIndex = 4
        _stmt.bindText(_argIndex, query)
        _argIndex = 5
        _stmt.bindLong(_argIndex, from)
        _argIndex = 6
        _stmt.bindLong(_argIndex, to)
        _argIndex = 7
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfInvoiceNo: Int = getColumnIndexOrThrow(_stmt, "invoiceNo")
        val _columnIndexOfCustomerName: Int = getColumnIndexOrThrow(_stmt, "customerName")
        val _columnIndexOfCustomerPhone: Int = getColumnIndexOrThrow(_stmt, "customerPhone")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSubtotal: Int = getColumnIndexOrThrow(_stmt, "subtotal")
        val _columnIndexOfDiscount: Int = getColumnIndexOrThrow(_stmt, "discount")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfTotalCost: Int = getColumnIndexOrThrow(_stmt, "totalCost")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<SaleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SaleEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpInvoiceNo: String
          _tmpInvoiceNo = _stmt.getText(_columnIndexOfInvoiceNo)
          val _tmpCustomerName: String
          _tmpCustomerName = _stmt.getText(_columnIndexOfCustomerName)
          val _tmpCustomerPhone: String?
          if (_stmt.isNull(_columnIndexOfCustomerPhone)) {
            _tmpCustomerPhone = null
          } else {
            _tmpCustomerPhone = _stmt.getText(_columnIndexOfCustomerPhone)
          }
          val _tmpWarehouseId: Long
          _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpSubtotal: Double
          _tmpSubtotal = _stmt.getDouble(_columnIndexOfSubtotal)
          val _tmpDiscount: Double
          _tmpDiscount = _stmt.getDouble(_columnIndexOfDiscount)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpTotalCost: Double
          _tmpTotalCost = _stmt.getDouble(_columnIndexOfTotalCost)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _item =
              SaleEntity(_tmpId,_tmpInvoiceNo,_tmpCustomerName,_tmpCustomerPhone,_tmpWarehouseId,_tmpDate,_tmpSubtotal,_tmpDiscount,_tmpTotal,_tmpTotalCost,_tmpStatus,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun saleFlow(id: Long): Flow<SaleEntity?> {
    val _sql: String = "SELECT * FROM sales WHERE id = ?"
    return createFlow(__db, false, arrayOf("sales")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfInvoiceNo: Int = getColumnIndexOrThrow(_stmt, "invoiceNo")
        val _columnIndexOfCustomerName: Int = getColumnIndexOrThrow(_stmt, "customerName")
        val _columnIndexOfCustomerPhone: Int = getColumnIndexOrThrow(_stmt, "customerPhone")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSubtotal: Int = getColumnIndexOrThrow(_stmt, "subtotal")
        val _columnIndexOfDiscount: Int = getColumnIndexOrThrow(_stmt, "discount")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfTotalCost: Int = getColumnIndexOrThrow(_stmt, "totalCost")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: SaleEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpInvoiceNo: String
          _tmpInvoiceNo = _stmt.getText(_columnIndexOfInvoiceNo)
          val _tmpCustomerName: String
          _tmpCustomerName = _stmt.getText(_columnIndexOfCustomerName)
          val _tmpCustomerPhone: String?
          if (_stmt.isNull(_columnIndexOfCustomerPhone)) {
            _tmpCustomerPhone = null
          } else {
            _tmpCustomerPhone = _stmt.getText(_columnIndexOfCustomerPhone)
          }
          val _tmpWarehouseId: Long
          _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpSubtotal: Double
          _tmpSubtotal = _stmt.getDouble(_columnIndexOfSubtotal)
          val _tmpDiscount: Double
          _tmpDiscount = _stmt.getDouble(_columnIndexOfDiscount)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpTotalCost: Double
          _tmpTotalCost = _stmt.getDouble(_columnIndexOfTotalCost)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _result =
              SaleEntity(_tmpId,_tmpInvoiceNo,_tmpCustomerName,_tmpCustomerPhone,_tmpWarehouseId,_tmpDate,_tmpSubtotal,_tmpDiscount,_tmpTotal,_tmpTotalCost,_tmpStatus,_tmpNote)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun sale(id: Long): SaleEntity? {
    val _sql: String = "SELECT * FROM sales WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfInvoiceNo: Int = getColumnIndexOrThrow(_stmt, "invoiceNo")
        val _columnIndexOfCustomerName: Int = getColumnIndexOrThrow(_stmt, "customerName")
        val _columnIndexOfCustomerPhone: Int = getColumnIndexOrThrow(_stmt, "customerPhone")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSubtotal: Int = getColumnIndexOrThrow(_stmt, "subtotal")
        val _columnIndexOfDiscount: Int = getColumnIndexOrThrow(_stmt, "discount")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfTotalCost: Int = getColumnIndexOrThrow(_stmt, "totalCost")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: SaleEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpInvoiceNo: String
          _tmpInvoiceNo = _stmt.getText(_columnIndexOfInvoiceNo)
          val _tmpCustomerName: String
          _tmpCustomerName = _stmt.getText(_columnIndexOfCustomerName)
          val _tmpCustomerPhone: String?
          if (_stmt.isNull(_columnIndexOfCustomerPhone)) {
            _tmpCustomerPhone = null
          } else {
            _tmpCustomerPhone = _stmt.getText(_columnIndexOfCustomerPhone)
          }
          val _tmpWarehouseId: Long
          _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpSubtotal: Double
          _tmpSubtotal = _stmt.getDouble(_columnIndexOfSubtotal)
          val _tmpDiscount: Double
          _tmpDiscount = _stmt.getDouble(_columnIndexOfDiscount)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpTotalCost: Double
          _tmpTotalCost = _stmt.getDouble(_columnIndexOfTotalCost)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _result =
              SaleEntity(_tmpId,_tmpInvoiceNo,_tmpCustomerName,_tmpCustomerPhone,_tmpWarehouseId,_tmpDate,_tmpSubtotal,_tmpDiscount,_tmpTotal,_tmpTotalCost,_tmpStatus,_tmpNote)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun saleItemsFlow(saleId: Long): Flow<List<SaleItemEntity>> {
    val _sql: String = "SELECT * FROM sale_items WHERE saleId = ?"
    return createFlow(__db, false, arrayOf("sale_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, saleId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSaleId: Int = getColumnIndexOrThrow(_stmt, "saleId")
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfProductName: Int = getColumnIndexOrThrow(_stmt, "productName")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfUnitPrice: Int = getColumnIndexOrThrow(_stmt, "unitPrice")
        val _columnIndexOfUnitCost: Int = getColumnIndexOrThrow(_stmt, "unitCost")
        val _result: MutableList<SaleItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SaleItemEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSaleId: Long
          _tmpSaleId = _stmt.getLong(_columnIndexOfSaleId)
          val _tmpProductId: Long
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId)
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpUnitPrice: Double
          _tmpUnitPrice = _stmt.getDouble(_columnIndexOfUnitPrice)
          val _tmpUnitCost: Double
          _tmpUnitCost = _stmt.getDouble(_columnIndexOfUnitCost)
          _item =
              SaleItemEntity(_tmpId,_tmpSaleId,_tmpProductId,_tmpProductName,_tmpQuantity,_tmpUnitPrice,_tmpUnitCost)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun saleItems(saleId: Long): List<SaleItemEntity> {
    val _sql: String = "SELECT * FROM sale_items WHERE saleId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, saleId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSaleId: Int = getColumnIndexOrThrow(_stmt, "saleId")
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfProductName: Int = getColumnIndexOrThrow(_stmt, "productName")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfUnitPrice: Int = getColumnIndexOrThrow(_stmt, "unitPrice")
        val _columnIndexOfUnitCost: Int = getColumnIndexOrThrow(_stmt, "unitCost")
        val _result: MutableList<SaleItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SaleItemEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSaleId: Long
          _tmpSaleId = _stmt.getLong(_columnIndexOfSaleId)
          val _tmpProductId: Long
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId)
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpUnitPrice: Double
          _tmpUnitPrice = _stmt.getDouble(_columnIndexOfUnitPrice)
          val _tmpUnitCost: Double
          _tmpUnitCost = _stmt.getDouble(_columnIndexOfUnitCost)
          _item =
              SaleItemEntity(_tmpId,_tmpSaleId,_tmpProductId,_tmpProductName,_tmpQuantity,_tmpUnitPrice,_tmpUnitCost)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allTransfers(): List<TransferEntity> {
    val _sql: String = "SELECT * FROM transfers"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfFromWarehouseId: Int = getColumnIndexOrThrow(_stmt, "fromWarehouseId")
        val _columnIndexOfToWarehouseId: Int = getColumnIndexOrThrow(_stmt, "toWarehouseId")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<TransferEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransferEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpProductId: Long
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId)
          val _tmpFromWarehouseId: Long
          _tmpFromWarehouseId = _stmt.getLong(_columnIndexOfFromWarehouseId)
          val _tmpToWarehouseId: Long
          _tmpToWarehouseId = _stmt.getLong(_columnIndexOfToWarehouseId)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _item =
              TransferEntity(_tmpId,_tmpProductId,_tmpFromWarehouseId,_tmpToWarehouseId,_tmpQuantity,_tmpDate,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun recentTransfers(): Flow<List<TransferRow>> {
    val _sql: String = """
        |SELECT t.id, p.name AS productName, p.unit, wf.name AS fromName, wt.name AS toName, t.quantity, t.date, t.note
        |           FROM transfers t JOIN products p ON p.id = t.productId
        |           JOIN warehouses wf ON wf.id = t.fromWarehouseId JOIN warehouses wt ON wt.id = t.toWarehouseId
        |           ORDER BY t.date DESC LIMIT 200
        """.trimMargin()
    return createFlow(__db, false, arrayOf("transfers", "products", "warehouses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfProductName: Int = 1
        val _columnIndexOfUnit: Int = 2
        val _columnIndexOfFromName: Int = 3
        val _columnIndexOfToName: Int = 4
        val _columnIndexOfQuantity: Int = 5
        val _columnIndexOfDate: Int = 6
        val _columnIndexOfNote: Int = 7
        val _result: MutableList<TransferRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransferRow
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpFromName: String
          _tmpFromName = _stmt.getText(_columnIndexOfFromName)
          val _tmpToName: String
          _tmpToName = _stmt.getText(_columnIndexOfToName)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _item =
              TransferRow(_tmpId,_tmpProductName,_tmpUnit,_tmpFromName,_tmpToName,_tmpQuantity,_tmpDate,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allMovements(): List<MovementEntity> {
    val _sql: String = "SELECT * FROM stock_movements"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfWarehouseId: Int = getColumnIndexOrThrow(_stmt, "warehouseId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfBalanceAfter: Int = getColumnIndexOrThrow(_stmt, "balanceAfter")
        val _columnIndexOfRefType: Int = getColumnIndexOrThrow(_stmt, "refType")
        val _columnIndexOfRefId: Int = getColumnIndexOrThrow(_stmt, "refId")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfUserName: Int = getColumnIndexOrThrow(_stmt, "userName")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _result: MutableList<MovementEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MovementEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpProductId: Long
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId)
          val _tmpWarehouseId: Long
          _tmpWarehouseId = _stmt.getLong(_columnIndexOfWarehouseId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpBalanceAfter: Double
          _tmpBalanceAfter = _stmt.getDouble(_columnIndexOfBalanceAfter)
          val _tmpRefType: String?
          if (_stmt.isNull(_columnIndexOfRefType)) {
            _tmpRefType = null
          } else {
            _tmpRefType = _stmt.getText(_columnIndexOfRefType)
          }
          val _tmpRefId: Long?
          if (_stmt.isNull(_columnIndexOfRefId)) {
            _tmpRefId = null
          } else {
            _tmpRefId = _stmt.getLong(_columnIndexOfRefId)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpUserName: String?
          if (_stmt.isNull(_columnIndexOfUserName)) {
            _tmpUserName = null
          } else {
            _tmpUserName = _stmt.getText(_columnIndexOfUserName)
          }
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          _item =
              MovementEntity(_tmpId,_tmpProductId,_tmpWarehouseId,_tmpType,_tmpQuantity,_tmpBalanceAfter,_tmpRefType,_tmpRefId,_tmpNote,_tmpUserName,_tmpDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun movements(
    productId: Long?,
    warehouseId: Long?,
    type: String?,
  ): Flow<List<MovementRow>> {
    val _sql: String = """
        |SELECT m.id, p.name AS productName, p.unit, w.name AS warehouseName, m.type, m.quantity, m.balanceAfter,
        |                  m.note, m.userName, m.date
        |           FROM stock_movements m JOIN products p ON p.id = m.productId JOIN warehouses w ON w.id = m.warehouseId
        |           WHERE (? IS NULL OR m.productId = ?)
        |             AND (? IS NULL OR m.warehouseId = ?)
        |             AND (? IS NULL OR m.type = ?)
        |           ORDER BY m.date DESC, m.id DESC LIMIT 500
        """.trimMargin()
    return createFlow(__db, false, arrayOf("stock_movements", "products", "warehouses")) {
        _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (productId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, productId)
        }
        _argIndex = 2
        if (productId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, productId)
        }
        _argIndex = 3
        if (warehouseId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, warehouseId)
        }
        _argIndex = 4
        if (warehouseId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, warehouseId)
        }
        _argIndex = 5
        if (type == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, type)
        }
        _argIndex = 6
        if (type == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, type)
        }
        val _columnIndexOfId: Int = 0
        val _columnIndexOfProductName: Int = 1
        val _columnIndexOfUnit: Int = 2
        val _columnIndexOfWarehouseName: Int = 3
        val _columnIndexOfType: Int = 4
        val _columnIndexOfQuantity: Int = 5
        val _columnIndexOfBalanceAfter: Int = 6
        val _columnIndexOfNote: Int = 7
        val _columnIndexOfUserName: Int = 8
        val _columnIndexOfDate: Int = 9
        val _result: MutableList<MovementRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: MovementRow
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpWarehouseName: String
          _tmpWarehouseName = _stmt.getText(_columnIndexOfWarehouseName)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpBalanceAfter: Double
          _tmpBalanceAfter = _stmt.getDouble(_columnIndexOfBalanceAfter)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpUserName: String?
          if (_stmt.isNull(_columnIndexOfUserName)) {
            _tmpUserName = null
          } else {
            _tmpUserName = _stmt.getText(_columnIndexOfUserName)
          }
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          _item =
              MovementRow(_tmpId,_tmpProductName,_tmpUnit,_tmpWarehouseName,_tmpType,_tmpQuantity,_tmpBalanceAfter,_tmpNote,_tmpUserName,_tmpDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePurchaseItemsFor(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM purchase_items WHERE purchaseId IN (")
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

  public override suspend fun deleteSaleItemsFor(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM sale_items WHERE saleId IN (")
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

  public override suspend fun updateSaleStatus(
    id: Long,
    status: String,
    note: String?,
  ) {
    val _sql: String = "UPDATE sales SET status = ?, note = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        if (note == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, note)
        }
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
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
