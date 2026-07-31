package com.roomieslo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roomieslo.app.ui.auth.AuthGateViewModel
import com.roomieslo.app.ui.navigation.Destinations
import com.roomieslo.app.ui.navigation.RoomieSloNavHost
import com.roomieslo.app.ui.theme.RoomieSloTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.status.SessionStatus

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoomieSloTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RoomieSloApp()
                }
            }
        }
    }
}

/**
 * Vstopna tocka z avtentikacijskim "vratarjem": dokler se seja nalaga iz shrambe, prikazemo
 * nalagalnik; nato zacetni cilj dolocimo enkrat (prijavljen -> oglasi, sicer -> prijava).
 * Kasnejso prijavo/odjavo obravnava navigacija sama, zato NavHosta ne gradimo znova.
 */
@Composable
private fun RoomieSloApp(viewModel: AuthGateViewModel = hiltViewModel()) {
    val status by viewModel.sessionStatus.collectAsState()
    when (status) {
        is SessionStatus.Initializing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            val startDestination = rememberSaveable {
                if (status is SessionStatus.Authenticated) Destinations.LISTINGS else Destinations.LOGIN
            }
            MainScaffold(navController = rememberNavController(), startDestination = startDestination)
        }
    }
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Destinations.LISTINGS, "Oglasi", Icons.Filled.Home),
    BottomTab(Destinations.SEARCH, "Iskanje", Icons.Filled.Search),
    BottomTab(Destinations.CHAT_LIST, "Klepeti", Icons.AutoMirrored.Filled.Chat),
    BottomTab(Destinations.FAVORITES, "Priljubljene", Icons.Filled.Favorite),
    BottomTab(Destinations.PROFILE, "Profil", Icons.Filled.Person)
)

@Composable
private fun MainScaffold(navController: NavHostController, startDestination: String) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            RoomieSloNavHost(navController = navController, startDestination = startDestination)
        }
    }
}
