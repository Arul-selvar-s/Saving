package com.saving.app.auth

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DriveAuth {

    private const val SCOPE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"

    fun getSignInClient(context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(SCOPE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    /** Fetches a short-lived OAuth access token for Drive calls. Returns null on any failure
     *  (no network, token expired mid-flow, consent revoked, etc.) — callers treat that as
     *  "sync unavailable right now" rather than crashing. */
    suspend fun getAccessToken(context: Context, account: GoogleSignInAccount): String? =
        withContext(Dispatchers.IO) {
            try {
                GoogleAuthUtil.getToken(context, account.account!!, "oauth2:$SCOPE_APPDATA")
            } catch (e: Exception) {
                null
            }
        }
}
