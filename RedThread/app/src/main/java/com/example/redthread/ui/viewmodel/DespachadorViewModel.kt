package com.example.redthread.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.repository.DespachadorRepository
import kotlinx.coroutines.launch
import java.io.File

data class Ruta(
    val id: Long,
    val nombre: String,
    val descripcion: String = "",
    val totalPedidos: Int = 0,
    val totalPrice: Long = 0
)

data class Pedido(
    val id: Int,           // shipmentId
    val orderId: Long,
    val nombre: String,    // placeholder
    val imagen: String,    // placeholder drawable
    val estado: String,
    val direccion: String,
    val mensaje: String = "",
    val motivoDevolucion: String = ""
)

class DespachadorViewModel : ViewModel() {

    private val repo = DespachadorRepository()

    val etapas = listOf("Recoger", "Entregar", "Retorno")
    val etapaSeleccionada = mutableStateOf("Recoger")

    val cargando = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    val rutasActivas = mutableStateListOf<Ruta>()
    val rutaSeleccionada = mutableStateOf<Ruta?>(null)

    val pendientes = mutableStateListOf<Pedido>()
    val porEntregar = mutableStateListOf<Pedido>()
    val retornos = mutableStateListOf<Pedido>()

    init {
        cargarRutasActivas()
    }

    fun cargarRutasActivas() {
        viewModelScope.launch {
            cargando.value = true
            error.value = null
            try {
                val data = repo.rutasActivas().map {
                    Ruta(
                        id = it.id,
                        nombre = it.nombre,
                        descripcion = it.descripcion,
                        totalPedidos = it.totalPedidos,
                        totalPrice = it.totalPrice
                    )
                }
                rutasActivas.clear()
                rutasActivas.addAll(data)
            } catch (e: Exception) {
                error.value = e.message ?: "Error cargando rutas activas."
            } finally {
                cargando.value = false
            }
        }
    }

    fun tomarRuta(routeId: Long) {
        viewModelScope.launch {
            cargando.value = true
            error.value = null
            try {
                val r = repo.tomarRuta(routeId)
                rutaSeleccionada.value = Ruta(
                    id = r.id,
                    nombre = r.nombre,
                    descripcion = r.descripcion,
                    totalPedidos = r.totalPedidos,
                    totalPrice = r.totalPrice
                )
                cargarPedidosRuta()
            } catch (e: Exception) {
                error.value = e.message ?: "No se pudo tomar ruta."
            } finally {
                cargando.value = false
            }
        }
    }

    fun cambiarEtapa(etapa: String) {
        etapaSeleccionada.value = etapa
    }

    fun cargarPedidosRuta() {
        val ruta = rutaSeleccionada.value ?: return
        viewModelScope.launch {
            cargando.value = true
            error.value = null
            try {
                val shipments = repo.shipmentsDeRuta(ruta.id)

                val allPedidos = shipments.map { s ->
                    Pedido(
                        id = s.id.toInt(),
                        orderId = s.orderId,
                        nombre = "Pedido #${s.orderId}",
                        imagen = "ic_box", // drawable placeholder
                        estado = s.status ?: "PENDING_PICKUP",
                        direccion = listOfNotNull(
                            s.addressLine1, s.addressLine2, s.city, s.state, s.zip, s.country
                        ).joinToString(", ")
                    )
                }

                pendientes.clear()
                porEntregar.clear()
                retornos.clear()

                allPedidos.forEach { p ->
                    when (p.estado) {
                        "PENDING_PICKUP", "ASSIGNED" -> pendientes.add(p)
                        "IN_TRANSIT" -> porEntregar.add(p)
                        "FAILED" -> retornos.add(p)
                    }
                }

            } catch (e: Exception) {
                error.value = e.message ?: "Error cargando pedidos de ruta."
            } finally {
                cargando.value = false
            }
        }
    }

    fun recogerPedido(shipmentId: Int) {
        viewModelScope.launch {
            try {
                repo.startShipment(shipmentId.toLong())
                cargarPedidosRuta()
            } catch (e: Exception) {
                error.value = e.message ?: "No se pudo marcar recogido."
            }
        }
    }

    fun confirmarEntrega(shipmentId: Int, receiverName: String, evidencia: File, lat: Double?, lng: Double?) {
        viewModelScope.launch {
            try {
                repo.delivered(shipmentId.toLong(), receiverName, evidencia, lat, lng)
                cargarPedidosRuta()
            } catch (e: Exception) {
                error.value = e.message ?: "No se pudo confirmar entrega."
            }
        }
    }

    fun marcarDevuelto(shipmentId: Int, motivo: String, evidencia: File, lat: Double?, lng: Double?) {
        viewModelScope.launch {
            try {
                repo.fail(shipmentId.toLong(), motivo, evidencia, lat, lng)
                cargarPedidosRuta()
            } catch (e: Exception) {
                error.value = e.message ?: "No se pudo marcar devolución."
            }
        }
    }
}
