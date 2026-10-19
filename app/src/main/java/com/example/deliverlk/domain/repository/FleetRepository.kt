package com.example.deliverlk.domain.repository

import com.example.deliverlk.domain.model.SafetyEvent
import com.example.deliverlk.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface FleetRepository {
    val vehicles: Flow<List<Vehicle>>
    val safetyEvents: Flow<List<SafetyEvent>>
    fun getVehicleById(id: String): Flow<Vehicle?>
    fun getSafetyEventsForDriver(driverId: String): Flow<List<SafetyEvent>>
    fun getDriverSafetyScore(driverId: String): Flow<Int>
}
