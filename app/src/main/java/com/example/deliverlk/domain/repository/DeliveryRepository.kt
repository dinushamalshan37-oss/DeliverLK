package com.example.deliverlk.domain.repository

import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.model.Earning
import kotlinx.coroutines.flow.Flow

interface DeliveryRepository {
    val deliveries: Flow<List<Delivery>>
    fun getDeliveryById(id: String): Flow<Delivery?>
    fun getDeliveryByTrackingCode(code: String): Flow<Delivery?>
    fun getDeliveriesForDriver(driverId: String): Flow<List<Delivery>>
    suspend fun createDelivery(delivery: Delivery): Result<Unit>
    suspend fun updateDeliveryStatus(
        id: String,
        status: DeliveryStatus,
        proofPhotoUri: String? = null,
        recipientName: String? = null,
        note: String? = null
    ): Result<Unit>
    suspend fun assignDriver(deliveryId: String, driverId: String, driverName: String): Result<Unit>
    suspend fun suggestOptimalOrder(): Result<Unit>
    fun getDriverEarnings(driverId: String): Flow<List<Earning>>
}
