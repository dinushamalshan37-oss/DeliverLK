package com.example.deliverlk.domain.model

sealed interface GoogleSignInResult {
    data class Success(val user: User) : GoogleSignInResult
    data class NewUserNeedsRole(
        val uid: String,
        val name: String,
        val email: String
    ) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
}