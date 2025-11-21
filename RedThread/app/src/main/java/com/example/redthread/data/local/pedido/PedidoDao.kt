package com.example.redthread.data.local.pedido

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedido ORDER BY fecha DESC")
    fun observarTodos(): Flow<List<PedidoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pedido: PedidoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturningId(pedido: PedidoEntity): Long
    @Query("SELECT * FROM pedido WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PedidoEntity?


    @Update
    suspend fun update(pedido: PedidoEntity)

    @Delete
    suspend fun delete(pedido: PedidoEntity)
}
