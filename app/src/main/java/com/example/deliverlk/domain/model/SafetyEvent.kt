package com.example.deliverlk.domain.model

enum class SafetyEventType {
    SPEEDING,
    HARSH_BRAKING,
    HARSH_CORNERING
}

data class SafetyEvent(
    val id: String,
    val driverId: String,
    val type: SafetyEventType,
    val speedKmH: Int,
    val locationText: String,
    val timestamp: String
)
