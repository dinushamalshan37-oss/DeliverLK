package com.example.deliverlk.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.deliverlk.R
import com.example.deliverlk.data.firebase.FirestoreConstants
import com.google.android.gms.location.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var driverUid: String? = null
    private var vehicleId: String? = null
    private var lastLocation: Location? = null
    private var lastUpdateTime: Long = 0
    private var lastSafetyEventTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    handleNewLocation(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_TRACKING) {
            stopTracking()
            stopSelf()
            return START_NOT_STICKY
        }

        driverUid = intent?.getStringExtra(EXTRA_DRIVER_UID)
        vehicleId = intent?.getStringExtra(EXTRA_VEHICLE_ID) ?: "veh_01"

        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startLocationUpdates()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            7000L
        ).apply {
            setMinUpdateIntervalMillis(4000L)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleNewLocation(location: Location) {
        val now = System.currentTimeMillis()
        val prevLoc = lastLocation
        val distance = if (prevLoc != null) location.distanceTo(prevLoc) else Float.MAX_VALUE
        val timeElapsed = now - lastUpdateTime

        // Throttled writes: write when moved > 25m or > 15s passed
        if (distance >= 25f || timeElapsed >= 15000L) {
            lastLocation = location
            lastUpdateTime = now
            updateVehicleLocationInFirestore(location)
        }

        // Safety event: speed > 60 km/h (speed is m/s in Location object, x 3.6 = km/h)
        val speedKmH = (location.speed * 3.6f).toInt()
        if (speedKmH > 60 && (now - lastSafetyEventTime > 30000L)) {
            lastSafetyEventTime = now
            recordSafetyEvent(location, speedKmH)
        }
    }

    private fun updateVehicleLocationInFirestore(location: Location) {
        val currentVehicleId = vehicleId ?: return
        val speedKmH = (location.speed * 3.6f).toInt()
        val status = if (speedKmH > 3) "MOVING" else "IDLE"

        val data = mapOf(
            FirestoreConstants.VEHICLE_LOCATION to GeoPoint(location.latitude, location.longitude),
            FirestoreConstants.VEHICLE_SPEED_KMH to speedKmH,
            FirestoreConstants.VEHICLE_HEADING to location.bearing,
            FirestoreConstants.VEHICLE_STATUS to status,
            FirestoreConstants.VEHICLE_LAST_UPDATED to FieldValue.serverTimestamp()
        )

        firestore.collection(FirestoreConstants.VEHICLES)
            .document(currentVehicleId)
            .update(data)
    }

    private fun recordSafetyEvent(location: Location, speedKmH: Int) {
        val uid = driverUid ?: return
        val currentVehicleId = vehicleId ?: return

        val event = mapOf(
            FirestoreConstants.SAFETY_COMPANY_ID to FirestoreConstants.DEFAULT_COMPANY_ID,
            FirestoreConstants.SAFETY_DRIVER_UID to uid,
            FirestoreConstants.SAFETY_VEHICLE_ID to currentVehicleId,
            FirestoreConstants.SAFETY_TYPE to "SPEEDING",
            FirestoreConstants.SAFETY_SPEED_KMH to speedKmH,
            FirestoreConstants.SAFETY_LOCATION to GeoPoint(location.latitude, location.longitude),
            FirestoreConstants.SAFETY_LOCATION_TEXT to "Recorded speed ${speedKmH}km/h",
            FirestoreConstants.SAFETY_AT to FieldValue.serverTimestamp()
        )

        firestore.collection(FirestoreConstants.SAFETY_EVENTS).add(event)
    }

    private fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        vehicleId?.let { vId ->
            firestore.collection(FirestoreConstants.VEHICLES)
                .document(vId)
                .update(FirestoreConstants.VEHICLE_STATUS, "OFFLINE")
        }
    }

    override fun onDestroy() {
        stopTracking()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Live Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies driver while live route tracking is active"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DeliverLK Active Duty")
            .setContentText("Sharing live GPS position with fleet dispatcher")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_TRACKING = "com.example.deliverlk.STOP_TRACKING"
        const val EXTRA_DRIVER_UID = "extra_driver_uid"
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"

        fun startService(context: Context, driverUid: String, vehicleId: String) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                putExtra(EXTRA_DRIVER_UID, driverUid)
                putExtra(EXTRA_VEHICLE_ID, vehicleId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }
}
