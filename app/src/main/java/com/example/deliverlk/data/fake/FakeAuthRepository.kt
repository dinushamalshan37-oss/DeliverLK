package com.example.deliverlk.data.fake

import com.example.deliverlk.domain.model.Role
import com.example.deliverlk.domain.model.User
import com.example.deliverlk.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> {
        delay(600) // Simulate network delay
        val cleanEmail = email.trim().lowercase()
        val user = when {
            cleanEmail.contains("manager") -> FakeData.demoManager
            cleanEmail.contains("driver") -> FakeData.demoDriver
            cleanEmail.contains("customer") -> FakeData.demoCustomer
            cleanEmail == FakeData.demoManager.email -> FakeData.demoManager
            cleanEmail == FakeData.demoDriver.email -> FakeData.demoDriver
            cleanEmail == FakeData.demoCustomer.email -> FakeData.demoCustomer
            else -> User(
                id = "usr_${System.currentTimeMillis()}",
                name = "Logged User",
                email = email,
                phone = "+94 77 000 0000",
                role = Role.MANAGER
            )
        }
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: Role
    ): Result<User> {
        delay(800)
        val user = User(
            id = "usr_${System.currentTimeMillis()}",
            name = name,
            email = email,
            phone = phone,
            role = role
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun logout() {
        delay(300)
        _currentUser.value = null
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }

    override suspend fun updateProfile(name: String, phone: String, companyOrVehicle: String): Result<User> {
        delay(400)
        val current = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        val updated = current.copy(name = name.trim(), phone = phone.trim(), company = companyOrVehicle.trim())
        _currentUser.value = updated
        return Result.success(updated)
    }
}
