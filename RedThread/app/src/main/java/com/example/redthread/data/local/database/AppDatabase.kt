package com.example.redthread.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.redthread.data.local.address.AddressEntity
import com.example.redthread.data.local.address.AddressDao
import com.example.redthread.data.local.producto.ProductoDao
import com.example.redthread.data.local.producto.ProductoEntity
import com.example.redthread.data.local.pedido.PedidoDao
import com.example.redthread.data.local.pedido.PedidoEntity
import com.example.redthread.data.local.ruta.RutaDao
import com.example.redthread.data.local.ruta.RutaEntity
import com.example.redthread.data.local.user.UserDao
import com.example.redthread.data.local.user.UserEntity
import com.example.redthread.domain.enums.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProductoEntity::class,
        PedidoEntity::class,
        RutaEntity::class,
        AddressEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun rutaDao(): RutaDao
    abstract fun addressDao(): AddressDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "redthread.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).userDao()

                                val seed = listOf(
                                    UserEntity(
                                        name = "Admin",
                                        email = "admin@redthread.cl",
                                        phone = "+56911111111",
                                        password = "Admin123!",
                                        role = UserRole.ADMINISTRADOR
                                    ),
                                    UserEntity(
                                        name = "Cliente Demo",
                                        email = "cliente@redthread.cl",
                                        phone = "+56922222222",
                                        password = "123456",
                                        role = UserRole.USUARIO
                                    ),
                                    UserEntity(
                                        name = "Despachador",
                                        email = "despachador@redthread.cl",
                                        phone = "+569333333333",
                                        password = "Despachador123!",
                                        role = UserRole.DESPACHADOR
                                    )
                                )

                                if (dao.count() == 0) seed.forEach { dao.insert(it) }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
