package com.example.deliverlk.feature.driver

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.deliverlk.R
import com.example.deliverlk.core.navigation.ScreenRoutes

sealed class DriverNavItem(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Route : DriverNavItem(ScreenRoutes.DRIVER_ROUTE, R.string.route, Icons.Default.Route)
    object Earnings : DriverNavItem(ScreenRoutes.DRIVER_EARNINGS, R.string.earnings, Icons.Default.AttachMoney)
    object Profile : DriverNavItem(ScreenRoutes.DRIVER_PROFILE, R.string.profile, Icons.Default.Person)
}

@Composable
fun DriverBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit
) {
    val items = listOf(
        DriverNavItem.Route,
        DriverNavItem.Earnings,
        DriverNavItem.Profile
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
