package com.example.deliverlk.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.deliverlk.R
import com.example.deliverlk.domain.model.DeliveryPriority
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.model.Role
import com.example.deliverlk.domain.model.VehicleStatus

@Composable
fun DeliveryStatus.label(): String {
    return when (this) {
        DeliveryStatus.PENDING -> stringResource(R.string.status_pending)
        DeliveryStatus.ASSIGNED -> stringResource(R.string.status_assigned)
        DeliveryStatus.PICKED_UP -> stringResource(R.string.status_picked_up)
        DeliveryStatus.OUT_FOR_DELIVERY -> stringResource(R.string.status_out_for_delivery)
        DeliveryStatus.DELIVERED -> stringResource(R.string.status_delivered)
        DeliveryStatus.FAILED -> stringResource(R.string.status_failed)
        DeliveryStatus.CANCELLED -> stringResource(R.string.status_cancelled)
    }
}

@Composable
fun VehicleStatus.label(): String {
    return when (this) {
        VehicleStatus.MOVING -> stringResource(R.string.moving)
        VehicleStatus.IDLE -> stringResource(R.string.idle)
        VehicleStatus.OFFLINE -> stringResource(R.string.offline)
    }
}

@Composable
fun Role.label(): String {
    return when (this) {
        Role.MANAGER -> stringResource(R.string.role_manager)
        Role.DRIVER -> stringResource(R.string.role_driver)
        Role.CUSTOMER -> stringResource(R.string.role_customer)
    }
}

@Composable
fun DeliveryPriority.label(): String {
    return when (this) {
        DeliveryPriority.LOW -> stringResource(R.string.priority_low)
        DeliveryPriority.MEDIUM -> stringResource(R.string.priority_medium)
        DeliveryPriority.HIGH -> stringResource(R.string.priority_high)
        DeliveryPriority.URGENT -> stringResource(R.string.priority_urgent)
    }
}