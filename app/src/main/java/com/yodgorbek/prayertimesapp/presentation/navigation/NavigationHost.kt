package com.yodgorbek.prayertimesapp.presentation.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yodgorbek.prayertimesapp.presentation.*
import com.yodgorbek.prayertimesapp.presentation.viewmodel.PrayerTimeViewModel

@Composable
fun NavigationHost(
    navController: NavHostController,
    viewModel: PrayerTimeViewModel,
    modifier: Modifier,
    context: Context
) {
    NavHost(navController, startDestination = NavRoute.PRAYER_TIMES.name, modifier = modifier) {
        composable(NavRoute.PRAYER_TIMES.name) {
            PrayerTimeScreen(viewModel, context)
        }
        composable(NavRoute.QIBLA.name) {
            QiblaScreen(viewModel, context)
        }
        composable(NavRoute.CITIES.name) {
            CitiesScreen(viewModel, context)
        }
        composable(NavRoute.SETTINGS.name) {
            SettingsScreen(viewModel, context)
        }
    }
}
