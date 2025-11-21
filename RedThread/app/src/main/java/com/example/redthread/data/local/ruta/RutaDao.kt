package com.example.redthread.data.local.ruta

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RutaDao {
    @Query("SELECT * FROM rutas ORDER BY id DESC")
    fun observarTodas(): Flow<List<RutaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ruta: RutaEntity)

    @Update
    suspend fun update(ruta: RutaEntity)

    @Delete
    suspend fun delete(ruta: RutaEntity)
}
