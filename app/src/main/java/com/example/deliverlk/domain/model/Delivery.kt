package com.example.deliverlk.domain.model

import com.google.firebase.firestore.GeoPoint

enum class DeliveryStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED,
    CANCELLED
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
    val stopNumber: Int = 1,
    val customerEmail: String = "",
    val customerUid: String? = null,
    val createdBy: String = "MANAGER",
    val pickupAddress: String = "",
    val pickupLocation: GeoPoint? = null,
    val receiverName: String = "",
    val receiverPhone: String = "",
    val packageWeightKg: Double? = null,
    val instructions: String = "",
    val companyId: String = "dlk_company_01"
)