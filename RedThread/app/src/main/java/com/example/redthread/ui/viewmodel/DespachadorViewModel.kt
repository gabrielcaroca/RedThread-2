package com.example.redthread.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.remote.toRutaUi
import com.example.redthread.data.remote.toPedidoUi
import com.example.redthread.data.repository.DespachadorRepository
import kotlinx.coroutines.launch
import java.io.File

class DespachadorViewModel : ViewModel() {

    private val repo = DespachadorRepository()

    val rutasActivas = mutableStateListOf<Ruta>()
    var rutaSeleccionada = mutableStateOf<Ruta?>(null)

    val etapas = listOf("Recoger", "Entregar", "Retorno")
    var etapaSeleccionada = mutableStateOf("Recoger")

    val pendientes = mutableStateListOf<Pedido>()
    val porEntregar = mutableStateListOf<Pedido>()
    val retornos = mutableStateListOf<Pedido>()

    var cargando = mutableStateOf(false)
    var error = mutableStateOf<String?>(null)

    init { cargarRutasActivas() }

    fun cambiarEtapa(nueva: String) {
        etapaSeleccionada.value = nueva
    }

    fun cargarRutasActivas() = viewModelScope.launch {
        cargando.value = true
        runCatching { repo.rutasActivas() }
            .onSuccess { lista ->
                rutasActivas.clear()
                rutasActivas.addAll(lista.map { it.toRutaUi() })
            }
            .onFailure { error.value = it.message }
        cargando.value = false
    }

    fun tomarRuta(rutaId: Long) = viewModelScope.launch {
        cargando.value = true
        runCatching { repo.tomarRuta(rutaId) }
            .onSuccess { rutaDto ->
                rutaSeleccionada.value = rutaDto.toRutaUi()
                cargarPedidosDeRuta(rutaDto.id)
            }
            .onFailure { error.value = it.message }
        cargando.value = false
    }

    fun cargarPedidosDeRuta(rutaId: Long) = viewModelScope.launch {
        runCatching { repo.pedidosDeRuta(rutaId) }
            .onSuccess { lista ->
                val pedidos = lista.map { it.toPedidoUi() }
                pendientes.clear()
                porEntregar.clear()
                retornos.clear()

                pendientes.addAll(pedidos.filter { it.estado == "PICKUP_PENDING" })
                porEntregar.addAll(pedidos.filter { it.estado == "OUT_FOR_DELIVERY" })
                retornos.addAll(pedidos.filter { it.estado == "RETURN_PENDING" })
            }
            .onFailure { error.value = it.message }
    }

    fun recogerPedido(id: Int) = viewModelScope.launch {
        runCatching { repo.recogerPedido(id.toLong()) }
            .onSuccess { rutaSeleccionada.value?.id?.let { cargarPedidosDeRuta(it) } }
            .onFailure { error.value = it.message }
    }

    fun confirmarEntrega(id: Int, file: File) = viewModelScope.launch {
        runCatching { repo.confirmarEntrega(id.toLong(), file) }
            .onSuccess { rutaSeleccionada.value?.id?.let { cargarPedidosDeRuta(it) } }
            .onFailure { error.value = it.message }
    }

    fun marcarDevuelto(id: Int, motivo: String, file: File) = viewModelScope.launch {
        runCatching { repo.devolverPedido(id.toLong(), motivo, file) }
            .onSuccess { rutaSeleccionada.value?.id?.let { cargarPedidosDeRuta(it) } }
            .onFailure { error.value = it.message }
    }
}

data class Ruta(
    val id: Long,
    val nombre: String,
    val descripcion: String = "",
    val totalPedidos: Int = 0
)

data class Pedido(
    val id: Int,
    val nombre: String,
    val imagen: String,
    val estado: String = "PICKUP_PENDING",
    val direccion: String = "",
    val mensaje: String = "",
    val motivoDevolucion: String = ""
)
