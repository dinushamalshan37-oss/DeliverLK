package com.example.deliverlk.data.fake

import com.example.deliverlk.domain.model.*

object FakeData {
    val initialVehicles = listOf(
        Vehicle(
            id = "veh_01",
            plateNumber = "WP-CAB-1234",
            driverId = "drv_01",
            driverName = "Saman Kumara",
            latitude = 6.9271,
            longitude = 79.8612,
            speedKmH = 45,
            deliveriesRemaining = 3,
            status = VehicleStatus.MOVING,
            lastUpdated = "Just now",
            heading = 90.0,
        ),
        Vehicle(
            id = "veh_02",
            plateNumber = "WP-PY-5678",
            driverId = "drv_02",
            driverName = "Sunil Perera",
            latitude = 6.9319,
            longitude = 79.8428,
            speedKmH = 0,
            deliveriesRemaining = 2,
            status = VehicleStatus.IDLE,
            lastUpdated = "5 mins ago",
            heading = 0.0,
        ),
    )

    val initialDeliveries = listOf(
        Delivery(
            id = "del_101",
            trackingCode = "DLK-COL-101",
            customerName = "Nimali Fernando",
            customerPhone = "+94 76 555 1234",
            address = "45 Galle Road, Colombo 03",
            latitude = 6.9147,
            longitude = 79.8517,
            itemsDescription = "Electronics & Accessories",
            specialInstructions = "Leave at reception if not available",
            priority = DeliveryPriority.HIGH,
            assignedDriverId = "drv_01",
            assignedDriverName = "Saman Kumara",
            status = DeliveryStatus.OUT_FOR_DELIVERY,
            etaMinutes = 15,
            distanceKm = 3.2,
            payoutLkr = 650.0,
            stopNumber = 1,
        ),
        Delivery(
            id = "del_102",
            trackingCode = "DLK-COL-102",
            customerName = "Ruwan Silva",
            customerPhone = "+94 77 888 9999",
            address = "12Duplication Road, Colombo 04",
            latitude = 6.8985,
            longitude = 79.8560,
            itemsDescription = "Documents / Envelope",
            specialInstructions = "Call upon arrival",
            priority = DeliveryPriority.MEDIUM,
            assignedDriverId = "drv_01",
            assignedDriverName = "Saman Kumara",
            status = DeliveryStatus.ASSIGNED,
            etaMinutes = 30,
            distanceKm = 5.8,
            payoutLkr = 450.0,
            stopNumber = 2,
        ),
    )

    val initialSafetyEvents = listOf(
        SafetyEvent(
            id = "evt_01",
            driverId = "drv_01",
            type = SafetyEventType.SPEEDING,
            speedKmH = 85,
            locationText = "Borella Junction",
            timestamp = "10 mins ago",
        ),
    )
}
