package com.example.deliverlk.data.firebase

import com.example.deliverlk.domain.model.Company
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryPriority
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.model.Earning
import com.example.deliverlk.domain.model.NewOrderRequest
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class FirestoreDeliveryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : DeliveryRepository {

    override val deliveries: Flow<List<Delivery>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.DELIVERIES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toDelivery() }
                    trySend(list.sortedWith(compareByDescending<Delivery> { it.status == DeliveryStatus.PENDING && it.createdBy == "CUSTOMER" }.thenByDescending { it.id }))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getDeliveryById(id: String): Flow<Delivery?> {
        return deliveries.map { list -> list.find { it.id == id } }
    }

    override fun getDeliveryByTrackingCode(code: String): Flow<Delivery?> {
        return deliveries.map { list ->
            val clean = code.trim().lowercase()
            list.find { it.trackingCode.lowercase() == clean || it.id.lowercase() == clean }
        }
    }

    override fun getDeliveriesForDriver(driverId: String): Flow<List<Delivery>> {
        return deliveries.map { list ->
            list.filter { it.assignedDriverId == driverId }
        }
    }

    override suspend fun createDelivery(delivery: Delivery): Result<Unit> {
        return try {
            val docRef = if (delivery.id.isBlank() || delivery.id.startsWith("del_")) {
                firestore.collection(FirestoreConstants.DELIVERIES).document()
            } else {
                firestore.collection(FirestoreConstants.DELIVERIES).document(delivery.id)
            }

            val data = mutableMapOf<String, Any?>(
                FirestoreConstants.DELIVERY_COMPANY_ID to delivery.companyId,
                FirestoreConstants.DELIVERY_ORDER_CODE to delivery.trackingCode,
                FirestoreConstants.DELIVERY_CUSTOMER_UID to delivery.customerUid,
                FirestoreConstants.DELIVERY_CUSTOMER_EMAIL to delivery.customerEmail.trim().lowercase(),
                FirestoreConstants.DELIVERY_CUSTOMER_NAME to delivery.receiverName.ifBlank { delivery.customerName },
                FirestoreConstants.DELIVERY_CUSTOMER_PHONE to delivery.receiverPhone.ifBlank { delivery.customerPhone },
                FirestoreConstants.DELIVERY_ADDRESS to delivery.address,
                FirestoreConstants.DELIVERY_LOCATION to GeoPoint(delivery.latitude, delivery.longitude),
                FirestoreConstants.DELIVERY_ITEMS to delivery.itemsDescription,
                FirestoreConstants.DELIVERY_PACKAGE_WEIGHT_KG to delivery.packageWeightKg,
                FirestoreConstants.DELIVERY_INSTRUCTIONS to delivery.specialInstructions,
                FirestoreConstants.DELIVERY_PRIORITY to delivery.priority.name,
                FirestoreConstants.DELIVERY_STATUS to delivery.status.name,
                FirestoreConstants.DELIVERY_CREATED_BY to delivery.createdBy,
                FirestoreConstants.DELIVERY_PICKUP_ADDRESS to delivery.pickupAddress,
                FirestoreConstants.DELIVERY_PICKUP_LOCATION to delivery.pickupLocation,
                FirestoreConstants.DELIVERY_RECEIVER_NAME to delivery.receiverName,
                FirestoreConstants.DELIVERY_RECEIVER_PHONE to delivery.receiverPhone,
                FirestoreConstants.DELIVERY_DRIVER_UID to delivery.assignedDriverId,
                FirestoreConstants.DELIVERY_DRIVER_NAME to delivery.assignedDriverName,
                FirestoreConstants.DELIVERY_STOP_ORDER to delivery.stopNumber,
                FirestoreConstants.DELIVERY_ETA_MINUTES to delivery.etaMinutes,
                FirestoreConstants.DELIVERY_DISTANCE_KM to delivery.distanceKm,
                FirestoreConstants.DELIVERY_AMOUNT_LKR to delivery.payoutLkr,
                FirestoreConstants.DELIVERY_CREATED_AT to FieldValue.serverTimestamp(),
                FirestoreConstants.DELIVERY_UPDATED_AT to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateDeliveryStatus(
        id: String,
        status: DeliveryStatus,
        proofPhotoUri: String?,
        recipientName: String?,
        note: String?
    ): Result<Unit> {
        return try {
            val deliveryRef = firestore.collection(FirestoreConstants.DELIVERIES).document(id)
            val batch = firestore.batch()

            val updates = mutableMapOf<String, Any?>(
                FirestoreConstants.DELIVERY_STATUS to status.name,
                FirestoreConstants.DELIVERY_UPDATED_AT to FieldValue.serverTimestamp()
            )

            if (status == DeliveryStatus.DELIVERED) {
                updates[FirestoreConstants.DELIVERY_DELIVERED_AT] = FieldValue.serverTimestamp()
            }

            if (proofPhotoUri != null || recipientName != null || note != null) {
                val proofMap = mapOf(
                    FirestoreConstants.PROOF_PHOTO_URL to proofPhotoUri,
                    FirestoreConstants.PROOF_RECIPIENT_NAME to recipientName,
                    FirestoreConstants.PROOF_NOTE to note,
                    FirestoreConstants.PROOF_CAPTURED_AT to FieldValue.serverTimestamp()
                )
                updates[FirestoreConstants.PROOF] = proofMap
            }

            batch.update(deliveryRef, updates)

            val eventRef = deliveryRef.collection(FirestoreConstants.EVENTS).document()
            val eventData = mapOf(
                FirestoreConstants.EVENT_STATUS to status.name,
                FirestoreConstants.EVENT_AT to FieldValue.serverTimestamp(),
                FirestoreConstants.EVENT_BY_UID to auth.currentUser?.uid
            )
            batch.set(eventRef, eventData)

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun assignDriver(
        deliveryId: String,
        driverId: String,
        driverName: String
    ): Result<Unit> {
        return try {
            val deliveryRef = firestore.collection(FirestoreConstants.DELIVERIES).document(deliveryId)
            val batch = firestore.batch()

            batch.update(
                deliveryRef,
                mapOf(
                    FirestoreConstants.DELIVERY_DRIVER_UID to driverId,
                    FirestoreConstants.DELIVERY_DRIVER_NAME to driverName,
                    FirestoreConstants.DELIVERY_STATUS to DeliveryStatus.ASSIGNED.name,
                    FirestoreConstants.DELIVERY_UPDATED_AT to FieldValue.serverTimestamp()
                )
            )

            val eventRef = deliveryRef.collection(FirestoreConstants.EVENTS).document()
            batch.set(
                eventRef,
                mapOf(
                    FirestoreConstants.EVENT_STATUS to DeliveryStatus.ASSIGNED.name,
                    FirestoreConstants.EVENT_AT to FieldValue.serverTimestamp(),
                    FirestoreConstants.EVENT_BY_UID to auth.currentUser?.uid
                )
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun suggestOptimalOrder(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(FirestoreConstants.DELIVERIES).get().await()
            val batch = firestore.batch()
            snapshot.documents.forEachIndexed { index, doc ->
                batch.update(doc.reference, FirestoreConstants.DELIVERY_STOP_ORDER, index + 1)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override fun getDriverEarnings(driverId: String): Flow<List<Earning>> {
        return flowOf(
            listOf(
                Earning("Mon", 2450.0, 5),
                Earning("Tue", 3200.0, 7),
                Earning("Wed", 2800.0, 6),
                Earning("Thu", 4100.0, 9),
                Earning("Fri", 3600.0, 8),
                Earning("Sat", 4800.0, 10),
                Earning("Sun", 1900.0, 4)
            )
        )
    }

    override fun observeMyOrders(email: String): Flow<List<Delivery>> = callbackFlow {
        val cleanEmail = email.trim().lowercase()
        val listener = firestore.collection(FirestoreConstants.DELIVERIES)
            .whereEqualTo(FirestoreConstants.DELIVERY_CUSTOMER_EMAIL, cleanEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toDelivery() }
                    trySend(list.sortedByDescending { it.id })
                }
            }
        awaitClose { listener.remove() }
    }

    private fun generateUniqueTrackingCode(): String {
        val allowedChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val suffix = (1..8).map { allowedChars.random() }.joinToString("")
        return "DLK-$suffix"
    }

    override suspend fun placeOrder(order: NewOrderRequest): Result<Delivery> {
        return try {
            val trackingCode = generateUniqueTrackingCode()
            val docRef = firestore.collection(FirestoreConstants.DELIVERIES).document()
            val batch = firestore.batch()

            val deliveryMap = mapOf(
                FirestoreConstants.DELIVERY_COMPANY_ID to order.companyId,
                FirestoreConstants.DELIVERY_ORDER_CODE to trackingCode,
                FirestoreConstants.DELIVERY_CUSTOMER_UID to order.customerUid,
                FirestoreConstants.DELIVERY_CUSTOMER_EMAIL to order.customerEmail.trim().lowercase(),
                FirestoreConstants.DELIVERY_CUSTOMER_NAME to order.receiverName,
                FirestoreConstants.DELIVERY_CUSTOMER_PHONE to order.receiverPhone,
                FirestoreConstants.DELIVERY_ADDRESS to order.dropoffAddress,
                FirestoreConstants.DELIVERY_LOCATION to order.dropoffLocation,
                FirestoreConstants.DELIVERY_ITEMS to order.packageDescription,
                FirestoreConstants.DELIVERY_PACKAGE_WEIGHT_KG to order.packageWeightKg,
                FirestoreConstants.DELIVERY_INSTRUCTIONS to order.instructions,
                FirestoreConstants.DELIVERY_PRIORITY to order.priority.name,
                FirestoreConstants.DELIVERY_STATUS to DeliveryStatus.PENDING.name,
                FirestoreConstants.DELIVERY_CREATED_BY to "CUSTOMER",
                FirestoreConstants.DELIVERY_PICKUP_ADDRESS to order.pickupAddress,
                FirestoreConstants.DELIVERY_PICKUP_LOCATION to order.pickupLocation,
                FirestoreConstants.DELIVERY_RECEIVER_NAME to order.receiverName,
                FirestoreConstants.DELIVERY_RECEIVER_PHONE to order.receiverPhone,
                FirestoreConstants.DELIVERY_CREATED_AT to FieldValue.serverTimestamp(),
                FirestoreConstants.DELIVERY_UPDATED_AT to FieldValue.serverTimestamp()
            )

            batch.set(docRef, deliveryMap)

            val eventRef = docRef.collection(FirestoreConstants.EVENTS).document()
            val eventData = mapOf(
                FirestoreConstants.EVENT_STATUS to DeliveryStatus.PENDING.name,
                FirestoreConstants.EVENT_AT to FieldValue.serverTimestamp(),
                FirestoreConstants.EVENT_BY_UID to order.customerUid
            )
            batch.set(eventRef, eventData)

            batch.commit().await()

            val createdDelivery = Delivery(
                id = docRef.id,
                trackingCode = trackingCode,
                customerName = order.receiverName,
                customerPhone = order.receiverPhone,
                address = order.dropoffAddress,
                latitude = order.dropoffLocation?.latitude ?: 6.9271,
                longitude = order.dropoffLocation?.longitude ?: 79.8612,
                itemsDescription = order.packageDescription,
                specialInstructions = order.instructions,
                priority = order.priority,
                status = DeliveryStatus.PENDING,
                customerEmail = order.customerEmail.trim().lowercase(),
                customerUid = order.customerUid,
                createdBy = "CUSTOMER",
                pickupAddress = order.pickupAddress,
                pickupLocation = order.pickupLocation,
                receiverName = order.receiverName,
                receiverPhone = order.receiverPhone,
                packageWeightKg = order.packageWeightKg,
                instructions = order.instructions,
                companyId = order.companyId
            )

            Result.success(createdDelivery)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun cancelMyOrder(deliveryId: String): Result<Unit> {
        return try {
            val docRef = firestore.collection(FirestoreConstants.DELIVERIES).document(deliveryId)
            val doc = docRef.get().await()
            val statusStr = doc.getString(FirestoreConstants.DELIVERY_STATUS)
            if (statusStr != DeliveryStatus.PENDING.name) {
                return Result.failure(Exception("Order can only be cancelled while pending."))
            }

            val batch = firestore.batch()
            batch.update(
                docRef,
                mapOf(
                    FirestoreConstants.DELIVERY_STATUS to DeliveryStatus.CANCELLED.name,
                    FirestoreConstants.DELIVERY_UPDATED_AT to FieldValue.serverTimestamp()
                )
            )

            val eventRef = docRef.collection(FirestoreConstants.EVENTS).document()
            batch.set(
                eventRef,
                mapOf(
                    FirestoreConstants.EVENT_STATUS to DeliveryStatus.CANCELLED.name,
                    FirestoreConstants.EVENT_AT to FieldValue.serverTimestamp(),
                    FirestoreConstants.EVENT_BY_UID to auth.currentUser?.uid
                )
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override fun observeOrder(deliveryId: String): Flow<Delivery?> {
        return getDeliveryById(deliveryId)
    }

    override suspend fun getCompanies(): Result<List<Company>> {
        return try {
            val snapshot = firestore.collection(FirestoreConstants.COMPANIES).get().await()
            val list = snapshot.documents.map { doc ->
                Company(
                    id = doc.id,
                    name = doc.getString(FirestoreConstants.COMPANY_NAME) ?: "DeliverLK Logistics",
                    ownerUid = doc.getString(FirestoreConstants.COMPANY_OWNER_UID)
                )
            }
            Result.success(if (list.isEmpty()) listOf(Company(FirestoreConstants.DEFAULT_COMPANY_ID, "DeliverLK Logistics")) else list)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.success(listOf(Company(FirestoreConstants.DEFAULT_COMPANY_ID, "DeliverLK Logistics")))
        }
    }

    companion object {
        fun DocumentSnapshot.toDelivery(): Delivery? {
            if (!exists()) return null
            val statusStr = getString(FirestoreConstants.DELIVERY_STATUS) ?: "PENDING"
            val status = try {
                DeliveryStatus.valueOf(statusStr.uppercase())
            } catch (e: Exception) {
                DeliveryStatus.PENDING
            }

            val priorityStr = getString(FirestoreConstants.DELIVERY_PRIORITY) ?: "MEDIUM"
            val priority = try {
                DeliveryPriority.valueOf(priorityStr.uppercase())
            } catch (e: Exception) {
                DeliveryPriority.MEDIUM
            }

            val geoPoint = getGeoPoint(FirestoreConstants.DELIVERY_LOCATION)
            val pickupGeoPoint = getGeoPoint(FirestoreConstants.DELIVERY_PICKUP_LOCATION)

            @Suppress("UNCHECKED_CAST")
            val proofMap = get(FirestoreConstants.PROOF) as? Map<String, Any?>

            return Delivery(
                id = id,
                trackingCode = getString(FirestoreConstants.DELIVERY_ORDER_CODE) ?: id,
                customerName = getString(FirestoreConstants.DELIVERY_CUSTOMER_NAME) ?: "",
                customerPhone = getString(FirestoreConstants.DELIVERY_CUSTOMER_PHONE) ?: "",
                address = getString(FirestoreConstants.DELIVERY_ADDRESS) ?: "",
                latitude = geoPoint?.latitude ?: 6.9271,
                longitude = geoPoint?.longitude ?: 79.8612,
                itemsDescription = getString(FirestoreConstants.DELIVERY_ITEMS) ?: "",
                specialInstructions = getString(FirestoreConstants.DELIVERY_INSTRUCTIONS) ?: "",
                priority = priority,
                assignedDriverId = getString(FirestoreConstants.DELIVERY_DRIVER_UID),
                assignedDriverName = getString(FirestoreConstants.DELIVERY_DRIVER_NAME),
                status = status,
                etaMinutes = getLong(FirestoreConstants.DELIVERY_ETA_MINUTES)?.toInt() ?: 30,
                distanceKm = getDouble(FirestoreConstants.DELIVERY_DISTANCE_KM) ?: 4.5,
                deliveryTime = getString("deliveryTime"),
                proofPhotoUri = proofMap?.get(FirestoreConstants.PROOF_PHOTO_URL) as? String,
                recipientName = proofMap?.get(FirestoreConstants.PROOF_RECIPIENT_NAME) as? String,
                note = proofMap?.get(FirestoreConstants.PROOF_NOTE) as? String,
                payoutLkr = getDouble(FirestoreConstants.DELIVERY_AMOUNT_LKR) ?: 450.0,
                stopNumber = getLong(FirestoreConstants.DELIVERY_STOP_ORDER)?.toInt() ?: 1,
                customerEmail = getString(FirestoreConstants.DELIVERY_CUSTOMER_EMAIL) ?: "",
                customerUid = getString(FirestoreConstants.DELIVERY_CUSTOMER_UID),
                createdBy = getString(FirestoreConstants.DELIVERY_CREATED_BY) ?: "MANAGER",
                pickupAddress = getString(FirestoreConstants.DELIVERY_PICKUP_ADDRESS) ?: "",
                pickupLocation = pickupGeoPoint,
                receiverName = getString(FirestoreConstants.DELIVERY_RECEIVER_NAME) ?: getString(FirestoreConstants.DELIVERY_CUSTOMER_NAME) ?: "",
                receiverPhone = getString(FirestoreConstants.DELIVERY_RECEIVER_PHONE) ?: getString(FirestoreConstants.DELIVERY_CUSTOMER_PHONE) ?: "",
                packageWeightKg = getDouble(FirestoreConstants.DELIVERY_PACKAGE_WEIGHT_KG),
                instructions = getString(FirestoreConstants.DELIVERY_INSTRUCTIONS) ?: "",
                companyId = getString(FirestoreConstants.DELIVERY_COMPANY_ID) ?: FirestoreConstants.DEFAULT_COMPANY_ID
            )
        }
    }
}