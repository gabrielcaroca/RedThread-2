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

class MainActivity : ComponentActivity() {

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()

        setContent {
            RedThreadTheme {
                val navController = rememberNavController()

                // AQUÍ ES DONDE SE ARREGLA TODO
                val authVm = rememberAuthViewModel()

                AppNavGraph(
                    navController = navController,
                    authViewModel = authVm
                )
            }
        }
    }
}
