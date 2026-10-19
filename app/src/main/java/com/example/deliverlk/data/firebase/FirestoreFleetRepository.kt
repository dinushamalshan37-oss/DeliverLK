package com.example.deliverlk.data.firebase

import com.example.deliverlk.domain.model.SafetyEvent
import com.example.deliverlk.domain.model.SafetyEventType
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.model.VehicleStatus
import com.example.deliverlk.domain.repository.FleetRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

class FirestoreFleetRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FleetRepository {

    override val vehicles: Flow<List<Vehicle>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.VEHICLES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toVehicle() }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    override val safetyEvents: Flow<List<SafetyEvent>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.SAFETY_EVENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toSafetyEvent() }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getVehicleById(id: String): Flow<Vehicle?> {
        return vehicles.map { list -> list.find { it.id == id } }
    }

    override fun getSafetyEventsForDriver(driverId: String): Flow<List<SafetyEvent>> {
        return safetyEvents.map { list -> list.filter { it.driverId == driverId } }
    }

    override fun getDriverSafetyScore(driverId: String): Flow<Int> {
        return safetyEvents.map { list ->
            val count = list.count { it.driverId == driverId }
            (100 - count * 12).coerceIn(40, 100)
        }
    }

    companion object {
        fun DocumentSnapshot.toVehicle(): Vehicle? {
            if (!exists()) return null
            val statusStr = getString(FirestoreConstants.VEHICLE_STATUS) ?: "OFFLINE"
            var status = try {
                VehicleStatus.valueOf(statusStr.uppercase())
            } catch (e: Exception) {
                VehicleStatus.OFFLINE
            }

            val timestamp = getTimestamp(FirestoreConstants.VEHICLE_LAST_UPDATED)
            val now = System.currentTimeMillis()
            val lastUpdatedText: String

            if (timestamp != null) {
                val lastUpdatedMillis = timestamp.toDate().time
                val diffMinutes = ((now - lastUpdatedMillis) / (1000 * 60)).toInt()
                if (diffMinutes >= 2) {
                    status = VehicleStatus.OFFLINE
                    lastUpdatedText = "Last seen $diffMinutes min ago"
                } else if (diffMinutes <= 0) {
                    lastUpdatedText = "Just now"
                } else {
                    lastUpdatedText = "Last seen $diffMinutes min ago"
                }
            } else {
                val stringVal = getString(FirestoreConstants.VEHICLE_LAST_UPDATED) ?: "Just now"
                if (stringVal.contains("min ago")) {
                    val minutesMatch = Regex("(\\d+)").find(stringVal)?.value?.toIntOrNull()
                    if (minutesMatch != null && minutesMatch >= 2) {
                        status = VehicleStatus.OFFLINE
                        lastUpdatedText = "Last seen $minutesMatch min ago"
                    } else {
                        lastUpdatedText = stringVal
                    }
                } else {
                    lastUpdatedText = stringVal
                }
            }

            val geoPoint = getGeoPoint(FirestoreConstants.VEHICLE_LOCATION) ?: GeoPoint(6.9271, 79.8612)

            return Vehicle(
                id = id,
                plateNumber = getString(FirestoreConstants.VEHICLE_PLATE) ?: id,
                driverId = getString(FirestoreConstants.VEHICLE_DRIVER_UID) ?: "",
                driverName = getString(FirestoreConstants.VEHICLE_DRIVER_NAME) ?: "Unassigned Driver",
                latitude = geoPoint.latitude,
                longitude = geoPoint.longitude,
                speedKmH = if (status == VehicleStatus.OFFLINE) 0 else (getLong(FirestoreConstants.VEHICLE_SPEED_KMH)?.toInt() ?: 0),
                deliveriesRemaining = 3,
                status = status,
                lastUpdated = lastUpdatedText,
                heading = getDouble(FirestoreConstants.VEHICLE_HEADING) ?: 0.0
            )
        }

        fun DocumentSnapshot.toSafetyEvent(): SafetyEvent? {
            if (!exists()) return null
            val typeStr = getString(FirestoreConstants.SAFETY_TYPE) ?: "SPEEDING"
            val type = try {
                SafetyEventType.valueOf(typeStr.uppercase())
            } catch (e: Exception) {
                SafetyEventType.SPEEDING
            }

            return SafetyEvent(
                id = id,
                driverId = getString(FirestoreConstants.SAFETY_DRIVER_UID) ?: "",
                type = type,
                speedKmH = getLong(FirestoreConstants.SAFETY_SPEED_KMH)?.toInt() ?: 0,
                locationText = getString(FirestoreConstants.SAFETY_LOCATION_TEXT) ?: "Location recorded",
                timestamp = "Today"
            )
        }
    }
}