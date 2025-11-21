// app/src/main/java/com/example/redthread/MainActivity.kt
package com.example.redthread

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.example.redthread.navigation.AppNavGraph
import com.example.redthread.ui.rememberAuthViewModel
import com.example.redthread.ui.theme.RedThreadTheme
import com.example.redthread.data.local.database.AppDatabase
import com.example.redthread.data.repository.UserRepository

class MainActivity : ComponentActivity() {

    private fun setupEdgeToEdge() {
        // Dibuja contenido detrás de las system bars (status/nav)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Mantenemos la status bar visible pero transparente (definido en el tema).
        // Como tu TopBar es oscuro, dejamos íconos claros (false).
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false  // iconos claros (fondo oscuro)
        // Si quisieras iconos oscuros (fondo claro): controller.isAppearanceLightStatusBars = true

        // La nav bar puede quedar negra (coincide con tu tema).
        // El color ya lo define el tema, no forzamos nada aquí.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()

        setContent {
            RedThreadTheme {
                val db = AppDatabase.getInstance(applicationContext)
                val userDao = db.userDao()
                val userRepository = UserRepository(userDao)
                val authVm = rememberAuthViewModel(userRepository)
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    authViewModel = authVm
                )
            }
        }
    }
}
