package com.example.deliverlk.data.firebase

import com.example.deliverlk.BuildConfig
import com.example.deliverlk.data.fake.FakeData
import com.example.deliverlk.domain.model.Role
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object DemoSeeder {

    suspend fun seedDemoDataIfNeeded(
        auth: FirebaseAuth = FirebaseAuth.getInstance(),
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<Unit> {
        if (!BuildConfig.DEBUG) {
            return Result.success(Unit)
        }

        return withContext(Dispatchers.IO) {
            try {
                // 1. Seed Company
                val companyRef = firestore.collection(FirestoreConstants.COMPANIES)
                    .document(FirestoreConstants.DEFAULT_COMPANY_ID)
                
                companyRef.set(
                    mapOf(
                        FirestoreConstants.COMPANY_NAME to "DeliverLK Logistics Sri Lanka",
                        FirestoreConstants.COMPANY_OWNER_UID to "mgr_01",
                        FirestoreConstants.COMPANY_CREATED_AT to FieldValue.serverTimestamp()
                    )
                ).await()

                // 2. Seed Demo Auth Users
                seedUser(auth, firestore, "manager@deliverlk.lk", "Kasun Perera", "+94 77 123 4567", Role.MANAGER, "mgr_01")
                seedUser(auth, firestore, "driver@deliverlk.lk", "Saman Kumara", "+94 71 987 6543", Role.DRIVER, "drv_01")
                seedUser(auth, firestore, "customer@deliverlk.lk", "Nimali Fernando", "+94 76 555 1234", Role.CUSTOMER, "cust_01")

                // 3. Seed Vehicles
                val batch = firestore.batch()
                FakeData.initialVehicles.forEach { vehicle ->
                    val vRef = firestore.collection(FirestoreConstants.VEHICLES).document(vehicle.id)
                    val vMap = mapOf(
                        FirestoreConstants.VEHICLE_COMPANY_ID to FirestoreConstants.DEFAULT_COMPANY_ID,
                        FirestoreConstants.VEHICLE_PLATE to vehicle.plateNumber,
                        FirestoreConstants.VEHICLE_DRIVER_UID to vehicle.driverId,
                        FirestoreConstants.VEHICLE_DRIVER_NAME to vehicle.driverName,
                        FirestoreConstants.VEHICLE_STATUS to vehicle.status.name,
                        FirestoreConstants.VEHICLE_LOCATION to GeoPoint(vehicle.latitude, vehicle.longitude),
                        FirestoreConstants.VEHICLE_SPEED_KMH to vehicle.speedKmH,
                        FirestoreConstants.VEHICLE_HEADING to 0.0,
                        FirestoreConstants.VEHICLE_LAST_UPDATED to FieldValue.serverTimestamp()
                    )
                    batch.set(vRef, vMap)
                }

                // 4. Seed Deliveries
                FakeData.initialDeliveries.forEach { delivery ->
                    val dRef = firestore.collection(FirestoreConstants.DELIVERIES).document(delivery.id)
                    val dMap = mapOf(
                        FirestoreConstants.DELIVERY_COMPANY_ID to FirestoreConstants.DEFAULT_COMPANY_ID,
                        FirestoreConstants.DELIVERY_ORDER_CODE to delivery.trackingCode,
                        FirestoreConstants.DELIVERY_CUSTOMER_NAME to delivery.customerName,
                        FirestoreConstants.DELIVERY_CUSTOMER_PHONE to delivery.customerPhone,
                        FirestoreConstants.DELIVERY_ADDRESS to delivery.address,
                        FirestoreConstants.DELIVERY_LOCATION to GeoPoint(delivery.latitude, delivery.longitude),
                        FirestoreConstants.DELIVERY_ITEMS to delivery.itemsDescription,
                        FirestoreConstants.DELIVERY_INSTRUCTIONS to delivery.specialInstructions,
                        FirestoreConstants.DELIVERY_PRIORITY to delivery.priority.name,
                        FirestoreConstants.DELIVERY_STATUS to delivery.status.name,
                        FirestoreConstants.DELIVERY_DRIVER_UID to delivery.assignedDriverId,
                        FirestoreConstants.DELIVERY_DRIVER_NAME to delivery.assignedDriverName,
                        FirestoreConstants.DELIVERY_STOP_ORDER to delivery.stopNumber,
                        FirestoreConstants.DELIVERY_ETA_MINUTES to delivery.etaMinutes,
                        FirestoreConstants.DELIVERY_DISTANCE_KM to delivery.distanceKm,
                        FirestoreConstants.DELIVERY_AMOUNT_LKR to delivery.payoutLkr,
                        FirestoreConstants.DELIVERY_CREATED_AT to FieldValue.serverTimestamp(),
                        FirestoreConstants.DELIVERY_UPDATED_AT to FieldValue.serverTimestamp()
                    )
                    batch.set(dRef, dMap)
                }

                // 5. Seed Safety Events
                FakeData.initialSafetyEvents.forEach { event ->
                    val sRef = firestore.collection(FirestoreConstants.SAFETY_EVENTS).document(event.id)
                    val sMap = mapOf(
                        FirestoreConstants.SAFETY_COMPANY_ID to FirestoreConstants.DEFAULT_COMPANY_ID,
                        FirestoreConstants.SAFETY_DRIVER_UID to event.driverId,
                        FirestoreConstants.SAFETY_VEHICLE_ID to "veh_01",
                        FirestoreConstants.SAFETY_TYPE to event.type.name,
                        FirestoreConstants.SAFETY_SPEED_KMH to event.speedKmH,
                        FirestoreConstants.SAFETY_LOCATION to GeoPoint(6.9271, 79.8612),
                        FirestoreConstants.SAFETY_LOCATION_TEXT to event.locationText,
                        FirestoreConstants.SAFETY_AT to FieldValue.serverTimestamp()
                    )
                    batch.set(sRef, sMap)
                }

                batch.commit().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun seedUser(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        email: String,
        name: String,
        phone: String,
        role: Role,
        customUid: String
    ) {
        var uid = customUid
        try {
            val res = auth.createUserWithEmailAndPassword(email, "123456").await()
            res.user?.let { uid = it.uid }
        } catch (e: Exception) {
            // User might already exist in Auth, try sign in
            try {
                val res = auth.signInWithEmailAndPassword(email, "123456").await()
                res.user?.let { uid = it.uid }
            } catch (ex: Exception) {
                // Ignore if sign in fails
            }
        }

        val userDoc = mapOf(
            FirestoreConstants.USER_NAME to name,
            FirestoreConstants.USER_EMAIL to email,
            FirestoreConstants.USER_PHONE to phone,
            FirestoreConstants.USER_ROLE to role.name,
            FirestoreConstants.USER_COMPANY_ID to FirestoreConstants.DEFAULT_COMPANY_ID,
            FirestoreConstants.USER_VEHICLE_ID to if (role == Role.DRIVER) "veh_01" else null,
            FirestoreConstants.USER_FCM_TOKEN to null,
            FirestoreConstants.USER_CREATED_AT to FieldValue.serverTimestamp()
        )

        firestore.collection(FirestoreConstants.USERS).document(uid).set(userDoc).await()
    }
}
