package com.sandalia.stockanchor.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.sandalia.stockanchor.data.local.SaleEntity
import com.sandalia.stockanchor.data.local.SaleItemEntity
import com.sandalia.stockanchor.data.local.SaleStatus
import java.io.File

/** Draws an A4 invoice with android.graphics.pdf (no third-party library). Premium feature. */
object InvoicePdf {
    private const val W = 595  // A4 @ 72dpi
    private const val H = 842
    private const val M = 40f

    private val navy = Color.parseColor("#0F2A44")
    private val teal = Color.parseColor("#0E9F8E")
    private val muted = Color.parseColor("#6B7A8C")
    private val line = Color.parseColor("#E3E8EF")

    fun create(
        context: Context, sale: SaleEntity, items: List<SaleItemEntity>, company: String, currency: String, warehouse: String?,
    ): File {
        val doc = PdfDocument()
        val rowH = 24f
        val perFirstPage = 20
        val perPage = 28
        val pages = if (items.size <= perFirstPage) 1 else 1 + ((items.size - perFirstPage + perPage - 1) / perPage)

        var index = 0
        for (pageNo in 1..pages) {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
            val c = page.canvas
            var y: Float

            if (pageNo == 1) {
                y = header(c, sale, company, warehouse)
            } else {
                y = M + 10
                c.drawText("${sale.invoiceNo} — continued", M, y, paint(11f, muted)); y += 20
            }
            y = tableHeader(c, y)

            val take = if (pageNo == 1) perFirstPage else perPage
            val slice = items.drop(index).take(take)
            slice.forEachIndexed { i, item ->
                if ((index + i) % 2 == 1) c.drawRect(M, y - 16, W - M, y + 8, fill(Color.parseColor("#F6F8FB")))
                c.drawText(ellipsize(item.productName, 34), M + 8, y, paint(10.5f, navy))
                c.drawText(Fmt.qty(item.quantity), 330f, y, paint(10.5f, navy, align = Paint.Align.RIGHT))
                c.drawText(Fmt.money(currency, item.unitPrice), 440f, y, paint(10.5f, navy, align = Paint.Align.RIGHT))
                c.drawText(Fmt.money(currency, item.quantity * item.unitPrice), W - M - 8, y, paint(10.5f, navy, bold = true, align = Paint.Align.RIGHT))
                y += rowH
            }
            index += slice.size

            if (pageNo == pages) totals(c, y + 6, sale, currency)
            footer(c, pageNo, pages)
            doc.finishPage(page)
        }

        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "${sale.invoiceNo}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /** Sends the file to another app. Returns false when no app can receive it. */
    fun share(context: Context, file: File, subject: String): Boolean {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeOf(file)
            putExtra(Intent.EXTRA_STREAM, uriOf(context, file))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return ShareIntents.start(context, intent)
    }

    /** Opens the file in a viewer (the viewer's own share button can send it on). Returns false when no viewer is installed. */
    fun open(context: Context, file: File): Boolean {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uriOf(context, file), mimeOf(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return ShareIntents.start(context, intent)
    }

    private fun uriOf(context: Context, file: File) = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    private fun mimeOf(file: File) = if (file.name.endsWith(".pdf")) "application/pdf" else "text/csv"

    private fun header(c: Canvas, sale: SaleEntity, company: String, warehouse: String?): Float {
        c.drawRect(0f, 0f, W.toFloat(), 120f, fill(navy))
        c.drawRect(0f, 120f, W.toFloat(), 124f, fill(teal))
        c.drawText(company, M, 58f, paint(22f, Color.WHITE, bold = true))
        warehouse?.let { c.drawText(it, M, 80f, paint(11f, Color.parseColor("#B8C7D9"))) }
        c.drawText("INVOICE", W - M, 58f, paint(26f, Color.WHITE, bold = true, align = Paint.Align.RIGHT))
        c.drawText(sale.invoiceNo, W - M, 80f, paint(11f, Color.parseColor("#B8C7D9"), align = Paint.Align.RIGHT))

        var y = 160f
        c.drawText("BILLED TO", M, y, paint(9f, muted, bold = true))
        c.drawText("DATE", 330f, y, paint(9f, muted, bold = true))
        c.drawText("STATUS", W - M, y, paint(9f, muted, bold = true, align = Paint.Align.RIGHT))
        y += 18
        c.drawText(sale.customerName, M, y, paint(13f, navy, bold = true))
        c.drawText(Fmt.dateTime(sale.date), 330f, y, paint(11f, navy))
        val cancelled = sale.status == SaleStatus.CANCELLED
        c.drawText(if (cancelled) "CANCELLED" else "PAID", W - M, y,
            paint(11f, if (cancelled) Color.parseColor("#C62828") else teal, bold = true, align = Paint.Align.RIGHT))
        sale.customerPhone?.let { y += 16; c.drawText(it, M, y, paint(11f, muted)) }
        return y + 34
    }

    private fun tableHeader(c: Canvas, top: Float): Float {
        c.drawRoundRect(RectF(M, top, W - M, top + 26), 4f, 4f, fill(Color.parseColor("#EAF6F4")))
        val y = top + 17
        c.drawText("ITEM", M + 8, y, paint(9.5f, navy, bold = true))
        c.drawText("QTY", 330f, y, paint(9.5f, navy, bold = true, align = Paint.Align.RIGHT))
        c.drawText("PRICE", 440f, y, paint(9.5f, navy, bold = true, align = Paint.Align.RIGHT))
        c.drawText("AMOUNT", W - M - 8, y, paint(9.5f, navy, bold = true, align = Paint.Align.RIGHT))
        return top + 48
    }

    private fun totals(c: Canvas, top: Float, sale: SaleEntity, currency: String) {
        var y = top
        c.drawLine(300f, y, W - M, y, stroke(line)); y += 22
        fun row(label: String, value: String, big: Boolean = false) {
            c.drawText(label, 320f, y, paint(if (big) 13f else 11f, if (big) navy else muted, bold = big))
            c.drawText(value, W - M - 8, y, paint(if (big) 15f else 11f, navy, bold = true, align = Paint.Align.RIGHT))
            y += if (big) 26 else 20
        }
        row("Subtotal", Fmt.money(currency, sale.subtotal))
        if (sale.discount > 0) row("Discount", "-" + Fmt.money(currency, sale.discount))
        c.drawRoundRect(RectF(310f, y - 4, W - M, y + 30), 6f, 6f, fill(navy))
        c.drawText("TOTAL", 322f, y + 18, paint(12f, Color.WHITE, bold = true))
        c.drawText(Fmt.money(currency, sale.total), W - M - 10, y + 19, paint(15f, Color.WHITE, bold = true, align = Paint.Align.RIGHT))
        y += 56
        sale.note?.let { c.drawText("Note: ${ellipsize(it, 90)}", M, y, paint(10f, muted)) }
    }

    private fun footer(c: Canvas, page: Int, pages: Int) {
        c.drawLine(M, H - 50f, W - M, H - 50f, stroke(line))
        c.drawText("Thank you for your business!", M, H - 32f, paint(10f, teal, bold = true))
        c.drawText("Generated by Stock Anchor  •  Page $page of $pages", W - M, H - 32f, paint(9f, muted, align = Paint.Align.RIGHT))
    }

    private fun paint(size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size; this.color = color; textAlign = align
        typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    private fun stroke(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.STROKE; strokeWidth = 1f }
    private fun ellipsize(s: String, max: Int) = if (s.length <= max) s else s.take(max - 1) + "…"

    /** Plain-text invoice (Basic plan share). */
    fun asText(sale: SaleEntity, items: List<SaleItemEntity>, company: String, currency: String): String = buildString {
        appendLine(company); appendLine("Invoice ${sale.invoiceNo}"); appendLine(Fmt.dateTime(sale.date))
        appendLine("Customer: ${sale.customerName}${sale.customerPhone?.let { " ($it)" } ?: ""}")
        appendLine("--------------------------------")
        items.forEach { appendLine("${it.productName}\n  ${Fmt.qty(it.quantity)} x ${Fmt.money(currency, it.unitPrice)} = ${Fmt.money(currency, it.quantity * it.unitPrice)}") }
        appendLine("--------------------------------")
        appendLine("Subtotal: ${Fmt.money(currency, sale.subtotal)}")
        if (sale.discount > 0) appendLine("Discount: -${Fmt.money(currency, sale.discount)}")
        appendLine("TOTAL: ${Fmt.money(currency, sale.total)}")
        if (sale.status == SaleStatus.CANCELLED) appendLine("** CANCELLED **")
        append("Thank you for your business!")
    }
}
