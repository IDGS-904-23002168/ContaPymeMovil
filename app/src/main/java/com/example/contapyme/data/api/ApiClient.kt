package com.example.contapyme.data.api

import android.content.Context
import com.example.contapyme.data.local.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Avisa a la app cuando el servidor responde 401. */
object SessionEvents {
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    fun notifyExpired() {
        _expired.tryEmit(Unit)
    }
}

private class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        if (original.header("Authorization") == null) {
            val token = runBlocking { sessionManager.token.first() }
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        val response = chain.proceed(builder.build())

        val path = original.url.encodedPath
        val esAuth = path.endsWith("/auth/login") ||
                path.endsWith("/auth/verify-2fa")

        if (response.code == 401 && !esAuth) {
            runBlocking { sessionManager.clearSession() }
            SessionEvents.notifyExpired()
        }

        return response
    }
}

object ApiClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null

    @Synchronized
    fun getRetrofit(context: Context): Retrofit {
        val sessionManager = SessionManager(context.applicationContext)
        val baseUrl = runBlocking {
            sessionManager.apiBaseUrl.first()
        }

        if (retrofit == null || currentBaseUrl != baseUrl) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            currentBaseUrl = baseUrl
        }

        return retrofit!!
    }

    fun getAuthApi(context: Context): AuthApi {
        return getRetrofit(context).create(AuthApi::class.java)
    }

    fun getClienteApi(context: Context): ClienteApi {
        return getRetrofit(context).create(ClienteApi::class.java)
    }

    fun getNotificacionesApi(context: Context): NotificacionesApi {
        return getRetrofit(context).create(NotificacionesApi::class.java)
    }

    fun getGastoApi(context: Context): GastoApi {
        return getRetrofit(context).create(GastoApi::class.java)
    }

    @Synchronized
    fun reset() {
        retrofit = null
        currentBaseUrl = null
    }
}

