package com.example.contapyme.data.repository

import android.content.Context
import com.example.contapyme.data.api.ApiClient
import com.example.contapyme.data.local.SessionManager
import com.example.contapyme.data.model.ApiErrorResponse
import com.example.contapyme.data.model.ProductoDto
import com.example.contapyme.data.model.ProductosStatsDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ProductosRepository(private val context: Context) {
    private val sessionManager = SessionManager(context)
    private fun productosApi() = ApiClient.getProductosApi(context)

    suspend fun getProductos(query: String? = null, categoria: String? = null): Result<List<ProductoDto>> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.token.first()
            if (token.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Sesión no iniciada. Por favor, ingresa a tu cuenta."))
            }
            val response = productosApi().getProductos(
                authorization = "Bearer $token",
                query = query?.takeIf { it.isNotBlank() },
                categoria = categoria?.takeIf { it.isNotBlank() }
            )
            if (response.isSuccessful) {
                val body = response.body()
                val list = body?.data ?: emptyList()
                Result.success(list)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), response.message())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStats(): Result<ProductosStatsDto?> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.token.first()
            if (token.isNullOrEmpty()) return@withContext Result.success(null)
            val response = productosApi().getStats("Bearer $token")
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    suspend fun getProducto(id: Int): Result<ProductoDto?> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.token.first()
            if (token.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Sesión no iniciada."))
            }
            val response = productosApi().getProducto("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), response.message())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?, fallback: String): String {
        return try {
            if (errorBody.isNullOrEmpty()) return fallback
            val gson = Gson()
            val obj = gson.fromJson(errorBody, ApiErrorResponse::class.java)
            obj.message ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }
}
