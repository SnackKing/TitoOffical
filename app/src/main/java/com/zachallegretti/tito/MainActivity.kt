package com.zachallegretti.tito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zachallegretti.tito.ui.theme.TitoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TitoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Get a NavController to manage navigation
                    val navController = rememberNavController()

                    // NavHost defines the navigation graph
                    NavHost(navController = navController, startDestination = Routes.MAIN_MENU) {
                        composable(Routes.MAIN_MENU) {
                            MainMenuScreen(
                                navController = navController // Pass navController to the screen
                            )
                        }
                        composable(Routes.SOUNDBOARD) {
                            SoundboardScreen(
                                navController = navController // Pass navController to the screen
                            )
                        }
                        composable(Routes.EAT_LIKE_TITO) {
                            EatLikeTitoScreen(
                                navController = navController
                            )
                        }
                        composable(Routes.GALLERY) {
                            GalleryScreen(
                                navController = navController
                            )
                        }
                        composable(Routes.POINTS) {
                            PointsScreen(
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MainMenuScreen(navController: NavController) {
        TitoTheme {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Welcome to the Official Daddy App",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )

                Button(onClick = { navController.navigate(Routes.SOUNDBOARD) }) { Text("Soundboard") }
                Button(onClick = { navController.navigate(Routes.EAT_LIKE_TITO) }) { Text("Eat like Tito") }
                Button(onClick = { navController.navigate(Routes.GALLERY) }) { Text("Tito Gallery") }
                Button(onClick = { navController.navigate(Routes.POINTS) }) { Text("Tito Points") }
            }
        }
    }

    @Preview(showBackground = true, name = "Main Menu Preview Light Mode")
    @Composable
    fun MainMenuScreenPreview() {
        TitoTheme { // Always wrap your preview in your app's theme
            val previewNavController = rememberNavController() // Provide a mock NavController
            MainMenuScreen(navController = previewNavController) // Call the actual Composable
        }
    }

    @Composable
    fun EatLikeTitoScreen(navController: NavController) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Eat Like Tito Screen!")
            Button(onClick = { navController.popBackStack() }) { Text("Back") }
        }
    }

    @Composable
    fun GalleryScreen(navController: NavController) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tito Gallery Screen!")
            Button(onClick = { navController.popBackStack() }) { Text("Back") }
        }
    }

    @Composable
    fun PointsScreen(navController: NavController) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tito Points Screen!")
            Button(onClick = { navController.popBackStack() }) { Text("Back") }
        }
    }
}