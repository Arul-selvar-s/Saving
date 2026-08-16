package com.saving.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.saving.app.auth.DriveAuth
import com.saving.app.data.db.AppDatabase
import com.saving.app.data.repository.SavingRepository
import com.saving.app.data.sync.SyncManager
import com.saving.app.ui.screens.HomeScreen
import com.saving.app.ui.theme.SavingTheme
import com.saving.app.viewmodel.MainViewModel
import com.saving.app.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.setSignedInAccount(account)
        } catch (e: ApiException) {
            // Sign-in was cancelled or failed — leave the app signed-out, user can retry
        }
    }

    // Handles the one-time "Allow Saving to access Drive" consent dialog, shown the first
    // time this device+account combo syncs (or after access is revoked).
    private val authorizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onAuthorizationResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(applicationContext)
        val repository = SavingRepository(database)
        val syncManager = SyncManager(applicationContext, repository)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository, syncManager)
        )[MainViewModel::class.java]

        // If already signed in from a previous session, this both restores the account
        // and immediately kicks off a sync — "when the app is opened it should refresh
        // and show the data from the saved cloud data".
        val existingAccount = DriveAuth.getLastSignedInAccount(applicationContext)
        viewModel.setSignedInAccount(existingAccount)

        setContent {
            SavingTheme {
                val authorizationNeeded by viewModel.authorizationNeeded.collectAsState()
                LaunchedEffect(authorizationNeeded) {
                    authorizationNeeded?.let { intentSender ->
                        authorizationLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build()
                        )
                    }
                }

                HomeScreen(
                    viewModel = viewModel,
                    onSignInClick = {
                        signInLauncher.launch(DriveAuth.getSignInClient(this).signInIntent)
                    },
                    onSignOutClick = {
                        DriveAuth.getSignInClient(this).signOut()
                        viewModel.setSignedInAccount(null)
                    }
                )
            }
        }
    }
}
