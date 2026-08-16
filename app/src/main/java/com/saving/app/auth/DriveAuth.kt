package com.saving.app.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Result of trying to get Drive access. NeedsConsent happens the first time on a given
 *  device/account combo (or if access was revoked) — the caller must launch the included
 *  IntentSender to show Google's one-time consent screen, then call
 *  DriveAuth.finishAuthorization() with the result. */
sealed class DriveAccessResult {
    data class Authorized(val accessToken: String) : DriveAccessResult()
    data class NeedsConsent(val intentSender: IntentSender) : DriveAccessResult()
    data class Failed(val message: String) : DriveAccessResult()
}

object DriveAuth {

    const val SCOPE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"

    /** Sign-in here only establishes "who is this user" (email/profile) for display purposes.
     *  Actual Drive access is requested separately via requestDriveAccess() below, using the
     *  Authorization API — unlike the older GoogleAuthUtil token API, this does NOT require
     *  the Google account to already be registered at the device/OS level, which was the
     *  actual cause of sync silently failing on a second phone. */
    fun getSignInClient(context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        return GoogleSignIn.getClient(context, options)
    }

    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    suspend fun requestDriveAccess(context: Context): DriveAccessResult = withContext(Dispatchers.IO) {
        try {
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(SCOPE_APPDATA)))
                .build()
            val result = Tasks.await(Identity.getAuthorizationClient(context).authorize(request))
            toDriveAccessResult(result)
        } catch (e: Exception) {
            DriveAccessResult.Failed(e.message ?: "Could not request Drive access")
        }
    }

    /** Call this from the ActivityResultLauncher callback after launching a NeedsConsent
     *  IntentSender, passing the resulting Intent data. */
    fun finishAuthorization(context: Context, data: Intent?): DriveAccessResult {
        return try {
            val result = Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(data)
            toDriveAccessResult(result)
        } catch (e: Exception) {
            DriveAccessResult.Failed(e.message ?: "Drive authorization was not completed")
        }
    }

    private fun toDriveAccessResult(result: AuthorizationResult): DriveAccessResult {
        val token = result.accessToken
        return when {
            token != null -> DriveAccessResult.Authorized(token)
            result.hasResolution() && result.pendingIntent != null ->
                DriveAccessResult.NeedsConsent(result.pendingIntent!!.intentSender)
            else -> DriveAccessResult.Failed("Drive access was not granted")
        }
    }
}
