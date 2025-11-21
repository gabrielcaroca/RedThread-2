package com.example.redthread.data.local.producto

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun observarTodos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE destacado = 1")
    fun obtenerDestacados(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE categoria = :categoria")
    fun observarPorCategoria(categoria: String): Flow<List<ProductoEntity>>

    @Query("SELECT DISTINCT subcategoria FROM productos ORDER BY subcategoria ASC")
    fun obvervarSubcategorias(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(producto: ProductoEntity)

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun count(): Int
}
