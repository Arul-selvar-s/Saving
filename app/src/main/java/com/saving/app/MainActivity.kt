package com.saving.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.saving.app.data.db.AppDatabase
import com.saving.app.data.repository.SavingRepository
import com.saving.app.ui.screens.HomeScreen
import com.saving.app.ui.theme.SavingTheme
import com.saving.app.viewmodel.MainViewModel
import com.saving.app.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(applicationContext)
        val repository = SavingRepository(database)
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository)
        )[MainViewModel::class.java]

        setContent {
            SavingTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
