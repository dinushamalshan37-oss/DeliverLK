package com.example.deliverlk.data.fake

import com.example.deliverlk.domain.model.SafetyEvent
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFleetRepository : FleetRepository {
    private val _vehiclesFlow = MutableStateFlow(FakeData.initialVehicles)
    private val _safetyEventsFlow = MutableStateFlow(FakeData.initialSafetyEvents)

    override val vehicles: Flow<List<Vehicle>> = _vehiclesFlow
    override val safetyEvents: Flow<List<SafetyEvent>> = _safetyEventsFlow

    override fun getVehicleById(id: String): Flow<Vehicle?> {
        return _vehiclesFlow.map { list -> list.find { it.id == id } }
    }

    override fun getSafetyEventsForDriver(driverId: String): Flow<List<SafetyEvent>> {
        return _safetyEventsFlow.map { list -> list.filter { it.driverId == driverId } }
    }

    override fun getDriverSafetyScore(driverId: String): Flow<Int> {
        return MutableStateFlow(95)
    }
}
