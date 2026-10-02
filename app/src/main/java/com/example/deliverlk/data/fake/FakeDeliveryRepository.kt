package com.example.deliverlk.data.fake

import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.model.Earning
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class FakeDeliveryRepository : DeliveryRepository {

    private val _deliveries = MutableStateFlow(FakeData.initialDeliveries)
    override val deliveries: Flow<List<Delivery>> = _deliveries.asStateFlow()

    override fun getDeliveryById(id: String): Flow<Delivery?> {
        return deliveries.map { list -> list.find { it.id == id } }
    }

    override fun getDeliveryByTrackingCode(code: String): Flow<Delivery?> {
        return deliveries.map { list ->
            list.find { it.trackingCode.equals(code.trim(), ignoreCase = true) || it.id.equals(code.trim(), ignoreCase = true) }
        }
    }

    override fun getDeliveriesForDriver(driverId: String): Flow<List<Delivery>> {
        return deliveries.map { list ->
            list.filter { it.assignedDriverId == driverId }
        }
    }

    override suspend fun createDelivery(delivery: Delivery): Result<Unit> {
        delay(600)
        _deliveries.update { current ->
            listOf(delivery) + current
        }
        return Result.success(Unit)
    }

    override suspend fun updateDeliveryStatus(
        id: String,
        status: DeliveryStatus,
        proofPhotoUri: String?,
        recipientName: String?,
        note: String?
    ): Result<Unit> {
        delay(500)
        _deliveries.update { list ->
            list.map { item ->
                if (item.id == id) {
                    item.copy(
                        status = status,
                        proofPhotoUri = proofPhotoUri ?: item.proofPhotoUri,
                        recipientName = recipientName ?: item.recipientName,
                        note = note ?: item.note,
                        deliveryTime = if (status == DeliveryStatus.DELIVERED) "Just now" else item.deliveryTime
                    )
                } else {
                    item
                }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun assignDriver(
        deliveryId: String,
        driverId: String,
        driverName: String
    ): Result<Unit> {
        delay(400)
        _deliveries.update { list ->
            list.map { item ->
                if (item.id == deliveryId) {
                    item.copy(
                        assignedDriverId = driverId,
                        assignedDriverName = driverName,
                        status = DeliveryStatus.ASSIGNED
                    )
                } else {
                    item
                }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun suggestOptimalOrder(): Result<Unit> {
        delay(800) // Simulate route optimization algorithm
        _deliveries.update { list ->
            list.mapIndexed { index, delivery ->
                delivery.copy(stopNumber = index + 1)
            }
        }
        return Result.success(Unit)
    }

    override fun getDriverEarnings(driverId: String): Flow<List<Earning>> {
        return flowOf(FakeData.driverEarningsData)
    }
}
