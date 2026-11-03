package com.sandalia.stockanchor.util

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.data.local.AppDatabase
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

enum class CsvExport(@StringRes val titleRes: Int, val fileName: String) {
    PRODUCTS(R.string.products, "products"),
    STOCK(R.string.stock_by_warehouse, "stock"),
    SALES(R.string.sales, "sales"),
    MOVEMENTS(R.string.stock_movements, "movements");

    val title: String get() = str(titleRes)
}

/** RFC-4180 CSV export/import over the Storage Access Framework (no storage permission needed). */
object Csv {

    private fun esc(v: Any?): String {
        val s = when (v) {
            null -> ""
            is Double -> if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
            else -> v.toString()
        }
        return if (s.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + s.replace("\"", "\"\"") + "\"" else s
    }

    suspend fun export(context: Context, db: AppDatabase, what: CsvExport, uri: Uri): Int {
        val catalog = db.catalog()
        val tx = db.tx()
        val products = catalog.allProducts().associateBy { it.id }
        val categories = catalog.allCategories().associateBy { it.id }
        val warehouses = catalog.allWarehouses().associateBy { it.id }
        val stocks = catalog.allStocks()

        val (header, rows) = when (what) {
            CsvExport.PRODUCTS -> listOf("name", "sku", "barcode", "category", "unit", "cost_price", "sale_price", "low_stock_threshold", "total_quantity") to
                products.values.sortedBy { it.name.lowercase() }.map { p ->
                    listOf(p.name, p.sku, p.barcode, p.categoryId?.let { categories[it]?.name }, p.unit, p.costPrice, p.salePrice,
                        p.lowStockThreshold, stocks.filter { it.productId == p.id }.sumOf { it.quantity })
                }
            CsvExport.STOCK -> listOf("name", "sku", "barcode", "warehouse", "quantity", "unit_cost") to
                stocks.mapNotNull { s ->
                    val p = products[s.productId] ?: return@mapNotNull null
                    listOf(p.name, p.sku, p.barcode, warehouses[s.warehouseId]?.name, s.quantity, p.costPrice)
                }
            CsvExport.SALES -> listOf("invoice_no", "date", "customer_name", "customer_phone", "status", "subtotal", "discount", "total", "cost", "profit") to
                tx.allSales().sortedByDescending { it.date }.map { s ->
                    listOf(s.invoiceNo, Fmt.dateTime(s.date), s.customerName, s.customerPhone, s.status, s.subtotal, s.discount, s.total,
                        s.totalCost, s.total - s.totalCost)
                }
            CsvExport.MOVEMENTS -> listOf("date", "product", "warehouse", "type", "quantity", "balance_after", "note", "user") to
                tx.allMovements().sortedByDescending { it.date }.map { m ->
                    listOf(Fmt.dateTime(m.date), products[m.productId]?.name, warehouses[m.warehouseId]?.name, m.type, m.quantity,
                        m.balanceAfter, m.note, m.userName)
                }
        }

        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            OutputStreamWriter(out, Charsets.UTF_8).use { w ->
                w.write("\uFEFF") // BOM so Excel opens UTF-8 correctly
                w.write(header.joinToString(",") + "\r\n")
                rows.forEach { r -> w.write(r.joinToString(",") { esc(it) } + "\r\n") }
            }
        } ?: error(str(R.string.could_not_open_the_file_for))
        return rows.size
    }

    /** Parses a CSV with a header row into maps keyed by lower-case header. */
    fun read(context: Context, uri: Uri): List<Map<String, String>> {
        val text = context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
        } ?: error(str(R.string.could_not_open_the_file))
        val records = parse(text.removePrefix("\uFEFF"))
        if (records.isEmpty()) return emptyList()
        val header = records.first().map { it.trim().lowercase() }
        return records.drop(1)
            .filter { r -> r.any { it.isNotBlank() } }
            .map { r -> header.mapIndexed { i, h -> h to (r.getOrNull(i)?.trim() ?: "") }.toMap() }
    }

    fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') { field.append('"'); i++ } else inQuotes = false
                } else field.append(c)
            } else when (c) {
                '"' -> inQuotes = true
                ',' -> { row.add(field.toString()); field.clear() }
                '\r' -> Unit
                '\n' -> { row.add(field.toString()); field.clear(); rows.add(row); row = mutableListOf() }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) { row.add(field.toString()); rows.add(row) }
        return rows
    }

    /** Template shared from the import screen. */
    const val IMPORT_TEMPLATE = "name,sku,barcode,category,warehouse,quantity,unit_cost,sale_price\r\n" +
        "USB-C Cable 1m,SKU-1001,8901000000011,Electronics,Main Warehouse,25,120,250\r\n"
}
