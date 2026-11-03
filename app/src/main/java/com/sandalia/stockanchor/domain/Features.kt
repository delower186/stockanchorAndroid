package com.sandalia.stockanchor.domain

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
        CATALOG_MANAGE -> "Manage products & categories"
        PURCHASES_CREATE -> "Record purchases"
        SALES_CREATE -> "Make sales"
        SALES_MANAGE -> "Manage & cancel sales"
        TRANSFERS_CREATE -> "Transfer stock"
        STOCK_ADJUST -> "Adjust stock counts"
        WAREHOUSES_MANAGE -> "Manage warehouses"
        SUPPLIERS_MANAGE -> "Manage suppliers"
        REPORTS_VIEW -> "View dashboard & reports"
        DATA_EXPORT -> "Export CSV"
        DATA_IMPORT -> "Import CSV"
        TEAM_MANAGE -> "Manage team"
        BACKUPS_MANAGE -> "Cloud backups"
        EXPENSES_MANAGE -> "Record expenses"
        else -> p
    }
}

/** Premium-only features, used for paywall copy and gating. */
enum class PremiumFeature(val title: String, val blurb: String) {
    CLOUD("Cloud database", "Your inventory lives on your own secure server and syncs across devices."),
    WAREHOUSES("Unlimited warehouses", "Track stock per location — Basic includes ${Limits.warehouses()}."),
    TRANSFERS("Stock transfers", "Move products between warehouses with a full audit trail."),
    SALES_MANAGEMENT("Sales management", "Search sales history, cancel and return orders."),
    PDF_INVOICE("PDF invoices", "Branded PDF invoices you can share on WhatsApp or email."),
    CSV("CSV export & import", "Export products, stock and sales; bulk import stock from a spreadsheet."),
    LOW_STOCK("Low stock alerts", "Daily notifications when products drop below their threshold."),
    BARCODE("Barcode scanning", "Scan to find products, fill product codes and ring up sales."),
    MOVEMENTS("Stock movement history", "Every in and out, by product, warehouse and type."),
    SUPPLIERS("Supplier management", "Keep suppliers and link them to purchases."),
    EXPENSES("Expenses & net profit", "Record rent, salaries and other costs to see your real (net) profit."),
    TEAM("Team accounts & permissions", "Invite staff with roles and fine-grained permissions."),
    BACKUP("Cloud backup & recovery", "Nightly automatic backups plus one-tap restore."),
}

object Limits {
    /** Warehouses allowed on the Basic (free) plan. Change freely. */
    const val BASIC_WAREHOUSES = 1

    /** "1 warehouse" / "3 warehouses" */
    fun warehouses(n: Int = BASIC_WAREHOUSES) = if (n == 1) "1 warehouse" else "$n warehouses"
}
