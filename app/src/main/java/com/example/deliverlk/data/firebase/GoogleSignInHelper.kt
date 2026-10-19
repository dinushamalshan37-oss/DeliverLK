package com.example.deliverlk.data.firebase

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.deliverlk.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleSignInHelper(private val context: Context) {

    private val credentialManager by lazy { CredentialManager.create(context) }

    suspend fun getGoogleIdToken(): Result<String?> {
        return try {
            val serverClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                "340601597786-vpma00rkola0hh38morde1n2pm8jtji8.apps.googleusercontent.com"
            }
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleIdTokenCredential.idToken)
            } else {
                Result.failure(Exception("Unsupported credential type returned."))
            }
        } catch (e: GetCredentialCancellationException) {
            // User closed the sheet / cancelled: no error
            Result.success(null)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}