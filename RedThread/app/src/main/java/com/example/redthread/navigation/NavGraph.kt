package com.example.redthread.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.redthread.ui.components.AppTopBar
import com.example.redthread.ui.screen.*
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.viewmodel.AuthViewModel
import com.example.redthread.domain.enums.UserRole
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redthread.ui.viewmodel.CartViewModel
import com.example.redthread.ui.viewmodel.DeveloperViewModel
import com.example.redthread.ui.viewmodel.ProductoViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val header by authViewModel.header.collectAsState()

    // VM del carrito a nivel de NavGraph
    val cartVm: CartViewModel = viewModel()
    val cartCount by cartVm.count.collectAsState()

    LaunchedEffect(header.email) {
        cartVm.bindToUserEmail(header.email)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                onLogoClick = {
                    navController.navigate(Route.Home.path) {
                        launchSingleTop = true
                        popUpTo(Route.Home.path) { inclusive = false }
                    }
                },
                onPerfilClick = {
                    if (authViewModel.header.value.isLoggedIn) {
                        navController.navigate(Route.Perfil.path) { launchSingleTop = true }
                    } else {
                        navController.navigate(Route.Login.path) { launchSingleTop = true }
                    }
                },
                onCarritoClick = {
                    navController.navigate(Route.Carrito.path) { launchSingleTop = true }
                },
                cartCount = cartCount
            )
        },
        containerColor = Black
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Route.Home.path,
            modifier = Modifier
                .padding(innerPadding)
                .background(Black)
        ) {
            // Home
            composable(Route.Home.path) {
                HomeScreen(
                    onProductoClick = { p ->
                        val nombre = URLEncoder.encode(p.nombre, StandardCharsets.UTF_8.toString())
                        val precio = URLEncoder.encode(p.precio, StandardCharsets.UTF_8.toString())
                        val categoria =
                            URLEncoder.encode(p.categoria, StandardCharsets.UTF_8.toString())

                        navController.navigate(
                            "${Route.ProductoDetalle.path}?id=${p.id}&nombre=$nombre&precio=$precio&categoria=$categoria"
                        )
                    },
                    onCarritoClick = {
                        navController.navigate(Route.Carrito.path) { launchSingleTop = true }
                    }
                )
            }

            // Login
            composable(Route.Login.path) {
                LoginScreenVm(
                    vm = authViewModel,
                    onLoginOkNavigateHome = {
                        navController.navigate(Route.Home.path) {
                            launchSingleTop = true
                            popUpTo(Route.Home.path) { inclusive = false }
                        }
                    },
                    onGoRegister = {
                        navController.navigate(Route.Register.path) { launchSingleTop = true }
                    },
                    onForgot = {                                      // <-- AGREGADO
                        navController.navigate(Route.Forgot.path) {   // navega al flujo de recuperación
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Register
            composable(Route.Register.path) {
                RegisterScreenVm(
                    vm = authViewModel,
                    onRegisteredNavigateLogin = {
                        navController.navigate(Route.Login.path) { launchSingleTop = true }
                    },
                    onGoLogin = {
                        navController.navigate(Route.Login.path) { launchSingleTop = true }
                    }
                )
            }

            // Perfil
            composable(Route.Perfil.path) {
                if (!header.isLoggedIn) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Route.Login.path) { launchSingleTop = true }
                    }
                } else {
                    val role = when (header.role) {
                        "ADMINISTRADOR" -> UserRole.ADMINISTRADOR
                        "DESPACHADOR" -> UserRole.DESPACHADOR
                        else -> UserRole.USUARIO
                    }
                    PerfilScreen(
                        role = role,
                        onLogout = { authViewModel.logout() },
                        onGoAdmin = { navController.navigate(Route.VistaModerador.path) },
                        onGoDespachador = { navController.navigate(Route.Despachador.path) },
                        navController = navController // 👈 agrega esto
                    )

                }
            }

            // Carrito
            composable(Route.Carrito.path) {
                CarroScreen(
                    vm = cartVm,
                    onGoCheckout = {
                        navController.navigate(Route.Checkout.path) { launchSingleTop = true }
                    }
                )
            }

            // Checkout
            composable(Route.Checkout.path) {
                CheckoutScreen(
                    cartVm = cartVm,
                    onGoPerfil = {
                        navController.navigate(Route.Perfil.path) { launchSingleTop = true }
                    },
                    onPaidSuccess = { pedidoId, totalSnapshot, metodo ->
                        navController.navigate(
                            buildPaymentProcessingPath(
                                id = pedidoId,
                                total = totalSnapshot,
                                metodo = metodo.name // "DEBITO"/"CREDITO"
                            )
                        )
                    }
                )
            }

            // NUEVO: pantalla de proceso de pago (spinner → confirmación)
            composable(
                route = Route.PaymentProcessing.path,
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                    navArgument("total") { type = NavType.IntType },
                    navArgument("m") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                val total = backStackEntry.arguments?.getInt("total") ?: 0
                val m = backStackEntry.arguments?.getString("m") ?: "DEBITO"
                PaymentProcessingScreen(
                    pedidoId = id,
                    total = total,
                    metodo = if (m == "CREDITO") MetodoPago.CREDITO else MetodoPago.DEBITO,
                    onFinish = {
                        // Decide dónde volver. Ej: Home:
                        navController.popBackStack(Route.Home.path, inclusive = false)
                    }
                )
            }

            // Admin/Dev
            composable(Route.VistaModerador.path) {
                val developerVm: DeveloperViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                val productoVm: ProductoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                DeveloperScreen(
                    vm = developerVm,
                    vmProducto = productoVm
                )
            }

            // Despachador
            composable(Route.Despachador.path) {
                DespachadorScreen()
            }
            // Historial de compras
            composable(Route.HistorialCompras.path) {
                HistorialComprasScreen(navController)
            }

// Detalle de compra
            composable(
                route = "${Route.DetalleCompra.path}/{id}/{fecha}/{total}/{productos}"
            ) { backStack ->
                val id = backStack.arguments?.getString("id")?.toIntOrNull() ?: 0
                val fecha = java.net.URLDecoder.decode(
                    backStack.arguments?.getString("fecha") ?: "",
                    java.nio.charset.StandardCharsets.UTF_8.toString()
                )
                val total = backStack.arguments?.getString("total")?.toLongOrNull() ?: 0
                val productosStr = java.net.URLDecoder.decode(
                    backStack.arguments?.getString("productos") ?: "",
                    java.nio.charset.StandardCharsets.UTF_8.toString()
                )
                val productos = productosStr.split("|")

                DetalleCompraScreen(
                    idCompra = id,
                    fecha = fecha,
                    total = total,
                    productos = productos,
                    navController = navController
                )
            }


            // Detalle de producto (query params)
            composable("${Route.ProductoDetalle.path}?id={id}&nombre={nombre}&precio={precio}&categoria={categoria}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toIntOrNull() ?: -1
                fun dec(s: String?) = java.net.URLDecoder.decode(
                    s ?: "",
                    java.nio.charset.StandardCharsets.UTF_8.toString()
                )

                val nombre = dec(backStack.arguments?.getString("nombre")).ifBlank { "Producto" }
                val precio = dec(backStack.arguments?.getString("precio")).ifBlank { "$0" }
                val categoria =
                    dec(backStack.arguments?.getString("categoria")).ifBlank { "polera" }

                DetalleProductoScreen(
                    id = id,
                    nombre = nombre,
                    precio = precio,
                    categoria = categoria,
                    cartVm = cartVm,
                    onAddedToCart = {
                        navController.navigate(Route.Home.path) {
                            launchSingleTop = true
                            popUpTo(Route.Home.path) { inclusive = false }
                        }
                    }
                )
            }

            // Olvidé contraseña
            composable(Route.Forgot.path) {
                ForgotPasswordScreenVm(
                    vm = authViewModel,   // <<-- PASAMOS EL MISMO VM
                    onDoneGoLogin = {
                        navController.navigate(Route.Login.path) {
                            launchSingleTop = true
                            popUpTo(Route.Login.path) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
