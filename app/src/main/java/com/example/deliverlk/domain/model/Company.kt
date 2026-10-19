package com.example.deliverlk.domain.model

data class Company(
    val id: String,
    val name: String,
    val ownerUid: String? = null
)