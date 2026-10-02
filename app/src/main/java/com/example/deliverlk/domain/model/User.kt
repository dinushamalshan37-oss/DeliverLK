package com.example.deliverlk.domain.model

enum class Role {
    MANAGER,
    DRIVER,
    CUSTOMER
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: Role,
    val company: String = "DeliverLK Logistics Sri Lanka"
)
