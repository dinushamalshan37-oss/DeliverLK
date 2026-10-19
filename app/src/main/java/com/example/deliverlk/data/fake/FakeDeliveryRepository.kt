package com.example.deliverlk.data.fake

import com.example.deliverlk.domain.model.*
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDeliveryRepository : DeliveryRepository {
    private val _deliveriesFlow = MutableStateFlow(FakeData.initialDeliveries)

    override val deliveries: Flow<List<Delivery>> = _deliveriesFlow

    override fun getDeliveryById(id: String): Flow<Delivery?> {
        return _deliveriesFlow.map { list -> list.find { it.id == id } }
    }

    override fun getDeliveryByTrackingCode(code: String): Flow<Delivery?> {
        return _deliveriesFlow.map { list -> list.find { it.trackingCode == code } }
    }

    override fun getDeliveriesForDriver(driverId: String): Flow<List<Delivery>> {
        return _deliveriesFlow.map { list -> list.filter { it.assignedDriverId == driverId } }
    }

    override suspend fun createDelivery(delivery: Delivery): Result<Unit> {
        _deliveriesFlow.value = _deliveriesFlow.value + delivery
        return Result.success(Unit)
    }

    override suspend fun updateDeliveryStatus(
        id: String,
        status: DeliveryStatus,
        proofPhotoUri: String?,
        recipientName: String?,
        note: String?,
    ): Result<Unit> {
        _deliveriesFlow.value = _deliveriesFlow.value.map {
            if (it.id == id) {
                it.copy(
                    status = status,
                    proofPhotoUri = proofPhotoUri ?: it.proofPhotoUri,
                    recipientName = recipientName ?: it.recipientName,
                    note = note ?: it.note,
                )
            } else {
                it
            }
        }
        return Result.success(Unit)
    }

    override suspend fun assignDriver(deliveryId: String, driverId: String, driverName: String): Result<Unit> {
        _deliveriesFlow.value = _deliveriesFlow.value.map {
            if (it.id == deliveryId) {
                it.copy(assignedDriverId = driverId, assignedDriverName = driverName, status = DeliveryStatus.ASSIGNED)
            } else {
                it
            }
        }
        return Result.success(Unit)
    }

    override suspend fun suggestOptimalOrder(): Result<Unit> {
        return Result.success(Unit)
    }

    override fun getDriverEarnings(driverId: String): Flow<List<Earning>> {
        return MutableStateFlow(listOf(
            Earning(
                dayLabel = "Today",
                amountLkr = 650.0,
                deliveriesCount = 1,
            )
        ))
    }

    override fun observeMyOrders(email: String): Flow<List<Delivery>> {
        return _deliveriesFlow
    }

    override suspend fun placeOrder(order: NewOrderRequest): Result<Delivery> {
        val delivery = Delivery(
            id = "del_${System.currentTimeMillis()}",
            trackingCode = "DLK-COL-${System.currentTimeMillis() % 1000}",
            customerName = order.receiverName,
            customerPhone = order.receiverPhone,
            address = order.dropoffAddress,
            latitude = order.dropoffLocation?.latitude ?: 6.9271,
            longitude = order.dropoffLocation?.longitude ?: 79.8612,
            itemsDescription = order.packageDescription,
            specialInstructions = order.instructions,
            priority = order.priority,
            payoutLkr = 450.0,
            customerEmail = order.customerEmail,
            customerUid = order.customerUid,
            pickupAddress = order.pickupAddress,
            pickupLocation = order.pickupLocation,
            receiverName = order.receiverName,
            receiverPhone = order.receiverPhone,
            packageWeightKg = order.packageWeightKg,
            instructions = order.instructions,
            companyId = order.companyId,
        )
        _deliveriesFlow.value = _deliveriesFlow.value + delivery
        return Result.success(delivery)
    }

    override suspend fun cancelMyOrder(deliveryId: String): Result<Unit> {
        updateDeliveryStatus(deliveryId, DeliveryStatus.CANCELLED)
        return Result.success(Unit)
    }

    override fun observeOrder(deliveryId: String): Flow<Delivery?> {
        return getDeliveryById(deliveryId)
    }

    override suspend fun getCompanies(): Result<List<Company>> {
        return Result.success(listOf(Company(id = "dlk_company_01", name = "DeliverLK Logistics Sri Lanka")))
    }
}
