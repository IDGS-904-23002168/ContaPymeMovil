package com.example.contapyme.data.model

import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Locale

private val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

/** Formato de fecha usado por la API: YYYY-MM-DD. */
fun fechaApi(fecha: LocalDate): String = fecha.toString()

fun fechaHoyApi(): String = LocalDate.now().toString()

/** Convierte fechas ISO (o YYYY-MM-DD) a una etiqueta corta en español. */
fun formatearFecha(valor: String?): String {
    if (valor.isNullOrBlank()) return "—"
    val fechaParte = valor.take(10)
    return try {
        val fecha = LocalDate.parse(fechaParte)
        "${fecha.dayOfMonth} ${MESES[fecha.monthValue - 1]} ${fecha.year}"
    } catch (_: DateTimeParseException) {
        valor
    }
}

fun formatearDinero(valor: Double?): String = "$" + "%,.2f".format(Locale.US, valor ?: 0.0)
