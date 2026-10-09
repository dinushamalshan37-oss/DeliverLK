package com.example.deliverlk.data.firebase

import com.example.deliverlk.domain.model.Role
import com.example.deliverlk.domain.model.User
import com.example.deliverlk.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var userSnapshotListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            userSnapshotListener?.remove()
            userSnapshotListener = null

            if (firebaseUser != null) {
                userSnapshotListener = firestore.collection(FirestoreConstants.USERS)
                    .document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            _currentUser.value = snapshot.toUser()
                        } else {
                            // Fallback if user doc doesn't exist yet
                            _currentUser.value = User(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "User",
                                email = firebaseUser.email ?: "",
                                phone = firebaseUser.phoneNumber ?: "",
                                role = Role.MANAGER
                            )
                        }
                    }
            } else {
                _currentUser.value = null
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
            val firebaseUser = authResult.user ?: throw Exception("User authentication failed.")
            
            val docSnap = firestore.collection(FirestoreConstants.USERS)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = docSnap.toUser() ?: User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "User",
                email = firebaseUser.email ?: email,
                phone = firebaseUser.phoneNumber ?: "",
                role = Role.MANAGER
            )
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: Role
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password.trim()).await()
            val firebaseUser = authResult.user ?: throw Exception("Registration failed.")
            val uid = firebaseUser.uid

            val userMap = mapOf(
                FirestoreConstants.USER_NAME to name.trim(),
                FirestoreConstants.USER_EMAIL to email.trim(),
                FirestoreConstants.USER_PHONE to phone.trim(),
                FirestoreConstants.USER_ROLE to role.name,
                FirestoreConstants.USER_COMPANY_ID to FirestoreConstants.DEFAULT_COMPANY_ID,
                FirestoreConstants.USER_VEHICLE_ID to null,
                FirestoreConstants.USER_FCM_TOKEN to null,
                FirestoreConstants.USER_CREATED_AT to FieldValue.serverTimestamp()
            )

            firestore.collection(FirestoreConstants.USERS)
                .document(uid)
                .set(userMap)
                .await()

            val user = User(
                id = uid,
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                role = role,
                company = "DeliverLK Logistics Sri Lanka"
            )
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    override suspend fun logout() {
        try {
            userSnapshotListener?.remove()
            userSnapshotListener = null
            auth.signOut()
            _currentUser.value = null
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    override suspend fun updateProfile(name: String, phone: String, companyOrVehicle: String): Result<User> {
        return try {
            val firebaseUser = auth.currentUser ?: throw Exception("Not logged in.")
            val uid = firebaseUser.uid

            val updates = mutableMapOf<String, Any>(
                FirestoreConstants.USER_NAME to name.trim(),
                FirestoreConstants.USER_PHONE to phone.trim()
            )

            val currentUsr = _currentUser.value
            if (currentUsr?.role == Role.MANAGER) {
                updates[FirestoreConstants.USER_COMPANY_ID] = companyOrVehicle.trim()
            }

            firestore.collection(FirestoreConstants.USERS)
                .document(uid)
                .update(updates)
                .await()

            currentUsr?.let {
                val updated = it.copy(
                    name = name.trim(),
                    phone = phone.trim(),
                    company = if (it.role == Role.MANAGER) companyOrVehicle.trim() else it.company
                )
                _currentUser.value = updated
                Result.success(updated)
            } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    private fun mapAuthException(e: Exception): String {
        return when (e) {
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
                    "ERROR_WRONG_PASSWORD" -> "Invalid password. Please try again."
                    "ERROR_USER_NOT_FOUND" -> "No user found with this email."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
                    "ERROR_WEAK_PASSWORD" -> "The password is too weak. Choose at least 6 characters."
                    else -> e.localizedMessage ?: "Authentication error occurred."
                }
            }
            else -> e.localizedMessage ?: "An unexpected error occurred."
        }
    }

    companion object {
        fun DocumentSnapshot.toUser(): User? {
            if (!exists()) return null
            val roleStr = getString(FirestoreConstants.USER_ROLE) ?: Role.MANAGER.name
            val role = try {
                Role.valueOf(roleStr.uppercase())
            } catch (e: Exception) {
                Role.MANAGER
            }
            return User(
                id = id,
                name = getString(FirestoreConstants.USER_NAME) ?: "",
                email = getString(FirestoreConstants.USER_EMAIL) ?: "",
                phone = getString(FirestoreConstants.USER_PHONE) ?: "",
                role = role,
                company = getString(FirestoreConstants.USER_COMPANY_ID) ?: "DeliverLK Logistics Sri Lanka"
            )
        }
    }
}
