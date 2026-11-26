
package com.example.contapyme.navigation

object Routes {
    const val INICIO = "inicio"
    const val LOGIN = "login"
    const val TWO_FA = "two_fa"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val CHANGE_PASSWORD = "change_password"
    const val API_CONFIG = "api_config"
    const val DASHBOARD = "dashboard"

    // Ventas
    const val REGISTRAR_VENTA = "registrar_venta"
    const val VENTAS = "ventas"
    const val DETALLE_VENTA = "detalle_venta"

    // Gastos
    const val REGISTRAR_GASTO = "registrar_gasto"
    const val GASTOS = "gastos"
    const val DETALLE_GASTO = "detalle_gasto"

    // Caja / Efectivo
    const val CAJA = "caja"
    const val DETALLE_MOVIMIENTO_CAJA = "detalle_movimiento_caja"

    // Cuentas por Cobrar
    const val CUENTAS_POR_COBRAR = "cuentas_por_cobrar"
    const val DETALLE_CUENTA_COBRAR = "detalle_cuenta_cobrar"

    // Clientes
    const val CLIENTES = "clientes"
    const val AGREGAR_CLIENTE = "agregar_cliente"
    const val EDITAR_CLIENTE = "editar_cliente"
    const val DETALLE_CLIENTE = "detalle_cliente"

    // Inventario
    const val INVENTARIO = "inventario"

    // Notificaciones
    const val NOTIFICACIONES = "notificaciones"
    const val DETALLE_ALERTA = "detalle_alerta"

    // Navegación con parámetros
    fun detalleCliente(id: String) = "$DETALLE_CLIENTE/$id"
    fun editarCliente(id: String) = "$EDITAR_CLIENTE/$id"

    fun detalleVenta(codigo: String) = "$DETALLE_VENTA/$codigo"
    fun detalleGasto(id: String) = "$DETALLE_GASTO/$id"
    fun detalleMovimientoCaja(id: String) = "$DETALLE_MOVIMIENTO_CAJA/$id"
    fun detalleCuentaCobrar(id: String) = "$DETALLE_CUENTA_COBRAR/$id"
    fun detalleAlerta(id: String) = "$DETALLE_ALERTA/$id"


}
