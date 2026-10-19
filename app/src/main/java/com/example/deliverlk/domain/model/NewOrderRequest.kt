package com.example.deliverlk.domain.model

import com.google.firebase.firestore.GeoPoint

data class NewOrderRequest(
    val customerEmail: String,
    val customerUid: String,
    val companyId: String,
    val pickupAddress: String,
    val pickupLocation: GeoPoint?,
    val dropoffAddress: String,
    val dropoffLocation: GeoPoint?,
    val receiverName: String,
    val receiverPhone: String,
    val packageDescription: String,
    val packageWeightKg: Double?,
    val instructions: String,
    val priority: DeliveryPriority
)