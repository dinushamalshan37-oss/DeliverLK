package com.example.deliverlk.data.local

import android.content.Context
import com.example.deliverlk.domain.model.SafetyEvent
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class SqliteFleetRepository(context: Context) : FleetRepository {
    private val dbHelper = AppDatabaseHelper.getInstance(context)

    override val vehicles: Flow<List<Vehicle>> = dbHelper.vehiclesFlow
    override val safetyEvents: Flow<List<SafetyEvent>> = dbHelper.safetyEventsFlow

    override fun getVehicleById(id: String): Flow<Vehicle?> {
        return vehicles.map { list -> list.find { it.id == id } }
    }

    override fun getSafetyEventsForDriver(driverId: String): Flow<List<SafetyEvent>> {
        return safetyEvents.map { list -> list.filter { it.driverId == driverId } }
    }

    override fun getDriverSafetyScore(driverId: String): Flow<Int> {
        return MutableStateFlow(95)
    }
}
