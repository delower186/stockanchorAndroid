package com.sandalia.stockanchor.domain
import androidx.annotation.StringRes
import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.plural
import com.sandalia.stockanchor.util.str

/** Permission keys — must match App\Support\Permissions on the server. */
object Perm {
    const val CATALOG_MANAGE = "catalog.manage"
    const val PURCHASES_CREATE = "purchases.create"
    const val SALES_CREATE = "sales.create"
    const val SALES_MANAGE = "sales.manage"
    const val TRANSFERS_CREATE = "transfers.create"
    const val STOCK_ADJUST = "stock.adjust"
    const val WAREHOUSES_MANAGE = "warehouses.manage"
    const val SUPPLIERS_MANAGE = "suppliers.manage"
    const val REPORTS_VIEW = "reports.view"
    const val DATA_EXPORT = "data.export"
    const val DATA_IMPORT = "data.import"
    const val TEAM_MANAGE = "team.manage"
    const val BACKUPS_MANAGE = "backups.manage"
    const val EXPENSES_MANAGE = "expenses.manage"

    val ALL = listOf(
        CATALOG_MANAGE, PURCHASES_CREATE, SALES_CREATE, SALES_MANAGE, TRANSFERS_CREATE, STOCK_ADJUST,
        WAREHOUSES_MANAGE, SUPPLIERS_MANAGE, REPORTS_VIEW, DATA_EXPORT, DATA_IMPORT, TEAM_MANAGE, BACKUPS_MANAGE,
        EXPENSES_MANAGE,
    )

    fun label(p: String): String = when (p) {
        CATALOG_MANAGE -> str(R.string.manage_products_categories)
        PURCHASES_CREATE -> str(R.string.record_purchases)
        SALES_CREATE -> str(R.string.make_sales)
        SALES_MANAGE -> str(R.string.manage_cancel_sales)
        TRANSFERS_CREATE -> str(R.string.transfer_stock)
        STOCK_ADJUST -> str(R.string.adjust_stock_counts)
        WAREHOUSES_MANAGE -> str(R.string.manage_warehouses)
        SUPPLIERS_MANAGE -> str(R.string.manage_suppliers)
        REPORTS_VIEW -> str(R.string.view_dashboard_reports)
        DATA_EXPORT -> str(R.string.export_csv)
        DATA_IMPORT -> str(R.string.import_csv)
        TEAM_MANAGE -> str(R.string.manage_team)
        BACKUPS_MANAGE -> str(R.string.cloud_backups)
        EXPENSES_MANAGE -> str(R.string.record_expenses)
        else -> p
    }
}

/** Premium-only features, used for paywall copy and gating. */
enum class PremiumFeature(@StringRes val titleRes: Int, @StringRes val blurbRes: Int) {
    CLOUD(R.string.pf_cloud, R.string.pf_cloud_blurb),
    WAREHOUSES(R.string.pf_warehouses, R.string.pf_warehouses_blurb),
    TRANSFERS(R.string.stock_transfers, R.string.pf_transfers_blurb),
    SALES_MANAGEMENT(R.string.pf_sales, R.string.pf_sales_blurb),
    PDF_INVOICE(R.string.pf_pdf, R.string.pf_pdf_blurb),
    CSV(R.string.csv_export_import, R.string.pf_csv_blurb),
    LOW_STOCK(R.string.low_stock_channel, R.string.pf_low_stock_blurb),
    BARCODE(R.string.pf_barcode, R.string.pf_barcode_blurb),
    MOVEMENTS(R.string.stock_movement_history, R.string.pf_movements_blurb),
    SUPPLIERS(R.string.supplier_management, R.string.pf_suppliers_blurb),
    EXPENSES(R.string.expenses_net_profit, R.string.pf_expenses_blurb),
    TEAM(R.string.pf_team, R.string.pf_team_blurb),
    BACKUP(R.string.cloud_backup_recovery, R.string.pf_backup_blurb);

    val title: String get() = str(titleRes)
    val blurb: String get() = if (this == WAREHOUSES) str(blurbRes, Limits.warehouses()) else str(blurbRes)
}

object Limits {
    /** Warehouses allowed on the Basic (free) plan. Change freely. */
    const val BASIC_WAREHOUSES = 1

    /** "1 warehouse" / "3 warehouses" */
    fun warehouses(n: Int = BASIC_WAREHOUSES) = plural(R.plurals.n_warehouses, n, n)
}
