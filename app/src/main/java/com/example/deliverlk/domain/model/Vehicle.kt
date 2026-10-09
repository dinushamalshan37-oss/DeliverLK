package com.example.deliverlk.domain.model

enum class VehicleStatus {
    MOVING,
    IDLE,
    OFFLINE
}

data class Vehicle(
    val id: String,
    val plateNumber: String,
    val driverId: String,
    val driverName: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmH: Int,
    val deliveriesRemaining: Int,
    val status: VehicleStatus,
    val lastUpdated: String,
    val heading: Double = 0.0
)
