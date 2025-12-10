package com.example.redthread.navigation

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.redthread.ui.components.AppTopBar
import com.example.redthread.ui.screen.*
import com.example.redthread.ui.screen.catalog.CreateProductScreen
import com.example.redthread.ui.screen.catalog.CreateVariantScreen
import com.example.redthread.ui.screen.catalog.UploadImageScreen
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.viewmodel.*
import com.example.redthread.domain.enums.UserRole
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.repository.CatalogRepository
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val header by authViewModel.header.collectAsState()

    val cartVm: CartViewModel = viewModel()
    val cartCount by cartVm.count.collectAsState()

    LaunchedEffect(header.isLoggedIn, header.email) {
        if (header.isLoggedIn) cartVm.refreshFromBackendIfLogged()
        else cartVm.clear()
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
                    if (authViewModel.header.value.isLoggedIn)
                        navController.navigate(Route.Perfil.path)
                    else
                        navController.navigate(Route.Login.path)
                },
                onCarritoClick = { navController.navigate(Route.Carrito.path) },
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

            // ============================================================
            // HOME (ARREGLADO — antes causaba el crash)
            // ============================================================
            composable(Route.Home.path) {

                val app = LocalContext.current.applicationContext as Application
                val repo = CatalogRepository(ApiClient.catalog)
                val factory = CatalogVmFactory(app, repo)
                val catalogVm: CatalogViewModel = viewModel(factory = factory)

                HomeScreen(
                    catalogVm = catalogVm,
                    onProductoClick = { p ->
                        val nombre = URLEncoder.encode(p.nombre, StandardCharsets.UTF_8.toString())
                        val precio = URLEncoder.encode(p.precio, StandardCharsets.UTF_8.toString())
                        val categoria = URLEncoder.encode(p.categoria, StandardCharsets.UTF_8.toString())

                        navController.navigate(
                            "${Route.ProductoDetalle.path}?id=${p.id}&nombre=$nombre&precio=$precio&categoria=$categoria"
                        )
                    },
                    onCarritoClick = { navController.navigate(Route.Carrito.path) }
                )
            }

            // LOGIN
            composable(Route.Login.path) {
                LoginScreenVm(
                    vm = authViewModel,
                    onCustomerNavigate = {
                        navController.navigate(Route.Home.path) {
                            launchSingleTop = true
                            popUpTo(Route.Home.path) { inclusive = false }
                        }
                    },
                    onAdminNavigate = {
                        navController.navigate(Route.VistaModerador.path) {
                            popUpTo(Route.Home.path) { inclusive = false }
                        }
                    },
                    onDriverNavigate = {
                        navController.navigate(Route.Despachador.path) {
                            popUpTo(Route.Home.path) { inclusive = false }
                        }
                    },
                    onGoRegister = { navController.navigate(Route.Register.path) },
                    onForgot = { navController.navigate(Route.Forgot.path) }
                )
            }

            // REGISTER
            composable(Route.Register.path) {
                RegisterScreenVm(
                    vm = authViewModel,
                    onRegisteredNavigateLogin = { navController.navigate(Route.Login.path) },
                    onGoLogin = { navController.navigate(Route.Login.path) }
                )
            }

            // PERFIL
            composable(Route.Perfil.path) {
                if (!header.isLoggedIn) {
                    LaunchedEffect(Unit) { navController.navigate(Route.Login.path) }
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
                        navController = navController,
                        header = header
                    )
                }
            }

            // CARRITO
            composable(Route.Carrito.path) {
                CarroScreen(
                    vm = cartVm,
                    onGoCheckout = { navController.navigate(Route.Checkout.path) }
                )
            }

            // CHECKOUT
            composable(Route.Checkout.path) {
                CheckoutScreen(
                    cartVm = cartVm,
                    onGoPerfil = { navController.navigate(Route.Perfil.path) },
                    onPaidSuccess = { id, total, metodo ->
                        navController.navigate(
                            buildPaymentProcessingPath(id, total, metodo.name)
                        )
                    }
                )
            }

            // PAYMENT
            composable(
                route = Route.PaymentProcessing.path,
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                    navArgument("total") { type = NavType.IntType },
                    navArgument("m") { type = NavType.StringType }
                )
            ) { backStack ->
                val id = backStack.arguments?.getLong("id") ?: 0L
                val total = backStack.arguments?.getInt("total") ?: 0
                val method = backStack.arguments?.getString("m") ?: "DEBITO"

                PaymentProcessingScreen(
                    pedidoId = id,
                    total = total,
                    metodo = if (method == "CREDITO") MetodoPago.CREDITO else MetodoPago.DEBITO,
                    onFinish = { navController.popBackStack(Route.Home.path, false) }
                )
            }

            // ============================================================
            // ADMIN (Vista Moderador)
            // ============================================================
            composable(Route.VistaModerador.path) {

                val app = LocalContext.current.applicationContext as Application
                val repo = CatalogRepository(ApiClient.catalog)
                val factory = CatalogVmFactory(app, repo)
                val catalogVm: CatalogViewModel = viewModel(factory = factory)

                val devVm: DeveloperViewModel = viewModel()

                DeveloperScreen(
                    vm = devVm,
                    catalogVm = catalogVm,
                    onCreateProduct = {
                        navController.navigate(Route.CrearProducto.path)
                    },
                    onEditProduct = { productId ->
                        navController.navigate("editar-producto/$productId")
                    }
                )

            }

            // DESPACHADOR
            composable(Route.Despachador.path) {
                DespachadorScreen()
            }

            // HISTORIAL COMPRAS
            composable(Route.HistorialCompras.path) {
                HistorialComprasScreen(navController)
            }

            // DETALLE COMPRA
            composable(
                route = Route.DetalleCompra.path + "/{id}/{fecha}/{total}/{productos}",
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("fecha") { type = NavType.StringType },
                    navArgument("total") { type = NavType.LongType },
                    navArgument("productos") { type = NavType.StringType }
                )
            ) { backStack ->
                val id = backStack.arguments?.getInt("id") ?: 0
                val fecha = backStack.arguments?.getString("fecha") ?: ""
                val total = backStack.arguments?.getLong("total") ?: 0L
                val productosRaw = backStack.arguments?.getString("productos") ?: ""

                val productos = if (productosRaw.isBlank()) emptyList()
                else productosRaw.split("|").map {
                    java.net.URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                }

                DetalleCompraScreen(
                    idCompra = id,
                    fecha = fecha,
                    total = total,
                    productos = productos,
                    navController = navController
                )
            }

            // DETALLE PRODUCTO
            composable(
                "${Route.ProductoDetalle.path}?id={id}&nombre={nombre}&precio={precio}&categoria={categoria}"
            ) { backStack ->

                val id = backStack.arguments?.getString("id")?.toIntOrNull() ?: -1
                fun dec(s: String?) =
                    java.net.URLDecoder.decode(s ?: "", StandardCharsets.UTF_8.toString())

                DetalleProductoScreen(
                    id = id,
                    nombre = dec(backStack.arguments?.getString("nombre")),
                    precio = dec(backStack.arguments?.getString("precio")),
                    categoria = dec(backStack.arguments?.getString("categoria")),
                    cartVm = cartVm,
                    onAddedToCart = {},
                    nav = navController                      // ← agregado
                )

            }

            // FORGOT PASSWORD
            composable(Route.Forgot.path) {
                ForgotPasswordScreenVm(
                    vm = authViewModel,
                    onDoneGoLogin = {
                        navController.navigate(Route.Login.path) {
                            popUpTo(Route.Login.path) { inclusive = true }
                        }
                    }
                )
            }

            // ============================================================
            // CREAR PRODUCTO
            // ============================================================
            composable(Route.CrearProducto.path) {

                val app = LocalContext.current.applicationContext as Application
                val repo = CatalogRepository(ApiClient.catalog)
                val factory = CatalogVmFactory(app, repo)
                val vm: CatalogViewModel = viewModel(factory = factory)

                CreateProductScreen(
                    vm = vm,
                    onNext = { id ->
                        navController.navigate("crear-variante/$id")
                    }
                )
            }

            // EDITAR PRODUCTO
            composable(
                "editar-producto/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { backStack ->

                val id = backStack.arguments?.getInt("productId") ?: 0

                val app = LocalContext.current.applicationContext as Application
                val repo = CatalogRepository(ApiClient.catalog)
                val factory = CatalogVmFactory(app, repo)
                val vm: CatalogViewModel = viewModel(factory = factory)

                CreateProductScreen(
                    vm = vm,
                    productId = id,
                    onNext = {
                        // ← ESTO ES LO QUE QUERÍAS
                        navController.popBackStack()
                    }
                )
            }





            // CREAR VARIANTE
            composable(
                route = Route.CrearVariante.path,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { backStack ->

                val productId = backStack.arguments?.getInt("productId") ?: 0

                val app = LocalContext.current.applicationContext as Application
                val repo = CatalogRepository(ApiClient.catalog)
                val factory = CatalogVmFactory(app, repo)
                val vm: CatalogViewModel = viewModel(factory = factory)

                CreateVariantScreen(
                    productId = productId,
                    vm = vm,
                    onNext = {
                        navController.navigate("subir-imagen/$productId")
                    }
                )
            }

            // SUBIR IMAGEN
            composable(
                route = Route.SubirImagen.path,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { backStack ->

                val productId = backStack.arguments?.getInt("productId") ?: 0

                val app = LocalContext.current.applicationContext as Application
                val repo = CatalogRepository(ApiClient.catalog)
                val factory = CatalogVmFactory(app, repo)
                val vm: CatalogViewModel = viewModel(factory = factory)

                UploadImageScreen(
                    productId = productId,
                    vm = vm,
                    onFinish = {
                        navController.navigate(Route.VistaModerador.path) {
                            popUpTo(Route.Home.path) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}
