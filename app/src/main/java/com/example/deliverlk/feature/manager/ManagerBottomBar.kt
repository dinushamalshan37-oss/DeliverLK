package com.example.deliverlk.feature.manager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.deliverlk.R
import com.example.deliverlk.core.navigation.ScreenRoutes

sealed class ManagerNavItem(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Dashboard : ManagerNavItem(ScreenRoutes.MANAGER_DASHBOARD, R.string.dashboard, Icons.Default.Dashboard)
    object Deliveries : ManagerNavItem(ScreenRoutes.MANAGER_DELIVERIES, R.string.deliveries, Icons.Default.LocalShipping)
    object Reports : ManagerNavItem(ScreenRoutes.REPORTS, R.string.reports, Icons.Default.BarChart)
    object Profile : ManagerNavItem(ScreenRoutes.MANAGER_PROFILE, R.string.profile, Icons.Default.Person)
}

@Composable
fun ManagerBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit
) {
    val items = listOf(
        ManagerNavItem.Dashboard,
        ManagerNavItem.Deliveries,
        ManagerNavItem.Reports,
        ManagerNavItem.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onNavigateToRoute(item.route)
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = stringResource(item.titleRes)) },
                label = { Text(text = stringResource(item.titleRes)) }
            )
        }
    }
}
