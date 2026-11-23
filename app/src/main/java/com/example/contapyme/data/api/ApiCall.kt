package com.example.contapyme.data.api

import com.example.contapyme.data.model.ApiErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

/**
 * Ejecuta una llamada de Retrofit y la convierte en Result.
 * - 2xx  -> Result.success(body)
 * - error del servidor -> Result.failure con el "message" que manda el backend
 * - sin red / URL mala -> mensaje claro
 */
suspend fun <T> apiCall(block: suspend () -> Response<T>): Result<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) Result.success(body)
            else Result.failure(Exception("Respuesta vacía del servidor"))
        } else {
            Result.failure(Exception(parseError(response.errorBody()?.string(), response.code())))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(Exception("No se pudo conectar con el servidor. Revisa tu conexión y la URL de la API."))
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ocurrió un error inesperado"))
    }
}

private fun parseError(raw: String?, code: Int): String {
    val mensaje = try {
        Gson().fromJson(raw, ApiErrorResponse::class.java)?.message
    } catch (e: Exception) {
        null
    }
    return mensaje?.takeIf { it.isNotBlank() } ?: "Error del servidor ($code)"
}
