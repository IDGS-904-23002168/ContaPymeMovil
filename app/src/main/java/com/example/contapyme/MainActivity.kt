package com.example.contapyme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.contapyme.navigation.Routes
import com.example.contapyme.ui.components.FloatingBottomBar
import com.example.contapyme.ui.components.QuickActionSheet
import com.example.contapyme.ui.screens.AgregarClienteScreen
import com.example.contapyme.ui.screens.clientes.ClientesScreen
import com.example.contapyme.ui.screens.ClientesScreen
import com.example.contapyme.ui.screens.ComprasScreen
import com.example.contapyme.ui.screens.DashboardScreen
import com.example.contapyme.ui.screens.DetalleAlertaScreen
import com.example.contapyme.ui.screens.DetalleClienteScreen
import com.example.contapyme.ui.screens.DetalleCompraScreen
import com.example.contapyme.ui.screens.DetalleVentaScreen
import com.example.contapyme.ui.screens.InicioScreen
import com.example.contapyme.ui.screens.InventarioScreen
import com.example.contapyme.ui.screens.NotificacionesScreen
import com.example.contapyme.ui.screens.RegistrarGastoScreen
import com.example.contapyme.ui.screens.RegistrarVentaScreen
import com.example.contapyme.ui.screens.VentasScreen
import com.example.contapyme.ui.screens.auth.ApiConfigScreen
import com.example.contapyme.ui.screens.auth.ChangePasswordScreen
import com.example.contapyme.ui.screens.auth.ForgotPasswordScreen
import com.example.contapyme.ui.screens.auth.LoginScreenNew
import com.example.contapyme.ui.screens.auth.ResetPasswordScreen
import com.example.contapyme.ui.screens.auth.TwoFAScreen
import com.example.contapyme.ui.theme.ContaPymeTheme
import com.example.contapyme.ui.viewmodel.AuthViewModel
import com.example.contapyme.ui.viewmodel.NotificacionesViewModel
import com.example.contapyme.ui.viewmodel.NotificacionesUiState
import com.example.contapyme.ui.screens.GastosScreen
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContaPymeTheme {
                AppNavHost()
            }
        }
    }
}

private val bottomBarRoutes = setOf(
    Routes.DASHBOARD,
    Routes.INVENTARIO,
    Routes.CLIENTES,
    Routes.VENTAS,
    Routes.COMPRAS
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    val navBackStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        navBackStackEntry?.destination?.route

    var showQuickActions by remember {
        mutableStateOf(false)
    }

    // ViewModel original de autenticación.
    val authViewModel: AuthViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsState()

    // Observar estado de sesión global: si se cierra sesión y no estamos en una pantalla pública, salir al Login
    androidx.compose.runtime.LaunchedEffect(uiState.isLoggedIn) {
        if (!uiState.isLoggedIn && currentRoute != null && currentRoute != Routes.LOGIN && currentRoute != Routes.TWO_FA && currentRoute != Routes.FORGOT_PASSWORD && !currentRoute.startsWith(Routes.RESET_PASSWORD) && currentRoute != Routes.API_CONFIG) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // ViewModel compartido del módulo de notificaciones.
    val notificacionesViewModel: NotificacionesViewModel =
        viewModel()

    // Estado de las notificaciones.
    val estadoNotificaciones by
    notificacionesViewModel.uiState.collectAsState()

    // Control de la sesión de notificaciones.
    var tokenActual by remember {
        mutableStateOf<String?>(null)
    }

    var cerrandoSesion by remember {
        mutableStateOf(false)
    }

    // Ciclo de vida de la Activity.
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    val lifecycle = lifecycleOwner.lifecycle

    var appActiva by remember(lifecycle) {
        mutableStateOf(
            lifecycle.currentState.isAtLeast(
                Lifecycle.State.STARTED
            )
        )
    }

    // Observar si la aplicación está visible o
    // pasa a segundo plano.
    DisposableEffect(lifecycle) {

        val observer = LifecycleEventObserver { _, evento ->

            when (evento) {

                Lifecycle.Event.ON_START -> {
                    appActiva = true
                }

                Lifecycle.Event.ON_STOP -> {
                    appActiva = false

                    notificacionesViewModel
                        .detenerActualizacionAutomatica()
                }

                else -> Unit
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)

            notificacionesViewModel
                .detenerActualizacionAutomatica()
        }
    }

    // Observar el token almacenado en SessionManager.
    // También detecta cuando el interceptor elimina
    // una sesión expirada.
    LaunchedEffect(authViewModel, notificacionesViewModel) {

        authViewModel.tokenFlow.collect { token ->

            val nuevoToken = token?.takeIf {
                it.isNotBlank()
            }

            // Limpiar las alertas si cambia el usuario
            // o se elimina la sesión.
            if (tokenActual != nuevoToken) {

                notificacionesViewModel.limpiarSesion()

                tokenActual = nuevoToken
            }

            if (nuevoToken == null) {

                notificacionesViewModel.limpiarSesion()

                cerrandoSesion = false
            }
        }
    }

    // Controlar las consultas según la sesión
    // y el estado de la aplicación.
    LaunchedEffect(
        tokenActual,
        appActiva,
        cerrandoSesion
    ) {

        when {

            tokenActual.isNullOrBlank() ||
                    cerrandoSesion -> {

                notificacionesViewModel.limpiarSesion()
            }

            appActiva -> {

                // Iniciar la sesión de notificaciones.
                notificacionesViewModel.iniciarSesion()

                // Actualizar al regresar a la aplicación.
                notificacionesViewModel.cargarNotificaciones(
                    mostrarCarga = false
                )

                // Consultar automáticamente cada 60 segundos.
                notificacionesViewModel
                    .iniciarActualizacionAutomatica()
            }

            else -> {

                // Detener consultas periódicas cuando
                // la aplicación pasa a segundo plano.
                notificacionesViewModel
                    .detenerActualizacionAutomatica()
            }
        }
    }

    // Evitar mostrar información anterior mientras
    // se está cerrando la sesión.
    val notificacionesState = if (
        tokenActual.isNullOrBlank() || cerrandoSesion
    ) {
        NotificacionesUiState()
    } else {
        estadoNotificaciones
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Color(0xFFF4F6FB)
            )
    ) {

        NavHost(
            navController = navController,
            startDestination = Routes.INICIO,
            modifier = Modifier.fillMaxSize()
        ) {
            // INICIO
            composable(Routes.INICIO) {
                InicioScreen(
                    onTimeout = {

                        val destination = if (
                            authViewModel.uiState.value.isLoggedIn
                        ) {
                            Routes.DASHBOARD
                        } else {
                            Routes.LOGIN
                        }
                        navController.navigate(destination) {
                            popUpTo(Routes.INICIO) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // INICIO DE SESIÓN
            composable(Routes.LOGIN) {
                LoginScreenNew(
                    viewModel = authViewModel,
                    onRequires2FA = {
                        navController.navigate(Routes.TWO_FA) {
                            launchSingleTop = true
                        }
                    },
                    onAlreadyLoggedIn = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(
                            Routes.FORGOT_PASSWORD
                        )
                    },
                    onNavigateToApiConfig = {
                        navController.navigate(
                            Routes.API_CONFIG
                        )
                    }
                )
            }

            // AUTENTICACIÓN DE DOS FACTORES
            composable(Routes.TWO_FA) {
                TwoFAScreen(
                    viewModel = authViewModel,
                    onBack = {
                        authViewModel.reset2FAFlow()
                        navController.popBackStack()
                    },
                    onVerified = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // RECUPERAR CONTRASEÑA
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    viewModel = authViewModel,

                    onBack = {
                        navController.popBackStack()
                    },

                    onNavigateToReset = { token ->
                        navController.navigate(
                            "${Routes.RESET_PASSWORD}/$token"
                        )
                    }
                )
            }

            // RESTABLECER CONTRASEÑA
            composable(
                "${Routes.RESET_PASSWORD}/{token}"
            ) { backStackEntry ->

                val token =
                    backStackEntry.arguments
                        ?.getString("token") ?: ""

                ResetPasswordScreen(
                    viewModel = authViewModel,
                    token = token,

                    onBack = {
                        navController.popBackStack()
                    },

                    onSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // CAMBIAR CONTRASEÑA
            composable(Routes.CHANGE_PASSWORD) {
                ChangePasswordScreen(
                    viewModel = authViewModel,

                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.API_CONFIG) {
                ApiConfigScreen(
                    viewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    userName = uiState.userName ?: "Usuario",
                    userEmail = uiState.userEmail,
                    rolName = uiState.rolName,
                    onNotificaciones = { navController.navigate(Routes.NOTIFICACIONES) },
                    onVerAlerta = { id -> navController.navigate(Routes.detalleAlerta(id)) },
                    onVerVentas = { navController.navigate(Routes.VENTAS) },
                    onCerrarSesion = {
                        authViewModel.logout {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            // CONFIGURACIÓN DE API
            composable(Routes.API_CONFIG) {
                ApiConfigScreen(
                    viewModel = authViewModel,

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // DASHBOARD
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNotificaciones = {
                        navController.navigate(Routes.NOTIFICACIONES)
                    },

                    onVerGastos = {
                        navController.navigate(Routes.GASTOS)
                    },

                    onVerAlerta = { id ->
                        navController.navigate(
                            Routes.detalleAlerta(id)
                        )
                    },

                    onVerVentas = {
                        navController.navigate(Routes.VENTAS)
                    },

                    onCerrarSesion = {
                        cerrandoSesion = true
                        notificacionesViewModel.limpiarSesion()
                        showQuickActions = false
                        authViewModel.logout()

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.DASHBOARD) {
                                inclusive = true
                            }
                        }
                    },

                    notificacionesState = notificacionesState,

                    onReintentarNotificaciones = {
                        notificacionesViewModel.reintentar()
                    }
                )
            }

            // REGISTRAR VENTA
            composable(Routes.REGISTRAR_VENTA) {
                RegistrarVentaScreen(
                    onBack = { navController.popBackStack() },
                    onGuardar = {
                        navController.navigate(Routes.VENTAS) {
                            popUpTo(Routes.DASHBOARD)
                            launchSingleTop = true
                        }

                    }
                )
            }

            // REGISTRAR GASTO
            composable(Routes.REGISTRAR_GASTO) {
                RegistrarGastoScreen(

                    onBack = {
                        navController.popBackStack()
                    },

                    onGuardar = {
                        navController.popBackStack()
                    }
                )

            }

            composable(Routes.GASTOS) {
                GastosScreen(
                    onBack = { navController.popBackStack() },
                    onRegistrar = {
                        navController.navigate(Routes.REGISTRAR_GASTO)
                    }
                )
            }

            // CLIENTES
            composable(Routes.CLIENTES) {
                ClientesScreen(

                    onBack = {
                        navController.popBackStack()
                    },

                    onAgregar = {
                        navController.navigate(
                            Routes.AGREGAR_CLIENTE
                        )
                    },

                    onVerDetalle = { id ->
                        navController.navigate(
                            Routes.detalleCliente(id)
                        )
                    }
                )
            }

            // AGREGAR CLIENTE
            composable(Routes.AGREGAR_CLIENTE) {
                AgregarClienteScreen(

                    onBack = { navController.popBackStack() },
                    onGuardar = {
                        navController.navigate(Routes.CLIENTES) {
                            popUpTo(Routes.CLIENTES) { inclusive = true }
                            launchSingleTop = true
                        }

                    }
                )
            }


            composable("${Routes.DETALLE_CLIENTE}/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                DetalleClienteScreen(
                    id = id,
                    onBack = { navController.popBackStack() },
                    onEditar = { clienteId -> navController.navigate(Routes.editarCliente(clienteId)) },
                    onVerVenta = { codigo -> navController.navigate(Routes.detalleVenta(codigo)) }
                )
            }

            composable("${Routes.EDITAR_CLIENTE}/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                AgregarClienteScreen(
                    clienteId = id,
                    onBack = { navController.popBackStack() },
                    onGuardar = {
                        navController.navigate(Routes.CLIENTES) {
                            popUpTo(Routes.CLIENTES) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // DETALLE DE CLIENTE
            composable(
                "${Routes.DETALLE_CLIENTE}/{id}"
            ) { backStackEntry ->

                val id =
                    backStackEntry.arguments?.getString("id")
                        ?: return@composable

                DetalleClienteScreen(
                    clienteId = id,

                    onBack = {
                        navController.popBackStack()
                    },

                    onEditar = {
                        navController.navigate(
                            Routes.editarCliente(id)
                        )
                    }
                )
            }

            // VENTAS
            composable(Routes.VENTAS) {
                VentasScreen(

                    onBack = {
                        navController.popBackStack()
                    },

                    onVerDetalle = { codigo ->
                        navController.navigate(
                            Routes.detalleVenta(codigo)
                        )
                    }
                )
            }


            composable("${Routes.DETALLE_VENTA}/{codigo}") { backStackEntry ->
                val codigo = backStackEntry.arguments?.getString("codigo").orEmpty()
                DetalleVentaScreen(
                    id = codigo,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.COMPRAS) {
                ComprasScreen(
                    onBack = { navController.popBackStack() },
                    onVerDetalle = { codigo -> navController.navigate(Routes.detalleCompra(codigo)) }
                )
            }

            composable("${Routes.DETALLE_COMPRA}/{codigo}") { backStackEntry ->
                val codigo = backStackEntry.arguments?.getString("codigo").orEmpty()
                DetalleCompraScreen(
                    id = codigo,
                    onBack = { navController.popBackStack() }
                )
            }

            // INVENTARIO
            composable(Routes.INVENTARIO) {
                InventarioScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // NOTIFICACIONES
            composable(Routes.NOTIFICACIONES) {
                NotificacionesScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onVerDetalle = { id ->
                        navController.navigate(
                            Routes.detalleAlerta(id)
                        )
                    },
                    // Estado compartido con el Dashboard.
                    uiState = notificacionesState,
                    // Cambiar el filtro seleccionado.
                    onFiltroChange = { filtro ->
                        notificacionesViewModel
                            .onFiltroChange(filtro)
                    },
                    // Actualización manual.
                    onActualizar = {
                        notificacionesViewModel.actualizar()
                    },
                    // Reintentar si ocurre un error.
                    onReintentar = {
                        notificacionesViewModel.reintentar()
                    }
                )
            }

            // DETALLE DE ALERTA
            composable(
                "${Routes.DETALLE_ALERTA}/{id}"
            ) { backStackEntry ->

                // Obtener el identificador real.
                val id =
                    backStackEntry.arguments
                        ?.getString("id") ?: ""

                DetalleAlertaScreen(

                    onBack = {
                        navController.popBackStack()
                    },

                    alertaId = id,

                    // Utilizar el mismo estado compartido.
                    uiState = notificacionesState,

                    onReintentar = {
                        notificacionesViewModel.reintentar()
                    }
                )
            }
        }

        // BARRA DE NAVEGACIÓN INFERIOR
        if (currentRoute in bottomBarRoutes) {
            FloatingBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(Routes.DASHBOARD) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },

                onAddClick = {
                    showQuickActions = true
                },

                modifier = Modifier.align(
                    Alignment.BottomCenter
                )
            )
        }

        // ACCIONES RÁPIDAS
        if (showQuickActions) {
            QuickActionSheet(

                onDismiss = {
                    showQuickActions = false
                },

                onRegistrarVenta = {
                    showQuickActions = false

                    navController.navigate(
                        Routes.REGISTRAR_VENTA
                    )
                },
                onRegistrarGasto = {
                    showQuickActions = false
                    navController.navigate(Routes.REGISTRAR_GASTO)
                },
                onConsultarCompras = {
                    showQuickActions = false
                    navController.navigate(Routes.COMPRAS)
                }
            )
        }
    }
}

