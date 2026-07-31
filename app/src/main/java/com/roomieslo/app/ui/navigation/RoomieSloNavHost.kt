package com.roomieslo.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.roomieslo.app.ui.admin.AdminDashboardScreen
import com.roomieslo.app.ui.auth.AcademicVerificationScreen
import com.roomieslo.app.ui.auth.LoginScreen
import com.roomieslo.app.ui.auth.RegisterScreen
import com.roomieslo.app.ui.chat.ChatListScreen
import com.roomieslo.app.ui.chat.ChatScreen
import com.roomieslo.app.ui.favorites.FavoritesScreen
import com.roomieslo.app.ui.listings.CreateListingScreen
import com.roomieslo.app.ui.listings.ListingDetailScreen
import com.roomieslo.app.ui.listings.ListingListScreen
import com.roomieslo.app.ui.profile.LifestyleQuestionnaireScreen
import com.roomieslo.app.ui.profile.ProfileScreen
import com.roomieslo.app.ui.report.ReportUserScreen
import com.roomieslo.app.ui.search.RecommendedProfilesScreen
import com.roomieslo.app.ui.search.SearchScreen

@Composable
fun RoomieSloNavHost(
    navController: NavHostController,
    startDestination: String = Destinations.LOGIN
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destinations.LOGIN) { LoginScreen(navController) }
        composable(Destinations.REGISTER) { RegisterScreen(navController) }
        composable(Destinations.ACADEMIC_VERIFICATION) { AcademicVerificationScreen(navController) }

        composable(Destinations.PROFILE) { ProfileScreen(navController) }
        composable(Destinations.LIFESTYLE_QUESTIONNAIRE) { LifestyleQuestionnaireScreen(navController) }

        composable(Destinations.LISTINGS) { ListingListScreen(navController) }
        composable(
            route = Destinations.LISTING_DETAIL,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { ListingDetailScreen(navController) }
        composable(
            route = "create_listing?listingId={listingId}",
            arguments = listOf(navArgument("listingId") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { CreateListingScreen(navController) }

        composable(Destinations.SEARCH) { SearchScreen(navController) }
        composable(Destinations.RECOMMENDED_PROFILES) { RecommendedProfilesScreen(navController) }

        composable(Destinations.CHAT_LIST) { ChatListScreen(navController) }
        composable(
            route = Destinations.CHAT,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { ChatScreen(navController) }

        composable(Destinations.FAVORITES) { FavoritesScreen(navController) }
        composable(
            route = Destinations.REPORT_USER,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { ReportUserScreen(navController) }
        composable(Destinations.ADMIN_DASHBOARD) { AdminDashboardScreen(navController) }
    }
}
