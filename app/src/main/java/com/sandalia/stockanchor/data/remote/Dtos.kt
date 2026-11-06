package com.sandalia.stockanchor.data.remote

/*
 * JSON contracts of the Laravel API. Gson is configured with LOWER_CASE_WITH_UNDERSCORES, so
 * `costPrice` <-> `cost_price`. Fields are nullable because Gson ignores Kotlin defaults.
 */

data class AuthResponse(
    val token: String? = null,
    /** Two-step verification is on: no token yet, send [challenge] + the authenticator code to auth/two-factor. */
    val twoFactorRequired: Boolean? = null,
    val challenge: String? = null,
    val user: UserDto? = null,
    val team: TeamDto? = null,
    val subscription: SubscriptionDto? = null,
)

data class UserDto(
    val id: Long = 0,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val permissions: List<String>? = null,
    val effectivePermissions: List<String>? = null,
    val isActive: Boolean? = null,
    val twoFactorEnabled: Boolean? = null,
)

data class TeamDto(val id: Long = 0, val name: String? = null, val currency: String? = null)

data class SubscriptionDto(
    val isPremium: Boolean? = null,
    val status: String? = null,
    val plan: String? = null,
    val expiresAt: String? = null,
    val autoRenewing: Boolean? = null,
)

data class CategoryDto(val id: Long, val name: String?, val description: String?)

data class WarehouseDto(val id: Long, val name: String?, val location: String?, val isDefault: Boolean?)

data class SupplierDto(
    val id: Long, val name: String?, val phone: String?, val email: String?, val address: String?, val note: String?,
)

data class ProductDto(
    val id: Long,
    val name: String?,
    val sku: String?,
    val barcode: String?,
    val categoryId: Long?,
    val warehouseId: Long?,
    val supplierId: Long?,
    val unit: String?,
    val costPrice: Double?,
    val salePrice: Double?,
    val lowStockThreshold: Double?,
    val description: String?,
    val createdAt: String?,
)

data class StockDto(val productId: Long, val warehouseId: Long, val quantity: Double?)

data class PurchaseItemDto(val id: Long, val purchaseId: Long, val productId: Long, val quantity: Double?, val unitCost: Double?)

data class PurchaseDto(
    val id: Long,
    val refNo: String?,
    val supplierId: Long?,
    val warehouseId: Long,
    val date: String?,
    val total: Double?,
    val note: String?,
    val items: List<PurchaseItemDto>?,
)

data class SaleItemDto(
    val id: Long, val saleId: Long, val productId: Long, val productName: String?,
    val quantity: Double?, val unitPrice: Double?, val unitCost: Double?,
)

data class SaleDto(
    val id: Long,
    val invoiceNo: String?,
    val customerName: String?,
    val customerPhone: String?,
    val warehouseId: Long,
    val date: String?,
    val subtotal: Double?,
    val discount: Double?,
    val total: Double?,
    val totalCost: Double?,
    val status: String?,
    val note: String?,
    val items: List<SaleItemDto>?,
)

data class TransferDto(
    val id: Long, val productId: Long, val fromWarehouseId: Long, val toWarehouseId: Long,
    val quantity: Double?, val date: String?, val note: String?,
)

data class MovementDto(
    val id: Long, val productId: Long, val warehouseId: Long, val type: String?, val quantity: Double?,
    val balanceAfter: Double?, val refType: String?, val refId: Long?, val note: String?, val userName: String?, val date: String?,
)

data class ExpenseDto(
    val id: Long, val category: String?, val amount: Double?, val date: String?, val note: String?, val userName: String?,
)

data class DeletedDto(
    val categories: List<Long>?, val warehouses: List<Long>?, val suppliers: List<Long>?, val products: List<Long>?,
    val expenses: List<Long>?,
)

data class SyncResponse(
    val serverTime: String?,
    val full: Boolean?,
    val categories: List<CategoryDto>?,
    val warehouses: List<WarehouseDto>?,
    val suppliers: List<SupplierDto>?,
    val products: List<ProductDto>?,
    val stocks: List<StockDto>?,
    val purchases: List<PurchaseDto>?,
    val sales: List<SaleDto>?,
    val transfers: List<TransferDto>?,
    val movements: List<MovementDto>?,
    val expenses: List<ExpenseDto>?,
    val deleted: DeletedDto?,
)

/* ---------- Requests ---------- */

/** [requirePremium]: the server issues no token unless the company has Premium or this request brings valid proof of payment. */
data class LoginRequest(
    val email: String, val password: String, val deviceName: String, val requirePremium: Boolean = true,
    val purchaseToken: String? = null, val promoCode: String? = null, val productId: String? = null, val basePlanId: String? = null,
)

data class RegisterRequest(
    val name: String, val email: String, val password: String, val companyName: String,
    val currency: String?, val deviceName: String, val emailCode: String,
    val purchaseToken: String?, val promoCode: String?, val productId: String?, val basePlanId: String?,
)

/** Asks for the email code; the server sends it only when the proof of payment is valid. */
data class RegisterCodeRequest(
    val email: String, val purchaseToken: String?, val promoCode: String?, val productId: String?, val basePlanId: String?,
)

data class RegisterCodeResponse(val expiresInMinutes: Int = 10, val resendAfterSeconds: Int = 60)

data class PromoRequest(val code: String)

data class SubscriptionPlansResponse(val productId: String? = null)

data class PromoCheckResponse(val valid: Boolean = false, val code: String? = null, val durationDays: Int = 0)

data class TwoFactorChallengeRequest(
    val challenge: String, val code: String, val requirePremium: Boolean = true,
    val purchaseToken: String? = null, val promoCode: String? = null, val productId: String? = null, val basePlanId: String? = null,
)

data class PasswordRequest(val password: String)

data class CodeRequest(val code: String)

data class TwoFactorStatus(val enabled: Boolean = false, val recoveryCodesLeft: Int = 0)

/** Secret for manual entry and the otpauth:// link that opens the authenticator app directly. */
data class TwoFactorSetup(val secret: String = "", val otpauthUrl: String = "")

data class RecoveryCodesResponse(val recoveryCodes: List<String> = emptyList())

data class VerifyPurchaseRequest(val purchaseToken: String, val productId: String?, val basePlanId: String?)

data class CategoryRequest(val name: String, val description: String?)

data class WarehouseRequest(val name: String, val location: String?, val isDefault: Boolean)

data class SupplierRequest(val name: String, val phone: String?, val email: String?, val address: String?, val note: String?)

data class ProductRequest(
    val name: String, val sku: String?, val barcode: String?, val categoryId: Long?, val warehouseId: Long?, val supplierId: Long?, val unit: String,
    val costPrice: Double, val salePrice: Double, val lowStockThreshold: Double, val description: String?,
)

data class LineRequest(val productId: Long, val quantity: Double, val unitCost: Double? = null, val unitPrice: Double? = null)

data class PurchaseRequest(
    val warehouseId: Long, val supplierId: Long?, val note: String?, val updateCostPrice: Boolean, val items: List<LineRequest>,
)

data class SaleRequest(
    val customerName: String, val customerPhone: String?, val warehouseId: Long, val discount: Double,
    val note: String?, val items: List<LineRequest>,
)

data class CancelRequest(val reason: String?)

/** [date] is ISO-8601 with offset. */
data class ExpenseRequest(val category: String, val amount: Double, val date: String, val note: String?)

data class TransferRequest(val productId: Long, val fromWarehouseId: Long, val toWarehouseId: Long, val quantity: Double, val note: String?)

data class AdjustRequest(val productId: Long, val warehouseId: Long, val setTo: Double, val note: String?)

data class ImportRowsRequest(val rows: List<Map<String, String>>)

data class ImportResult(val imported: Int?, val createdProducts: Int?, val errors: List<String>?)

data class PermissionsResponse(val permissions: List<String>?, val roles: Map<String, List<String>>?)

data class MemberRequest(
    val name: String?, val email: String?, val password: String?, val role: String?,
    val permissions: List<String>?, val isActive: Boolean?,
)

data class BackupDto(
    val id: Long, val size: Long?, val counts: Map<String, Int>?, val isAuto: Boolean?, val note: String?, val createdAt: String?,
)

data class BackupRequest(val note: String?)

data class MessageResponse(val message: String?)
