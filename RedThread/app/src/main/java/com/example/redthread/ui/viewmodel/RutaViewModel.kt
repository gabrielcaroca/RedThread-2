package com.example.redthread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.local.database.AppDatabase
import com.example.redthread.data.local.ruta.RutaEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RutaViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).rutaDao()

    val rutas = dao.observarTodas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun crearRuta(nombre: String, pedidosSeleccionados: List<Long>) {
        viewModelScope.launch {
            val r = RutaEntity(
                nombre = nombre,
                pedidosIds = pedidosSeleccionados.joinToString(",")
            )
            dao.upsert(r)
        }
    }

    fun actualizarRuta(ruta: RutaEntity) {
        viewModelScope.launch { dao.update(ruta) }
    }

    fun eliminarRuta(ruta: RutaEntity) {
        viewModelScope.launch { dao.delete(ruta) }
    }
}
