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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.saving.app.auth.DriveAuth
import com.saving.app.data.db.AppDatabase
import com.saving.app.data.goal.GoalStore
import com.saving.app.data.repository.SavingRepository
import com.saving.app.data.sync.SyncManager
import com.saving.app.ui.screens.HomeScreen
import com.saving.app.ui.theme.SavingTheme
import com.saving.app.viewmodel.MainViewModel
import com.saving.app.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch

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
        val goalStore = GoalStore(applicationContext)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository, syncManager, goalStore)
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

                // Fires only after the current account's data has been successfully synced
                // and uploaded to Drive (see MainViewModel.requestSwitchAccount). Signing out
                // of the Google client forces the account picker to appear on the next
                // sign-in intent, instead of silently re-selecting the same account.
                val switchAccountRequested by viewModel.switchAccountRequested.collectAsState()
                LaunchedEffect(switchAccountRequested) {
                    if (switchAccountRequested) {
                        viewModel.consumeSwitchAccountRequest()
                        // Must be awaited — launching the picker before sign-out actually
                        // finishes is what let Google silently reuse the cached account.
                        DriveAuth.signOut(this@MainActivity)
                        viewModel.setSignedInAccount(null)
                        signInLauncher.launch(DriveAuth.getSignInClient(this@MainActivity).signInIntent)
                    }
                }

                HomeScreen(
                    viewModel = viewModel,
                    onSignInClick = {
                        signInLauncher.launch(DriveAuth.getSignInClient(this).signInIntent)
                    },
                    onSignOutClick = {
                        lifecycleScope.launch {
                            DriveAuth.signOut(this@MainActivity)
                            viewModel.setSignedInAccount(null)
                        }
                    },
                    // Dedicated top-bar button: backs up the current account's data to
                    // Drive first, then (via switchAccountRequested above) signs out and
                    // shows Google's account picker so a different login can be chosen.
                    onSwitchAccountClick = { viewModel.requestSwitchAccount() }
                )
            }
        }
    }
}
