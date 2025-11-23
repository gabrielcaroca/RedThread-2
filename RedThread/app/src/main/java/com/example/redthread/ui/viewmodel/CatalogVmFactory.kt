package com.example.redthread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.redthread.data.repository.CatalogRepository

class CatalogVmFactory(
    private val app: Application,
    private val repo: CatalogRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogViewModel::class.java)) {
            return CatalogViewModel(app, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
