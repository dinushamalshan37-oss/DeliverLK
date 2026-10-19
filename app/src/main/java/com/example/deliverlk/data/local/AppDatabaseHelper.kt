package com.example.deliverlk.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.deliverlk.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "deliverlk.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: AppDatabaseHelper? = null

        fun getInstance(context: Context): AppDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabaseHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    val deliveriesFlow = MutableStateFlow<List<Delivery>>(emptyList())
    val vehiclesFlow = MutableStateFlow<List<Vehicle>>(emptyList())
    val safetyEventsFlow = MutableStateFlow<List<SafetyEvent>>(emptyList())

    init {
        refreshFlows()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id TEXT PRIMARY KEY,
                name TEXT,
                email TEXT,
                phone TEXT,
                role TEXT,
                company TEXT,
                vehicleId TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE deliveries (
                id TEXT PRIMARY KEY,
                trackingCode TEXT,
                customerName TEXT,
                customerPhone TEXT,
                address TEXT,
                latitude REAL,
                longitude REAL,
                itemsDescription TEXT,
                specialInstructions TEXT,
                priority TEXT,
                assignedDriverId TEXT,
                assignedDriverName TEXT,
                status TEXT,
                etaMinutes INTEGER,
                distanceKm REAL,
                payoutLkr REAL,
                stopNumber INTEGER,
                customerEmail TEXT,
                customerUid TEXT,
                createdBy TEXT,
                pickupAddress TEXT,
                receiverName TEXT,
                receiverPhone TEXT,
                packageWeightKg REAL,
                instructions TEXT,
                companyId TEXT,
                proofPhotoUri TEXT,
                recipientName TEXT,
                note TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE vehicles (
                id TEXT PRIMARY KEY,
                plateNumber TEXT,
                driverId TEXT,
                driverName TEXT,
                latitude REAL,
                longitude REAL,
                speedKmH INTEGER,
                deliveriesRemaining INTEGER,
                status TEXT,
                lastUpdated TEXT,
                heading REAL
            )
        """)

        db.execSQL("""
            CREATE TABLE safety_events (
                id TEXT PRIMARY KEY,
                driverId TEXT,
                type TEXT,
                speedKmH INTEGER,
                locationText TEXT,
                timestamp TEXT
            )
        """)

        // Seed initial data
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('mgr_01', 'Kasun Perera', 'manager@deliverlk.lk', '+94 77 123 4567', 'MANAGER', 'DeliverLK Logistics Sri Lanka', NULL)")

        db.execSQL("INSERT OR REPLACE INTO vehicles VALUES ('veh_01', 'WP-CAB-1234', 'drv_01', 'Saman Kumara', 6.9271, 79.8612, 45, 3, 'MOVING', 'Just now', 90.0)")
        db.execSQL("INSERT OR REPLACE INTO vehicles VALUES ('veh_02', 'WP-PY-5678', 'drv_02', 'Sunil Perera', 6.9319, 79.8428, 0, 2, 'IDLE', '5 mins ago', 0.0)")

        db.execSQL("""
            INSERT OR REPLACE INTO deliveries VALUES (
                'del_101', 'DLK-COL-101', 'Nimali Fernando', '+94 76 555 1234', '45 Galle Road, Colombo 03',
                6.9147, 79.8517, 'Electronics & Accessories', 'Leave at reception if not available', 'HIGH',
                'drv_01', 'Saman Kumara', 'OUT_FOR_DELIVERY', 15, 3.2, 650.0, 1, 'customer@deliverlk.lk',
                'cust_01', 'MANAGER', 'Central Warehouse, Colombo', 'Nimali Fernando', '+94 76 555 1234',
                1.5, 'Handle with care', 'dlk_company_01', NULL, NULL, NULL
            )
        """)
        db.execSQL("""
            INSERT OR REPLACE INTO deliveries VALUES (
                'del_102', 'DLK-COL-102', 'Ruwan Silva', '+94 77 888 9999', '12 Duplication Road, Colombo 04',
                6.8985, 79.8560, 'Documents / Envelope', 'Call upon arrival', 'MEDIUM',
                'drv_01', 'Saman Kumara', 'ASSIGNED', 30, 5.8, 450.0, 2, 'customer@deliverlk.lk',
                'cust_01', 'MANAGER', 'Central Warehouse, Colombo', 'Ruwan Silva', '+94 77 888 9999',
                0.5, 'Urgent document', 'dlk_company_01', NULL, NULL, NULL
            )
        """)

        db.execSQL("INSERT OR REPLACE INTO safety_events VALUES ('evt_01', 'drv_01', 'SPEEDING', 85, 'Borella Junction', '10 mins ago')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS deliveries")
        db.execSQL("DROP TABLE IF EXISTS vehicles")
        db.execSQL("DROP TABLE IF EXISTS safety_events")
        onCreate(db)
    }

    fun refreshFlows() {
        deliveriesFlow.value = getAllDeliveriesList()
        vehiclesFlow.value = getAllVehiclesList()
        safetyEventsFlow.value = getAllSafetyEventsList()
    }

    fun getAllDeliveriesList(): List<Delivery> {
        val list = mutableListOf<Delivery>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM deliveries", null)
        cursor.use {
            while (cursor.moveToNext()) {
                list.add(
                    Delivery(
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        trackingCode = cursor.getString(cursor.getColumnIndexOrThrow("trackingCode")),
                        customerName = cursor.getString(cursor.getColumnIndexOrThrow("customerName")),
                        customerPhone = cursor.getString(cursor.getColumnIndexOrThrow("customerPhone")),
                        address = cursor.getString(cursor.getColumnIndexOrThrow("address")),
                        latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                        longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                        itemsDescription = cursor.getString(cursor.getColumnIndexOrThrow("itemsDescription")),
                        specialInstructions = cursor.getString(cursor.getColumnIndexOrThrow("specialInstructions")),
                        priority = DeliveryPriority.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("priority")) ?: "MEDIUM"),
                        assignedDriverId = cursor.getString(cursor.getColumnIndexOrThrow("assignedDriverId")),
                        assignedDriverName = cursor.getString(cursor.getColumnIndexOrThrow("assignedDriverName")),
                        status = DeliveryStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "PENDING"),
                        etaMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("etaMinutes")),
                        distanceKm = cursor.getDouble(cursor.getColumnIndexOrThrow("distanceKm")),
                        payoutLkr = cursor.getDouble(cursor.getColumnIndexOrThrow("payoutLkr")),
                        stopNumber = cursor.getInt(cursor.getColumnIndexOrThrow("stopNumber")),
                        customerEmail = cursor.getString(cursor.getColumnIndexOrThrow("customerEmail")),
                        customerUid = cursor.getString(cursor.getColumnIndexOrThrow("customerUid")),
                        createdBy = cursor.getString(cursor.getColumnIndexOrThrow("createdBy")),
                        pickupAddress = cursor.getString(cursor.getColumnIndexOrThrow("pickupAddress")),
                        receiverName = cursor.getString(cursor.getColumnIndexOrThrow("receiverName")),
                        receiverPhone = cursor.getString(cursor.getColumnIndexOrThrow("receiverPhone")),
                        packageWeightKg = cursor.getDouble(cursor.getColumnIndexOrThrow("packageWeightKg")),
                        instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions")),
                        companyId = cursor.getString(cursor.getColumnIndexOrThrow("companyId")),
                        proofPhotoUri = cursor.getString(cursor.getColumnIndexOrThrow("proofPhotoUri")),
                        recipientName = cursor.getString(cursor.getColumnIndexOrThrow("recipientName")),
                        note = cursor.getString(cursor.getColumnIndexOrThrow("note")),
                    )
                )
            }
        }
        return list
    }

    fun getAllVehiclesList(): List<Vehicle> {
        val list = mutableListOf<Vehicle>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM vehicles", null)
        cursor.use {
            while (cursor.moveToNext()) {
                list.add(
                    Vehicle(
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        plateNumber = cursor.getString(cursor.getColumnIndexOrThrow("plateNumber")),
                        driverId = cursor.getString(cursor.getColumnIndexOrThrow("driverId")),
                        driverName = cursor.getString(cursor.getColumnIndexOrThrow("driverName")),
                        latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                        longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                        speedKmH = cursor.getInt(cursor.getColumnIndexOrThrow("speedKmH")),
                        deliveriesRemaining = cursor.getInt(cursor.getColumnIndexOrThrow("deliveriesRemaining")),
                        status = VehicleStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "MOVING"),
                        lastUpdated = cursor.getString(cursor.getColumnIndexOrThrow("lastUpdated")),
                        heading = cursor.getDouble(cursor.getColumnIndexOrThrow("heading")),
                    )
                )
            }
        }
        return list
    }

    fun getAllSafetyEventsList(): List<SafetyEvent> {
        val list = mutableListOf<SafetyEvent>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM safety_events", null)
        cursor.use {
            while (cursor.moveToNext()) {
                list.add(
                    SafetyEvent(
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        driverId = cursor.getString(cursor.getColumnIndexOrThrow("driverId")),
                        type = SafetyEventType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("type")) ?: "SPEEDING"),
                        speedKmH = cursor.getInt(cursor.getColumnIndexOrThrow("speedKmH")),
                        locationText = cursor.getString(cursor.getColumnIndexOrThrow("locationText")),
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                    )
                )
            }
        }
        return list
    }
}
