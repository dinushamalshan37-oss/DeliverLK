package com.example.deliverlk

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.deliverlk.core.navigation.ScreenRoutes
import com.example.deliverlk.di.AppContainer
import com.example.deliverlk.domain.model.Role
import com.example.deliverlk.feature.auth.*
import com.example.deliverlk.feature.customer.*
import com.example.deliverlk.feature.driver.*
import com.example.deliverlk.feature.manager.*
import com.example.deliverlk.ui.theme.DeliverLKTheme

@Composable
fun DeliverLkApp(appContainer: AppContainer) {
    DeliverLKTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Determine active role for bottom bars
        val currentUser by appContainer.authRepository.currentUser.collectAsState()

        val isManagerRoute = currentRoute in listOf(
            ScreenRoutes.MANAGER_DASHBOARD,
            ScreenRoutes.MANAGER_DELIVERIES,
            ScreenRoutes.REPORTS,
            ScreenRoutes.MANAGER_PROFILE
        )

        val isDriverRoute = currentRoute in listOf(
            ScreenRoutes.DRIVER_ROUTE,
            ScreenRoutes.DRIVER_EARNINGS,
            ScreenRoutes.DRIVER_PROFILE
        )

        Scaffold(
            bottomBar = {
                if (isManagerRoute && currentUser?.role == Role.MANAGER) {
                    ManagerBottomBar(
                        currentRoute = currentRoute,
                        onNavigateToRoute = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                } else if (isDriverRoute && currentUser?.role == Role.DRIVER) {
                    DriverBottomBar(
                        currentRoute = currentRoute,
                        onNavigateToRoute = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = ScreenRoutes.SPLASH,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Auth Flow
                composable(ScreenRoutes.SPLASH) {
                    SplashScreen(
                        onNavigateNext = {
                            navController.navigate(ScreenRoutes.ONBOARDING) {
                                popUpTo(ScreenRoutes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(ScreenRoutes.ONBOARDING) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            navController.navigate(ScreenRoutes.LOGIN) {
                                popUpTo(ScreenRoutes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }

                composable(ScreenRoutes.LOGIN) {
                    val viewModel: LoginViewModel = viewModel {
                        LoginViewModel(appContainer.authRepository)
                    }
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            val role = appContainer.authRepository.currentUser.value?.role ?: Role.MANAGER
                            val dest = when (role) {
                                Role.MANAGER -> ScreenRoutes.MANAGER_DASHBOARD
                                Role.DRIVER -> ScreenRoutes.DRIVER_ROUTE
                                Role.CUSTOMER -> ScreenRoutes.CUSTOMER_TRACK
                            }
                            navController.navigate(dest) {
                                popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(ScreenRoutes.REGISTER)
                        }
                    )
                }

                composable(ScreenRoutes.REGISTER) {
                    val viewModel: RegisterViewModel = viewModel {
                        RegisterViewModel(appContainer.authRepository)
                    }
                    RegisterScreen(
                        viewModel = viewModel,
                        onRegisterSuccess = {
                            val role = appContainer.authRepository.currentUser.value?.role ?: Role.MANAGER
                            val dest = when (role) {
                                Role.MANAGER -> ScreenRoutes.MANAGER_DASHBOARD
                                Role.DRIVER -> ScreenRoutes.DRIVER_ROUTE
                                Role.CUSTOMER -> ScreenRoutes.CUSTOMER_TRACK
                            }
                            navController.navigate(dest) {
                                popUpTo(ScreenRoutes.REGISTER) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // Manager Graph
                composable(ScreenRoutes.MANAGER_DASHBOARD) {
                    val viewModel: ManagerDashboardViewModel = viewModel {
                        ManagerDashboardViewModel(appContainer.fleetRepository, appContainer.deliveryRepository)
                    }
                    ManagerDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCreateDelivery = { navController.navigate(ScreenRoutes.CREATE_DELIVERY) },
                        onNavigateToFleetList = { navController.navigate(ScreenRoutes.FLEET_LIST) },
                        onNavigateToDriverDetail = { driverId ->
                            navController.navigate(ScreenRoutes.driverDetail(driverId))
                        }
                    )
                }

                composable(ScreenRoutes.FLEET_LIST) {
                    val viewModel: FleetListViewModel = viewModel {
                        FleetListViewModel(appContainer.fleetRepository)
                    }
                    FleetListScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDriverDetail = { driverId ->
                            navController.navigate(ScreenRoutes.driverDetail(driverId))
                        }
                    )
                }

                composable(
                    route = ScreenRoutes.DRIVER_DETAIL,
                    arguments = listOf(navArgument("driverId") { type = NavType.StringType })
                ) { entry ->
                    val driverId = entry.arguments?.getString("driverId") ?: "drv_01"
                    val viewModel: DriverDetailViewModel = viewModel {
                        DriverDetailViewModel(driverId, appContainer.fleetRepository, appContainer.deliveryRepository)
                    }
                    DriverDetailScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDeliveryDetail = { delId ->
                            navController.navigate(ScreenRoutes.managerDeliveryDetail(delId))
                        }
                    )
                }

                composable(ScreenRoutes.MANAGER_DELIVERIES) {
                    val viewModel: ManagerDeliveriesViewModel = viewModel {
                        ManagerDeliveriesViewModel(appContainer.deliveryRepository)
                    }
                    ManagerDeliveriesScreen(
                        viewModel = viewModel,
                        onNavigateToCreateDelivery = { navController.navigate(ScreenRoutes.CREATE_DELIVERY) },
                        onNavigateToDeliveryDetail = { delId ->
                            navController.navigate(ScreenRoutes.managerDeliveryDetail(delId))
                        }
                    )
                }

                composable(ScreenRoutes.CREATE_DELIVERY) {
                    val viewModel: CreateDeliveryViewModel = viewModel {
                        CreateDeliveryViewModel(appContainer.deliveryRepository, appContainer.fleetRepository)
                    }
                    CreateDeliveryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = ScreenRoutes.MANAGER_DELIVERY_DETAIL,
                    arguments = listOf(navArgument("deliveryId") { type = NavType.StringType })
                ) { entry ->
                    val deliveryId = entry.arguments?.getString("deliveryId") ?: "del_101"
                    val viewModel: ManagerDeliveryDetailViewModel = viewModel {
                        ManagerDeliveryDetailViewModel(deliveryId, appContainer.deliveryRepository, appContainer.fleetRepository)
                    }
                    ManagerDeliveryDetailScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(ScreenRoutes.REPORTS) {
                    val viewModel: ReportsViewModel = viewModel {
                        ReportsViewModel(appContainer.deliveryRepository, appContainer.fleetRepository)
                    }
                    ReportsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(ScreenRoutes.MANAGER_PROFILE) {
                    val viewModel: ManagerProfileViewModel = viewModel {
                        ManagerProfileViewModel(appContainer.authRepository)
                    }
                    ManagerProfileScreen(
                        viewModel = viewModel,
                        onLogoutSuccess = {
                            navController.navigate(ScreenRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // Driver Graph
                composable(ScreenRoutes.DRIVER_ROUTE) {
                    val viewModel: DriverRouteViewModel = viewModel {
                        DriverRouteViewModel(appContainer, appContainer.authRepository, appContainer.deliveryRepository)
                    }
                    DriverRouteScreen(
                        viewModel = viewModel,
                        onNavigateToDeliveryDetail = { delId ->
                            navController.navigate(ScreenRoutes.driverDeliveryDetail(delId))
                        },
                        onNavigateToNavigationScreen = { delId ->
                            navController.navigate(ScreenRoutes.driverNavigation(delId))
                        }
                    )
                }

                composable(
                    route = ScreenRoutes.DRIVER_DELIVERY_DETAIL,
                    arguments = listOf(navArgument("deliveryId") { type = NavType.StringType })
                ) { entry ->
                    val deliveryId = entry.arguments?.getString("deliveryId") ?: "del_101"
                    val viewModel: DriverDeliveryDetailViewModel = viewModel {
                        DriverDeliveryDetailViewModel(deliveryId, appContainer.deliveryRepository)
                    }
                    DriverDeliveryDetailScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToNavigation = { delId ->
                            navController.navigate(ScreenRoutes.driverNavigation(delId))
                        }
                    )
                }

                composable(
                    route = ScreenRoutes.DRIVER_NAVIGATION,
                    arguments = listOf(navArgument("deliveryId") { type = NavType.StringType })
                ) { entry ->
                    val deliveryId = entry.arguments?.getString("deliveryId") ?: "del_101"
                    val viewModel: DriverNavigationViewModel = viewModel {
                        DriverNavigationViewModel(deliveryId, appContainer.deliveryRepository)
                    }
                    DriverNavigationScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onArrivedAtDestination = { delId ->
                            navController.navigate(ScreenRoutes.proofOfDelivery(delId)) {
                                popUpTo(ScreenRoutes.DRIVER_NAVIGATION) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = ScreenRoutes.PROOF_OF_DELIVERY,
                    arguments = listOf(navArgument("deliveryId") { type = NavType.StringType })
                ) { entry ->
                    val deliveryId = entry.arguments?.getString("deliveryId") ?: "del_101"
                    val viewModel: ProofOfDeliveryViewModel = viewModel {
                        ProofOfDeliveryViewModel(deliveryId, appContainer.deliveryRepository)
                    }
                    ProofOfDeliveryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onDeliveryCompleted = {
                            navController.navigate(ScreenRoutes.DRIVER_ROUTE) {
                                popUpTo(ScreenRoutes.DRIVER_ROUTE) { inclusive = true }
                            }
                        }
                    )
                }

                composable(ScreenRoutes.DRIVER_EARNINGS) {
                    val viewModel: DriverEarningsViewModel = viewModel {
                        DriverEarningsViewModel(appContainer.deliveryRepository)
                    }
                    DriverEarningsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(ScreenRoutes.DRIVER_PROFILE) {
                    val viewModel: DriverProfileViewModel = viewModel {
                        DriverProfileViewModel(appContainer, appContainer.authRepository, appContainer.fleetRepository)
                    }
                    DriverProfileScreen(
                        viewModel = viewModel,
                        onLogoutSuccess = {
                            navController.navigate(ScreenRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // Customer Graph
                composable(ScreenRoutes.CUSTOMER_TRACK) {
                    val viewModel: CustomerTrackViewModel = viewModel {
                        CustomerTrackViewModel(appContainer.deliveryRepository)
                    }
                    CustomerTrackScreen(
                        viewModel = viewModel,
                        onNavigateToLiveTracking = { trackingCode ->
                            navController.navigate(ScreenRoutes.customerLiveTracking(trackingCode))
                        },
                        onNavigateToProfile = {
                            navController.navigate(ScreenRoutes.CUSTOMER_PROFILE)
                        }
                    )
                }

                composable(
                    route = ScreenRoutes.CUSTOMER_LIVE_TRACKING,
                    arguments = listOf(navArgument("trackingCode") { type = NavType.StringType })
                ) { entry ->
                    val code = entry.arguments?.getString("trackingCode") ?: "DLK-COL-101"
                    val viewModel: CustomerLiveTrackingViewModel = viewModel {
                        CustomerLiveTrackingViewModel(code, appContainer.deliveryRepository, appContainer.fleetRepository)
                    }
                    CustomerLiveTrackingScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDelivered = { trackingCode ->
                            navController.navigate(ScreenRoutes.customerDelivered(trackingCode)) {
                                popUpTo(ScreenRoutes.CUSTOMER_LIVE_TRACKING) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = ScreenRoutes.CUSTOMER_DELIVERED,
                    arguments = listOf(navArgument("trackingCode") { type = NavType.StringType })
                ) { entry ->
                    val code = entry.arguments?.getString("trackingCode") ?: "DLK-COL-101"
                    val viewModel: CustomerDeliveredViewModel = viewModel {
                        CustomerDeliveredViewModel(code, appContainer.deliveryRepository)
                    }
                    CustomerDeliveredScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(ScreenRoutes.CUSTOMER_PROFILE) {
                    val viewModel: CustomerProfileViewModel = viewModel {
                        CustomerProfileViewModel(appContainer.authRepository)
                    }
                    CustomerProfileScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onLogoutSuccess = {
                            navController.navigate(ScreenRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
