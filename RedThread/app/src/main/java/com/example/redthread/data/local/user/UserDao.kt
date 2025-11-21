package com.example.redthread.data.local.user
import androidx.room.*      // Para @Dao, @Query, @Insert, @Update, etc.
import kotlinx.coroutines.flow.Flow // Para los retornos reactivos de Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// @Dao define las operaciones permitidas sobre la tabla de usuarios.
@Dao
interface UserDao {

    // Inserta un usuario. Si el ID ya existe, aborta la inserción.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET email = :email, phone = :phone WHERE id = :id")
    suspend fun updateContact(id: Int, email: String, phone: String)

    @Query("UPDATE users SET password = :password WHERE id = :id")
    suspend fun updatePassword(id: Int, password: String)

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): UserEntity?

    @Query("UPDATE users SET password = :password WHERE email = :email")
    suspend fun updatePasswordByEmail(email: String, password: String)

    @Query("UPDATE users SET password = :password WHERE phone = :phone")
    suspend fun updatePasswordByPhone(phone: String, password: String)

    // Devuelve un usuario por su email, o null si no existe.
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): UserEntity?

    // Devuelve la cantidad total de usuarios registrados.
    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    // Devuelve la lista completa de usuarios (para administración o debug).
    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAll(): List<UserEntity>

}
//@Dao
//interface PedidoDao {
//
//    @Query("SELECT * FROM pedidos")
//    fun obtenerPedidos(): Flow<List<PedidoEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertarPedido(pedido: PedidoEntity)
//
//    @Update
//    suspend fun actualizarPedido(pedido: PedidoEntity)
//
//    @Query("UPDATE pedidos SET estado = :nuevoEstado WHERE id = :id")
//    suspend fun cambiarEstado(id: Int, nuevoEstado: String)
//}