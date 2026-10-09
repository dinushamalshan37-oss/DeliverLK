package com.example.deliverlk.data.fake

import com.example.deliverlk.domain.model.SafetyEvent
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.model.VehicleStatus
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.random.Random

class FakeFleetRepository(
    externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : FleetRepository {

    private val _vehicles = MutableStateFlow(FakeData.initialVehicles)
    override val vehicles: Flow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _safetyEvents = MutableStateFlow(FakeData.initialSafetyEvents)
    override val safetyEvents: Flow<List<SafetyEvent>> = _safetyEvents.asStateFlow()

    init {
        // Nudge vehicle positions every 3 seconds to simulate movement on map
        externalScope.launch {
            while (isActive) {
                delay(3000)
                _vehicles.update { list ->
                    list.map { v ->
                        if (v.status == VehicleStatus.MOVING) {
                            val dLat = (Random.nextDouble() - 0.5) * 0.0008
                            val dLng = (Random.nextDouble() - 0.5) * 0.0008
                            val newSpeed = (v.speedKmH + Random.nextInt(-3, 4)).coerceIn(20, 75)
                            val calcHeading = (Math.toDegrees(atan2(dLng, dLat)) + 360) % 360
                            v.copy(
                                latitude = v.latitude + dLat,
                                longitude = v.longitude + dLng,
                                speedKmH = newSpeed,
                                heading = calcHeading,
                                lastUpdated = "Just now"
                            )
                        } else {
                            v
                        }
                    }
                }
            }
        }
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
}
