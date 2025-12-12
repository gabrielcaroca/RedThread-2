package com.example.redthread.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.repository.AddressRepository
import com.example.redthread.data.repository.AuthRepository

class ProfileVmFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {

            val sessionPrefs = SessionPrefs(context.applicationContext)

            val repo = AddressRepository(
                api = ApiClient.address,
                sessionPrefs = sessionPrefs
            )

            val authRepo = AuthRepository(
                context = context.applicationContext,
                session = sessionPrefs
            )

            return ProfileViewModel(repo, authRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
