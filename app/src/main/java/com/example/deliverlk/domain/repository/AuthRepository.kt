package com.example.deliverlk.domain.repository

import android.content.Context
import com.example.deliverlk.domain.model.GoogleSignInResult
import com.example.deliverlk.domain.model.Role
import com.example.deliverlk.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, phone: String, email: String, password: String, role: Role): Result<User>
    suspend fun logout()
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signInWithGoogle(context: Context): Result<GoogleSignInResult>
    suspend fun completeGoogleRegistration(uid: String, name: String, email: String, phone: String, role: Role): Result<User>
}