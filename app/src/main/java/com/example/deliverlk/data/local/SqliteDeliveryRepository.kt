package com.example.deliverlk.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.deliverlk.domain.model.*
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class SqliteDeliveryRepository(context: Context) : DeliveryRepository {
    private val dbHelper = AppDatabaseHelper.getInstance(context)

    override val deliveries: Flow<List<Delivery>> = dbHelper.deliveriesFlow

    override fun getDeliveryById(id: String): Flow<Delivery?> {
        return deliveries.map { list -> list.find { it.id == id } }
    }

    override fun getDeliveryByTrackingCode(code: String): Flow<Delivery?> {
        return deliveries.map { list -> list.find { it.trackingCode == code } }
    }

    override fun getDeliveriesForDriver(driverId: String): Flow<List<Delivery>> {
        return deliveries.map { list -> list.filter { it.assignedDriverId == driverId } }
    }

    override suspend fun createDelivery(delivery: Delivery): Result<Unit> {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("id", delivery.id)
            put("trackingCode", delivery.trackingCode)
            put("customerName", delivery.customerName)
            put("customerPhone", delivery.customerPhone)
            put("address", delivery.address)
            put("latitude", delivery.latitude)
            put("longitude", delivery.longitude)
            put("itemsDescription", delivery.itemsDescription)
            put("specialInstructions", delivery.specialInstructions)
            put("priority", delivery.priority.name)
            put("assignedDriverId", delivery.assignedDriverId)
            put("assignedDriverName", delivery.assignedDriverName)
            put("status", delivery.status.name)
            put("etaMinutes", delivery.etaMinutes)
            put("distanceKm", delivery.distanceKm)
            put("payoutLkr", delivery.payoutLkr)
            put("stopNumber", delivery.stopNumber)
            put("customerEmail", delivery.customerEmail)
            put("customerUid", delivery.customerUid)
            put("createdBy", delivery.createdBy)
            put("pickupAddress", delivery.pickupAddress)
            put("receiverName", delivery.receiverName)
            put("receiverPhone", delivery.receiverPhone)
            put("packageWeightKg", delivery.packageWeightKg)
            put("instructions", delivery.instructions)
            put("companyId", delivery.companyId)
            put("proofPhotoUri", delivery.proofPhotoUri)
            put("recipientName", delivery.recipientName)
            put("note", delivery.note)
        }
        db.insertWithOnConflict("deliveries", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        dbHelper.refreshFlows()
        return Result.success(Unit)
    }

    override suspend fun updateDeliveryStatus(
        id: String,
        status: DeliveryStatus,
        proofPhotoUri: String?,
        recipientName: String?,
        note: String?,
    ): Result<Unit> {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("status", status.name)
            proofPhotoUri?.let { put("proofPhotoUri", it) }
            recipientName?.let { put("recipientName", it) }
            note?.let { put("note", it) }
        }
        db.update("deliveries", cv, "id = ?", arrayOf(id))
        dbHelper.refreshFlows()
        return Result.success(Unit)
    }

    override suspend fun assignDriver(deliveryId: String, driverId: String, driverName: String): Result<Unit> {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("assignedDriverId", driverId)
            put("assignedDriverName", driverName)
            put("status", DeliveryStatus.ASSIGNED.name)
        }
        db.update("deliveries", cv, "id = ?", arrayOf(deliveryId))
        dbHelper.refreshFlows()
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
        return deliveries
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
        createDelivery(delivery)
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
