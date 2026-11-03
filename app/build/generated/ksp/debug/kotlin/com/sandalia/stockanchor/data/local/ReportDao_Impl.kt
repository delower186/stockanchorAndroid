package com.sandalia.stockanchor.`data`.local

import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ReportDao_Impl(
  __db: RoomDatabase,
) : ReportDao {
  private val __db: RoomDatabase
  init {
    this.__db = __db
  }

  public override fun periodTotals(from: Long, to: Long): Flow<PeriodTotals> {
    val _sql: String = """
        |SELECT COUNT(*) AS salesCount, COALESCE(SUM(total),0) AS revenue, COALESCE(SUM(totalCost),0) AS cost
        |           FROM sales WHERE status = 'completed' AND date BETWEEN ? AND ?
        """.trimMargin()
    return createFlow(__db, false, arrayOf("sales")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        _argIndex = 2
        _stmt.bindLong(_argIndex, to)
        val _columnIndexOfSalesCount: Int = 0
        val _columnIndexOfRevenue: Int = 1
        val _columnIndexOfCost: Int = 2
        val _result: PeriodTotals
        if (_stmt.step()) {
          val _tmpSalesCount: Int
          _tmpSalesCount = _stmt.getLong(_columnIndexOfSalesCount).toInt()
          val _tmpRevenue: Double
          _tmpRevenue = _stmt.getDouble(_columnIndexOfRevenue)
          val _tmpCost: Double
          _tmpCost = _stmt.getDouble(_columnIndexOfCost)
          _result = PeriodTotals(_tmpSalesCount,_tmpRevenue,_tmpCost)
        } else {
          error("The query result was empty, but expected a single row to return a NON-NULL object of type <com.sandalia.stockanchor.`data`.local.PeriodTotals>.")
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun purchaseTotal(from: Long, to: Long): Flow<Double> {
    val _sql: String = "SELECT COALESCE(SUM(total),0) FROM purchases WHERE date BETWEEN ? AND ?"
    return createFlow(__db, false, arrayOf("purchases")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        _argIndex = 2
        _stmt.bindLong(_argIndex, to)
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

  public override fun itemsSold(from: Long, to: Long): Flow<Double> {
    val _sql: String = """
        |SELECT COALESCE(SUM(i.quantity),0) FROM sale_items i JOIN sales s ON s.id = i.saleId
        |           WHERE s.status = 'completed' AND s.date BETWEEN ? AND ?
        """.trimMargin()
    return createFlow(__db, false, arrayOf("sale_items", "sales")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        _argIndex = 2
        _stmt.bindLong(_argIndex, to)
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

  public override fun stockIn(from: Long, to: Long): Flow<Double> {
    val _sql: String =
        "SELECT COALESCE(SUM(quantity),0) FROM stock_movements WHERE type IN ('purchase','import') AND date BETWEEN ? AND ?"
    return createFlow(__db, false, arrayOf("stock_movements")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        _argIndex = 2
        _stmt.bindLong(_argIndex, to)
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

  public override fun stockTotals(): Flow<StockTotals> {
    val _sql: String = """
        |SELECT COALESCE(SUM(s.quantity),0) AS quantity,
        |                  COALESCE(SUM(s.quantity * p.costPrice),0) AS costValue,
        |                  COALESCE(SUM(s.quantity * p.salePrice),0) AS retailValue
        |           FROM stocks s JOIN products p ON p.id = s.productId
        """.trimMargin()
    return createFlow(__db, false, arrayOf("stocks", "products")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfQuantity: Int = 0
        val _columnIndexOfCostValue: Int = 1
        val _columnIndexOfRetailValue: Int = 2
        val _result: StockTotals
        if (_stmt.step()) {
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpCostValue: Double
          _tmpCostValue = _stmt.getDouble(_columnIndexOfCostValue)
          val _tmpRetailValue: Double
          _tmpRetailValue = _stmt.getDouble(_columnIndexOfRetailValue)
          _result = StockTotals(_tmpQuantity,_tmpCostValue,_tmpRetailValue)
        } else {
          error("The query result was empty, but expected a single row to return a NON-NULL object of type <com.sandalia.stockanchor.`data`.local.StockTotals>.")
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun buckets(
    fmt: String,
    from: Long,
    to: Long,
  ): Flow<List<DayBucket>> {
    val _sql: String = """
        |SELECT strftime(?, date / 1000, 'unixepoch', 'localtime') AS bucket,
        |                  COALESCE(SUM(total),0) AS revenue, COALESCE(SUM(total - totalCost),0) AS profit
        |           FROM sales WHERE status = 'completed' AND date BETWEEN ? AND ?
        |           GROUP BY bucket ORDER BY bucket
        """.trimMargin()
    return createFlow(__db, false, arrayOf("sales")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, fmt)
        _argIndex = 2
        _stmt.bindLong(_argIndex, from)
        _argIndex = 3
        _stmt.bindLong(_argIndex, to)
        val _columnIndexOfBucket: Int = 0
        val _columnIndexOfRevenue: Int = 1
        val _columnIndexOfProfit: Int = 2
        val _result: MutableList<DayBucket> = mutableListOf()
        while (_stmt.step()) {
          val _item: DayBucket
          val _tmpBucket: String
          _tmpBucket = _stmt.getText(_columnIndexOfBucket)
          val _tmpRevenue: Double
          _tmpRevenue = _stmt.getDouble(_columnIndexOfRevenue)
          val _tmpProfit: Double
          _tmpProfit = _stmt.getDouble(_columnIndexOfProfit)
          _item = DayBucket(_tmpBucket,_tmpRevenue,_tmpProfit)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun productCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM products"
    return createFlow(__db, false, arrayOf("products")) { _connection ->
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

  public override fun expenseTotal(from: Long, to: Long): Flow<Double> {
    val _sql: String = "SELECT COALESCE(SUM(amount),0) FROM expenses WHERE date BETWEEN ? AND ?"
    return createFlow(__db, false, arrayOf("expenses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        _argIndex = 2
        _stmt.bindLong(_argIndex, to)
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
