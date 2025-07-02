package com.yodgorbek.prayertimesapp



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.yodgorbek.prayertimesapp.presentation.MainScreen
import com.yodgorbek.prayertimesapp.presentation.viewmodel.PrayerTimeViewModel
import com.yodgorbek.prayertimesapp.util.LocalizationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrayerTimesApp()
        }
    }
}

@Composable
fun PrayerTimesApp(viewModel: PrayerTimeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val localizedContext = LocalizationHelper.setLocale(context, viewModel.uiState.value.settings.language)
    MaterialTheme {
        MainScreen(rememberNavController(), viewModel)
    }
}
