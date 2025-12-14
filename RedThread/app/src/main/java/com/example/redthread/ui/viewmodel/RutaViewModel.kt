package com.example.redthread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.local.database.AppDatabase
import com.example.redthread.data.local.ruta.RutaEntity
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.CreateRouteRequest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RutaViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getInstance(app).rutaDao()
    private val deliveryApi = ApiClient.delivery   // Retrofit delivery-service

    val rutas = dao.observarTodas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun crearRuta(nombre: String, pedidosSeleccionados: List<Long>) {
        viewModelScope.launch {
            // 1) Crear ruta REAL en delivery-service
            try {
                val req = CreateRouteRequest(
                    nombre = nombre,
                    descripcion = "",
                    orderIds = pedidosSeleccionados,
                    totalPrice = null
                )
                val created = deliveryApi.createRoute(req)

                // 2) Guardar snapshot local solo para UI admin
                val r = RutaEntity(
                    nombre = created.nombre,
                    pedidosIds = pedidosSeleccionados.joinToString(","),
                    activa = true
                )
                dao.upsert(r)

            } catch (e: Exception) {
                // Si quieres, después lo conectamos a un state error para mostrarlo en UI
                e.printStackTrace()
            }
        }
    }

    fun crearRutaConPedidos(pedidos: List<Long>) {
        if (pedidos.isEmpty()) return

        val nombreRuta = "Ruta ${System.currentTimeMillis()}"

        crearRuta(
            nombre = nombreRuta,
            pedidosSeleccionados = pedidos
        )
    }


    fun actualizarRuta(ruta: RutaEntity) {
        viewModelScope.launch { dao.update(ruta) }
    }

    fun eliminarRuta(ruta: RutaEntity) {
        viewModelScope.launch { dao.delete(ruta) }
    }
}
