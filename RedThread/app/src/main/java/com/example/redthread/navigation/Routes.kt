package com.example.redthread.navigation

//Cada uno de los objetos va a representar una pantalla
//Si es necesario agregar mas.
sealed class Route(val path: String) {
    data object Home : Route("home") //1
    data object Login : Route("login") //2
    data object Register : Route("register")//3
    data object Carrito : Route("carro")//4
    data object DetalleProducto : Route("detalle_producto") //5
    data object Perfil : Route("perfil") // 6
    data object Checkout : Route("checkout") // 7
    data object Despachador : Route("despachador") // 8
    data object VistaModerador : Route("vista_moderador")
    data object ProductoDetalle : Route("product")
    data object HistorialCompras : Route("historial_compras")
    data object DetalleCompra : Route("detalle_compra")
    data object CrearProducto : Route("crear-producto")
    data object CrearVariante : Route("crear-variante/{productId}")
    data object SubirImagen : Route("subir-imagen/{productId}")
    data object EditVariant : Route("admin/variant/edit/{productId}/{variantId}")
    data object EditarProducto : Route("editar-producto/{productId}")




    data object Forgot : Route("recuperar")
    data object PaymentProcessing : Route("payment-processing?id={id}&total={total}&m={m}")
}

/* Helpers de rutas */
fun buildPaymentProcessingPath(
    id: Long,
    total: Int,
    metodo: String // "DEBITO" o "CREDITO"
): String = "payment-processing?id=$id&total=$total&m=$metodo"

/*
* Rutas principales de la aplicación
* 1. Home = Catálogo general de productos
* 2. Login = Pantalla de inicio de sesión
* 3. Register = Registro de nuevo usuario
* 4. Carrito = Muestra los productos agregados
* 5. DetalleProducto = Información de un producto seleccionado
* 6. Perfil = Datos y configuración del usuario
* 7. Checkout = Confirmación y pago de la compra
* 8. Despachador = Panel completo del despachador con gestión de pedidos y rutas
*/
