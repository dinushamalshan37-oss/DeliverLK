package com.example.deliverlk.data.fake

import com.example.deliverlk.domain.model.*

object FakeData {

    val demoManager = User(
        id = "mgr_01",
        name = "Kasun Perera",
        email = "manager@deliverlk.lk",
        phone = "+94 77 123 4567",
        role = Role.MANAGER,
        company = "DeliverLK Logistics Sri Lanka"
    )

    val demoDriver = User(
        id = "drv_01",
        name = "Saman Kumara",
        email = "driver@deliverlk.lk",
        phone = "+94 71 987 6543",
        role = Role.DRIVER,
        company = "DeliverLK Logistics Sri Lanka"
    )

    val demoCustomer = User(
        id = "cust_01",
        name = "Nimali Fernando",
        email = "customer@deliverlk.lk",
        phone = "+94 76 555 1234",
        role = Role.CUSTOMER,
        company = "DeliverLK Retail"
    )

    val initialVehicles = listOf(
        Vehicle(
            id = "veh_01",
            plateNumber = "WP CAB-1234",
            driverId = "drv_01",
            driverName = "Saman Kumara",
            latitude = 6.9271,
            longitude = 79.8612,
            speedKmH = 48,
            deliveriesRemaining = 5,
            status = VehicleStatus.MOVING,
            lastUpdated = "2 mins ago"
        ),
        Vehicle(
            id = "veh_02",
            plateNumber = "CP KB-5521",
            driverId = "drv_02",
            driverName = "Kamal Silva",
            latitude = 6.9344,
            longitude = 79.8428,
            speedKmH = 0,
            deliveriesRemaining = 2,
            status = VehicleStatus.IDLE,
            lastUpdated = "Just now"
        ),
        Vehicle(
            id = "veh_03",
            plateNumber = "WP DAW-9081",
            driverId = "drv_03",
            driverName = "Sunil Shantha",
            latitude = 6.8511,
            longitude = 79.8653,
            speedKmH = 62,
            deliveriesRemaining = 7,
            status = VehicleStatus.MOVING,
            lastUpdated = "1 min ago"
        ),
        Vehicle(
            id = "veh_04",
            plateNumber = "SG NC-4410",
            driverId = "drv_04",
            driverName = "Ruwan Wickramasinghe",
            latitude = 6.8720,
            longitude = 79.8882,
            speedKmH = 35,
            deliveriesRemaining = 3,
            status = VehicleStatus.MOVING,
            lastUpdated = "5 mins ago"
        ),
        Vehicle(
            id = "veh_05",
            plateNumber = "WP CAD-7733",
            driverId = "drv_05",
            driverName = "Nuwan Jayasinghe",
            latitude = 6.9095,
            longitude = 79.8920,
            speedKmH = 0,
            deliveriesRemaining = 0,
            status = VehicleStatus.OFFLINE,
            lastUpdated = "25 mins ago"
        ),
        Vehicle(
            id = "veh_06",
            plateNumber = "WP CAC-3390",
            driverId = "drv_06",
            driverName = "Dinesh Bandara",
            latitude = 6.9080,
            longitude = 79.8530,
            speedKmH = 55,
            deliveriesRemaining = 4,
            status = VehicleStatus.MOVING,
            lastUpdated = "Just now"
        ),
        Vehicle(
            id = "veh_07",
            plateNumber = "NW SP-1102",
            driverId = "drv_07",
            driverName = "Asanka Cooray",
            latitude = 6.8480,
            longitude = 79.9265,
            speedKmH = 0,
            deliveriesRemaining = 1,
            status = VehicleStatus.IDLE,
            lastUpdated = "10 mins ago"
        ),
        Vehicle(
            id = "veh_08",
            plateNumber = "WP CAR-8821",
            driverId = "drv_08",
            driverName = "Pradeep Gunawardena",
            latitude = 6.9147,
            longitude = 79.8778,
            speedKmH = 42,
            deliveriesRemaining = 6,
            status = VehicleStatus.MOVING,
            lastUpdated = "3 mins ago"
        )
    )

    val initialDeliveries = listOf(
        Delivery(
            id = "del_101",
            trackingCode = "DLK-COL-101",
            customerName = "Nimali Fernando",
            customerPhone = "+94 76 555 1234",
            address = "No. 45, Galle Road, Colombo 03",
            latitude = 6.9080,
            longitude = 79.8530,
            itemsDescription = "Electronics & Accessories Box (2.5 kg)",
            specialInstructions = "Call customer before arrival. Ring doorbell twice.",
            priority = DeliveryPriority.HIGH,
            assignedDriverId = "drv_01",
            assignedDriverName = "Saman Kumara",
            status = DeliveryStatus.OUT_FOR_DELIVERY,
            etaMinutes = 15,
            distanceKm = 3.2,
            payoutLkr = 650.0,
            stopNumber = 1
        ),
        Delivery(
            id = "del_102",
            trackingCode = "DLK-COL-102",
            customerName = "Pathum Nisanka",
            customerPhone = "+94 77 888 9900",
            address = "No. 128, Duplication Road, Colombo 04",
            latitude = 6.8950,
            longitude = 79.8560,
            itemsDescription = "Office Documents Envelope",
            specialInstructions = "Leave with security at main lobby desk.",
            priority = DeliveryPriority.MEDIUM,
            assignedDriverId = "drv_01",
            assignedDriverName = "Saman Kumara",
            status = DeliveryStatus.ASSIGNED,
            etaMinutes = 35,
            distanceKm = 5.8,
            payoutLkr = 450.0,
            stopNumber = 2
        ),
        Delivery(
            id = "del_103",
            trackingCode = "DLK-COL-103",
            customerName = "Dilshan Mendis",
            customerPhone = "+94 71 444 3322",
            address = "No. 12, High Level Road, Nugegoda",
            latitude = 6.8720,
            longitude = 79.8882,
            itemsDescription = "Home Appliances & Cookware",
            specialInstructions = "Fragile package. Handle with care.",
            priority = DeliveryPriority.URGENT,
            assignedDriverId = "drv_01",
            assignedDriverName = "Saman Kumara",
            status = DeliveryStatus.PICKED_UP,
            etaMinutes = 50,
            distanceKm = 8.1,
            payoutLkr = 850.0,
            stopNumber = 3
        ),
        Delivery(
            id = "del_104",
            trackingCode = "DLK-COL-104",
            customerName = "Chanthi Kariyawasam",
            customerPhone = "+94 75 111 2233",
            address = "No. 89, Kynsey Road, Colombo 08",
            latitude = 6.9180,
            longitude = 79.8750,
            itemsDescription = "Medical Supplies & Supplements",
            specialInstructions = "Temperature controlled package.",
            priority = DeliveryPriority.HIGH,
            assignedDriverId = "drv_01",
            assignedDriverName = "Saman Kumara",
            status = DeliveryStatus.DELIVERED,
            etaMinutes = 0,
            distanceKm = 0.0,
            deliveryTime = "10:30 AM",
            recipientName = "Self",
            payoutLkr = 500.0,
            stopNumber = 0
        ),
        Delivery(
            id = "del_105",
            trackingCode = "DLK-COL-105",
            customerName = "Kavinda De Silva",
            customerPhone = "+94 72 333 4455",
            address = "No. 34, Parliament Road, Rajagiriya",
            latitude = 6.9095,
            longitude = 79.8920,
            itemsDescription = "Apparel & Shoes Parcel",
            specialInstructions = "Customer pre-paid online.",
            priority = DeliveryPriority.LOW,
            assignedDriverId = "drv_02",
            assignedDriverName = "Kamal Silva",
            status = DeliveryStatus.PENDING,
            etaMinutes = 60,
            distanceKm = 10.5,
            payoutLkr = 400.0,
            stopNumber = 1
        ),
        Delivery(
            id = "del_106",
            trackingCode = "DLK-COL-106",
            customerName = "Sanduni Jayawardena",
            customerPhone = "+94 78 999 1122",
            address = "No. 210, Baseline Road, Borella",
            latitude = 6.9147,
            longitude = 79.8778,
            itemsDescription = "Gourmet Gift Basket",
            specialInstructions = "Deliver before 4 PM.",
            priority = DeliveryPriority.MEDIUM,
            assignedDriverId = "drv_03",
            assignedDriverName = "Sunil Shantha",
            status = DeliveryStatus.OUT_FOR_DELIVERY,
            etaMinutes = 20,
            distanceKm = 4.0,
            payoutLkr = 550.0,
            stopNumber = 1
        )
    )

    val initialSafetyEvents = listOf(
        SafetyEvent(
            id = "se_01",
            driverId = "drv_01",
            type = SafetyEventType.SPEEDING,
            speedKmH = 74,
            locationText = "High Level Road, Kirulapone (Speed limit 50 km/h)",
            timestamp = "Today 09:15 AM"
        ),
        SafetyEvent(
            id = "se_02",
            driverId = "drv_01",
            type = SafetyEventType.HARSH_BRAKING,
            speedKmH = 42,
            locationText = "Galle Road Junction, Bambalapitiya",
            timestamp = "Yesterday 04:30 PM"
        ),
        SafetyEvent(
            id = "se_03",
            driverId = "drv_03",
            type = SafetyEventType.HARSH_CORNERING,
            speedKmH = 58,
            locationText = "Rajagiriya Flyover Turn",
            timestamp = "Yesterday 11:20 AM"
        )
    )

    val driverEarningsData = listOf(
        Earning("Mon", 2450.0, 5),
        Earning("Tue", 3200.0, 7),
        Earning("Wed", 2800.0, 6),
        Earning("Thu", 4100.0, 9),
        Earning("Fri", 3600.0, 8),
        Earning("Sat", 4800.0, 10),
        Earning("Sun", 1900.0, 4)
    )
}
