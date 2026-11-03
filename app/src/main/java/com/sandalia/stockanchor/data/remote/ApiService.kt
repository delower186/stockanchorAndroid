package com.sandalia.stockanchor.data.remote

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // ---- Auth & subscription ----
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): AuthResponse
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): AuthResponse
    @POST("auth/logout") suspend fun logout(): MessageResponse
    @GET("me") suspend fun me(): AuthResponse
    @POST("auth/two-factor") suspend fun twoFactorChallenge(@Body body: TwoFactorChallengeRequest): AuthResponse
    @GET("me/two-factor") suspend fun twoFactorStatus(): TwoFactorStatus
    @POST("me/two-factor") suspend fun enableTwoFactor(@Body body: PasswordRequest): TwoFactorSetup
    @POST("me/two-factor/confirm") suspend fun confirmTwoFactor(@Body body: CodeRequest): RecoveryCodesResponse
    @POST("me/two-factor/recovery-codes") suspend fun newRecoveryCodes(@Body body: PasswordRequest): RecoveryCodesResponse
    @HTTP(method = "DELETE", path = "me/two-factor", hasBody = true) suspend fun disableTwoFactor(@Body body: PasswordRequest): MessageResponse
    @POST("subscription/verify") suspend fun verifyPurchase(@Body body: VerifyPurchaseRequest): AuthResponse

    // ---- Sync ----
    @GET("sync") suspend fun sync(@Query("since") since: String?): SyncResponse
    @POST("sync/import") suspend fun importLocal(@Body body: JsonObject): JsonElement

    // ---- Catalogue ----
    @POST("categories") suspend fun createCategory(@Body body: CategoryRequest): JsonElement
    @PUT("categories/{id}") suspend fun updateCategory(@Path("id") id: Long, @Body body: CategoryRequest): JsonElement
    @DELETE("categories/{id}") suspend fun deleteCategory(@Path("id") id: Long): JsonElement

    @POST("warehouses") suspend fun createWarehouse(@Body body: WarehouseRequest): JsonElement
    @PUT("warehouses/{id}") suspend fun updateWarehouse(@Path("id") id: Long, @Body body: WarehouseRequest): JsonElement
    @DELETE("warehouses/{id}") suspend fun deleteWarehouse(@Path("id") id: Long): JsonElement

    @POST("suppliers") suspend fun createSupplier(@Body body: SupplierRequest): JsonElement
    @PUT("suppliers/{id}") suspend fun updateSupplier(@Path("id") id: Long, @Body body: SupplierRequest): JsonElement
    @DELETE("suppliers/{id}") suspend fun deleteSupplier(@Path("id") id: Long): JsonElement

    @POST("products") suspend fun createProduct(@Body body: ProductRequest): JsonElement
    @PUT("products/{id}") suspend fun updateProduct(@Path("id") id: Long, @Body body: ProductRequest): JsonElement
    @DELETE("products/{id}") suspend fun deleteProduct(@Path("id") id: Long): JsonElement

    // ---- Stock operations ----
    @POST("purchases") suspend fun createPurchase(@Body body: PurchaseRequest): JsonElement
    @POST("sales") suspend fun createSale(@Body body: SaleRequest): SaleDto
    @POST("sales/{id}/cancel") suspend fun cancelSale(@Path("id") id: Long, @Body body: CancelRequest): JsonElement
    @POST("transfers") suspend fun createTransfer(@Body body: TransferRequest): JsonElement
    @POST("stocks/adjust") suspend fun adjustStock(@Body body: AdjustRequest): JsonElement
    @POST("import/stock") suspend fun importStock(@Body body: ImportRowsRequest): ImportResult

    // ---- Expenses ----
    @POST("expenses") suspend fun createExpense(@Body body: ExpenseRequest): JsonElement
    @PUT("expenses/{id}") suspend fun updateExpense(@Path("id") id: Long, @Body body: ExpenseRequest): JsonElement
    @DELETE("expenses/{id}") suspend fun deleteExpense(@Path("id") id: Long): JsonElement

    // ---- Team ----
    @GET("team/permissions") suspend fun permissions(): PermissionsResponse
    @GET("team/members") suspend fun members(): List<UserDto>
    @POST("team/members") suspend fun createMember(@Body body: MemberRequest): UserDto
    @PUT("team/members/{id}") suspend fun updateMember(@Path("id") id: Long, @Body body: MemberRequest): UserDto
    @DELETE("team/members/{id}") suspend fun deleteMember(@Path("id") id: Long): MessageResponse

    // ---- Cloud backup ----
    @GET("backups") suspend fun backups(): List<BackupDto>
    @POST("backups") suspend fun createBackup(@Body body: BackupRequest): BackupDto
    @POST("backups/{id}/restore") suspend fun restoreBackup(@Path("id") id: Long): MessageResponse
    @DELETE("backups/{id}") suspend fun deleteBackup(@Path("id") id: Long): MessageResponse
}
