package com.example.contapyme.data.api

import com.example.contapyme.data.model.ApiErrorResponse
import com.google.gson.Gson

/** Interpreta el cuerpo de error estándar { success, message, code, errors }. */
object ApiErrorParser {
    fun parse(errorBody: String?, fallback: String, httpCode: Int = 0): String {
        if (!errorBody.isNullOrBlank()) {
            try {
                val error = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                if (!error?.message.isNullOrBlank()) return error!!.message!!
                if (!error?.code.isNullOrBlank()) return error!!.code!!
            } catch (_: Exception) {
                // Cuerpo no JSON: se usa el mensaje HTTP.
            }
        }
        return when (httpCode) {
            401 -> "Tu sesión ha expirado. Inicia sesión nuevamente."
            403 -> "No tienes permiso para realizar esta operación."
            else -> fallback
        }
    }
}
