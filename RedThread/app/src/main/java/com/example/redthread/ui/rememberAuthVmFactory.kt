package com.example.redthread.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.repository.AuthRepository
import com.example.redthread.ui.viewmodel.AuthViewModel

class AuthVmFactory(
    private val context: Context,
    private val sessionPrefs: SessionPrefs
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val repo = AuthRepository(context)
            return AuthViewModel(repo, sessionPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun rememberAuthViewModel(): AuthViewModel {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as Application

    val session = remember { SessionPrefs(app) }
    val factory = remember { AuthVmFactory(context, session) }

    return viewModel(factory = factory)
}
