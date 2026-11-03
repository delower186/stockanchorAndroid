package com.sandalia.stockanchor.data.remote

import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.str
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.sandalia.stockanchor.BuildConfig
import com.sandalia.stockanchor.data.prefs.SessionStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Error surfaced to the UI with a human readable message. */
class ApiException(message: String, val status: Int = 0, val code: String? = null) : Exception(message)

enum class ApiEvent { UNAUTHENTICATED, ACCOUNT_DISABLED, TEAM_SUSPENDED, SUBSCRIPTION_REQUIRED }

class ApiClient(private val session: SessionStore) {

    val gson: Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .serializeNulls()
        .create()

    private val _events = MutableSharedFlow<ApiEvent>(extraBufferCapacity = 4)
    /** Global events (token expired, subscription lapsed) observed by the app shell. */
    val events: SharedFlow<ApiEvent> = _events

    @Volatile private var cachedBaseUrl: String? = null
    @Volatile private var cachedService: ApiService? = null

    val service: ApiService
        get() {
            val base = session.baseUrl()
            val existing = cachedService
            if (existing != null && base == cachedBaseUrl) return existing
            return build(base).also { cachedService = it; cachedBaseUrl = base }
        }

    private fun build(baseUrl: String): ApiService {
        val http = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("X-Client", "StockAnchor-Android/${BuildConfig.VERSION_NAME}")
                session.token()?.let { builder.header("Authorization", "Bearer $it") }
                chain.proceed(builder.build())
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(http)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    /** Runs an API call and converts HTTP / network failures into [ApiException]. */
    suspend fun <T> call(block: suspend ApiService.() -> T): T {
        try {
            return service.block()
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string().orEmpty()
            var message = str(R.string.server_error_x, e.code())
            var code: String? = null
            runCatching {
                val json = JsonParser.parseString(body).asJsonObject
                code = json.get("code")?.takeIf { !it.isJsonNull }?.asString
                val errors = json.getAsJsonObject("errors")
                message = errors?.entrySet()?.firstOrNull()?.value?.asJsonArray?.firstOrNull()?.asString
                    ?: json.get("message")?.asString ?: message
            }
            when (e.code()) {
                401 -> _events.tryEmit(ApiEvent.UNAUTHENTICATED)
                402 -> _events.tryEmit(ApiEvent.SUBSCRIPTION_REQUIRED)
                403 -> when (code) {
                    "account_disabled" -> _events.tryEmit(ApiEvent.ACCOUNT_DISABLED)
                    "team_suspended" -> _events.tryEmit(ApiEvent.TEAM_SUSPENDED)
                }
            }
            throw ApiException(message, e.code(), code)
        } catch (e: IOException) {
            throw ApiException(str(R.string.can_t_reach_the_stock_anchor))
        }
    }

    companion object {
        val DEFAULT_BASE_URL: String = BuildConfig.API_BASE_URL
    }
}
