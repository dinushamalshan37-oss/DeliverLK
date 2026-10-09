package com.example.deliverlk.core.navigation

object ScreenRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Manager
    const val MANAGER_DASHBOARD = "manager_dashboard"
    const val FLEET_LIST = "fleet_list"
    const val DRIVER_DETAIL = "driver_detail/{driverId}"
    fun driverDetail(driverId: String) = "driver_detail/$driverId"

    const val MANAGER_DELIVERIES = "manager_deliveries"
    const val CREATE_DELIVERY = "create_delivery"
    const val MANAGER_DELIVERY_DETAIL = "manager_delivery_detail/{deliveryId}"
    fun managerDeliveryDetail(deliveryId: String) = "manager_delivery_detail/$deliveryId"

    const val REPORTS = "reports"
    const val MANAGER_PROFILE = "manager_profile"

    // Driver
    const val DRIVER_ROUTE = "driver_route"
    const val DRIVER_DELIVERY_DETAIL = "driver_delivery_detail/{deliveryId}"
    fun driverDeliveryDetail(deliveryId: String) = "driver_delivery_detail/$deliveryId"

    const val DRIVER_NAVIGATION = "driver_navigation/{deliveryId}"
    fun driverNavigation(deliveryId: String) = "driver_navigation/$deliveryId"

    const val PROOF_OF_DELIVERY = "proof_of_delivery/{deliveryId}"
    fun proofOfDelivery(deliveryId: String) = "proof_of_delivery/$deliveryId"

    const val DRIVER_EARNINGS = "driver_earnings"
    const val DRIVER_PROFILE = "driver_profile"

    // Customer
    const val CUSTOMER_TRACK = "customer_track"
    const val CUSTOMER_LIVE_TRACKING = "customer_live_tracking/{trackingCode}"
    fun customerLiveTracking(trackingCode: String) = "customer_live_tracking/$trackingCode"

    const val CUSTOMER_DELIVERED = "customer_delivered/{trackingCode}"
    fun customerDelivered(trackingCode: String) = "customer_delivered/$trackingCode"

    const val CUSTOMER_PROFILE = "customer_profile"
}
