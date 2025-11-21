package com.example.redthread.data.local.address

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {

    @Query("SELECT * FROM address WHERE userId = :userId ORDER BY predeterminada DESC, id DESC")
    fun observarPorUsuario(userId: Int): Flow<List<AddressEntity>>

    @Query("SELECT * FROM address WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(address: AddressEntity): Long

    @Update
    suspend fun update(address: AddressEntity)

    @Delete
    suspend fun delete(address: AddressEntity)

    // Asegura que sólo una dirección quede como predeterminada por usuario
    @Query("UPDATE address SET predeterminada = 0 WHERE userId = :userId")
    suspend fun clearDefault(userId: Int)

    @Query("UPDATE address SET predeterminada = 1 WHERE id = :addressId")
    suspend fun setDefault(addressId: Long)
}
