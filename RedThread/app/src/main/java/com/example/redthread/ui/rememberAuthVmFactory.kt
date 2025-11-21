package com.example.redthread.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.repository.UserRepository
import com.example.redthread.ui.viewmodel.AuthViewModel

class AuthVmFactory(
    private val repository: UserRepository,
    private val sessionPrefs: SessionPrefs
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository, sessionPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun rememberAuthViewModel(repository: UserRepository): AuthViewModel {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as Application
    val session = remember { SessionPrefs(app) }
    val factory = remember { AuthVmFactory(repository, session) }
    return viewModel(factory = factory)
}
