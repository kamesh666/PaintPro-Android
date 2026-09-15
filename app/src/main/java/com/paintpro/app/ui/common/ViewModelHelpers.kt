package com.paintpro.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.paintpro.app.PaintProApplication
import com.paintpro.app.di.AppContainer

/** Reads the process-wide [AppContainer] held by [PaintProApplication]. */
@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current.applicationContext as PaintProApplication
    return context.container
}

/** Builds a one-off [ViewModelProvider.Factory] from a plain constructor lambda. */
fun <VM : ViewModel> viewModelFactory(create: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
            create() as T
    }
