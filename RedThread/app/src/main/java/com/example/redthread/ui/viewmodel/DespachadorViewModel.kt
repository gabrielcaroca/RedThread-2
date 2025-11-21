package com.example.redthread.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class DespachadorViewModel : ViewModel() {

    val rutas = listOf(
        Ruta("Centro", "Zona centro de la ciudad", 2500),
        Ruta("Norte", "Sector residencial norte", 3500),
        Ruta("Sur", "Sector sur industrial", 4000)
    )

    var rutaSeleccionada = mutableStateOf<Ruta?>(null)
    val etapas = listOf("Recoger", "Entregar", "Retorno")
    var etapaSeleccionada = mutableStateOf("Recoger")

    var pendientes = mutableStateListOf<Pedido>()
    var porEntregar = mutableStateListOf<Pedido>()
    var retornos = mutableStateListOf<Pedido>()

    init {
        seleccionarRuta(rutas.first())
    }

    fun cambiarEtapa(nueva: String) {
        etapaSeleccionada.value = nueva
    }

    fun seleccionarRuta(ruta: Ruta) {
        rutaSeleccionada.value = ruta
        pendientes.clear()
        porEntregar.clear()
        retornos.clear()

        when (ruta.nombre) {
            "Centro" -> pendientes.addAll(
                listOf(
                    Pedido(1, "Polera básica", "ph_polera"),
                    Pedido(2, "Zapatillas deportivas", "ph_zapatillas")
                )
            )
            "Norte" -> pendientes.addAll(
                listOf(Pedido(3, "Pantalón azul", "ph_pantalon"))
            )
            "Sur" -> pendientes.addAll(
                listOf(
                    Pedido(4, "Chaqueta impermeable", "ph_chaqueta"),
                    Pedido(5, "Accesorio urbano", "ph_accesorio")
                )
            )
        }
    }

    fun recogerPedido(index: Int) {
        val pedido = pendientes[index]
        pendientes.removeAt(index)
        porEntregar.add(pedido)
    }

    fun guardarEvidencia(index: Int, uri: Uri) {
        val actual = porEntregar[index]
        porEntregar[index] = actual.copy(fotoEvidencia = uri)
    }

    fun confirmarEntrega(index: Int) {
        porEntregar.removeAt(index)
    }

    fun marcarDevuelto(index: Int, motivo: String) {
        val pedido = porEntregar[index]
        porEntregar.removeAt(index)
        retornos.add(pedido.copy(devuelto = true, motivoDevolucion = motivo))
    }
}

data class Ruta(
    val nombre: String,
    val descripcion: String,
    val precio: Int
)

data class Pedido(
    val id: Int,
    val nombre: String,
    val imagen: String,
    val fotoEvidencia: Uri? = null,
    val devuelto: Boolean = false,
    val motivoDevolucion: String = "" // ← nuevo campo
)
