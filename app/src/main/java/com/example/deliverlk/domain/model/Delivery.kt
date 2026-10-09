package com.example.deliverlk.domain.model

enum class DeliveryStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED
}

enum class DeliveryPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

data class Delivery(
    val id: String,
    val trackingCode: String,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val itemsDescription: String,
    val specialInstructions: String,
    val priority: DeliveryPriority = DeliveryPriority.MEDIUM,
    val assignedDriverId: String? = null,
    val assignedDriverName: String? = null,
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val etaMinutes: Int = 30,
    val distanceKm: Double = 4.5,
    val deliveryTime: String? = null,
    val proofPhotoUri: String? = null,
    val recipientName: String? = null,
    val note: String? = null,
    val payoutLkr: Double = 450.0,
    val stopNumber: Int = 1
)
