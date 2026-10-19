package com.example.deliverlk.data.local

import android.content.Context
import com.example.deliverlk.domain.model.*
import com.example.deliverlk.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SqliteAuthRepository(context: Context) : AuthRepository {
    private val dbHelper = AppDatabaseHelper.getInstance(context)

    private val _currentUser = MutableStateFlow<User?>(
        User(
            id = "mgr_01",
            name = "Kasun Perera",
            email = "manager@deliverlk.lk",
            phone = "+94 77 123 4567",
            role = Role.MANAGER,
            company = "DeliverLK Logistics Sri Lanka",
            vehicleId = null,
        )
    )
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> {
        val user = User(
            id = "mgr_01",
            name = "Kasun Perera",
            email = email,
            phone = "+94 77 123 4567",
            role = Role.MANAGER,
            company = "DeliverLK Logistics Sri Lanka",
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun register(name: String, phone: String, email: String, password: String, role: Role): Result<User> {
        val user = User(
            id = "usr_01",
            name = name,
            email = email,
            phone = phone,
            role = role,
            company = "DeliverLK Logistics Sri Lanka",
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun signInWithGoogle(context: Context): Result<GoogleSignInResult> {
        val user = User(
            id = "google_01",
            name = "Google User",
            email = "google@deliverlk.lk",
            phone = "+94 77 000 0000",
            role = Role.CUSTOMER,
            company = "DeliverLK Logistics Sri Lanka",
        )
        _currentUser.value = user
        return Result.success(GoogleSignInResult.Success(user))
    }

    override suspend fun completeGoogleRegistration(uid: String, name: String, email: String, phone: String, role: Role): Result<User> {
        val user = User(
            id = uid,
            name = name,
            email = email,
            phone = phone,
            role = role,
            company = "DeliverLK Logistics Sri Lanka",
        )
        _currentUser.value = user
        return Result.success(user)
    }
}
