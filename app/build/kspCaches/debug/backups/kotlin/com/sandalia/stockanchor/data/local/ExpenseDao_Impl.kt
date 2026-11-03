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
public class ExpenseDao_Impl(
  __db: RoomDatabase,
) : ExpenseDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfExpenseEntity: EntityUpsertAdapter<ExpenseEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfExpenseEntity = EntityUpsertAdapter<ExpenseEntity>(object :
        EntityInsertAdapter<ExpenseEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `expenses` (`id`,`category`,`amount`,`date`,`note`,`userName`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExpenseEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.category)
        statement.bindDouble(3, entity.amount)
        statement.bindLong(4, entity.date)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpNote)
        }
        val _tmpUserName: String? = entity.userName
        if (_tmpUserName == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpUserName)
        }
      }
    }, object : EntityDeleteOrUpdateAdapter<ExpenseEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `expenses` SET `id` = ?,`category` = ?,`amount` = ?,`date` = ?,`note` = ?,`userName` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ExpenseEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.category)
        statement.bindDouble(3, entity.amount)
        statement.bindLong(4, entity.date)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpNote)
        }
        val _tmpUserName: String? = entity.userName
        if (_tmpUserName == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpUserName)
        }
        statement.bindLong(7, entity.id)
      }
    })
  }

  public override suspend fun upsert(e: ExpenseEntity): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __upsertAdapterOfExpenseEntity.upsertAndReturnId(_connection, e)
    _result
  }

  public override suspend fun upsertAll(e: List<ExpenseEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __upsertAdapterOfExpenseEntity.upsert(_connection, e)
  }

  public override fun expenses(from: Long, to: Long): Flow<List<ExpenseEntity>> {
    val _sql: String =
        "SELECT * FROM expenses WHERE date BETWEEN ? AND ? ORDER BY date DESC, id DESC"
    return createFlow(__db, false, arrayOf("expenses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        _argIndex = 2
        _stmt.bindLong(_argIndex, to)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfUserName: Int = getColumnIndexOrThrow(_stmt, "userName")
        val _result: MutableList<ExpenseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExpenseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
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
          _item = ExpenseEntity(_tmpId,_tmpCategory,_tmpAmount,_tmpDate,_tmpNote,_tmpUserName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun categoryTotals(from: Long, to: Long): Flow<List<ExpenseCategoryTotal>> {
    val _sql: String = """
        |SELECT category, SUM(amount) AS total, COUNT(*) AS count FROM expenses
        |           WHERE date BETWEEN ? AND ? GROUP BY category ORDER BY total DESC
        """.trimMargin()
    return createFlow(__db, false, arrayOf("expenses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        _argIndex = 2
        _stmt.bindLong(_argIndex, to)
        val _columnIndexOfCategory: Int = 0
        val _columnIndexOfTotal: Int = 1
        val _columnIndexOfCount: Int = 2
        val _result: MutableList<ExpenseCategoryTotal> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExpenseCategoryTotal
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpCount: Int
          _tmpCount = _stmt.getLong(_columnIndexOfCount).toInt()
          _item = ExpenseCategoryTotal(_tmpCategory,_tmpTotal,_tmpCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun usedCategories(): Flow<List<String>> {
    val _sql: String = "SELECT DISTINCT category FROM expenses ORDER BY category"
    return createFlow(__db, false, arrayOf("expenses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun all(): List<ExpenseEntity> {
    val _sql: String = "SELECT * FROM expenses"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfUserName: Int = getColumnIndexOrThrow(_stmt, "userName")
        val _result: MutableList<ExpenseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExpenseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
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
          _item = ExpenseEntity(_tmpId,_tmpCategory,_tmpAmount,_tmpDate,_tmpNote,_tmpUserName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM expenses WHERE id = ?"
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

  public override suspend fun deleteIds(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM expenses WHERE id IN (")
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

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM expenses"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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
