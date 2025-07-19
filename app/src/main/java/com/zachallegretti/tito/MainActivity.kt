package com.zachallegretti.tito

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.zachallegretti.tito.ui.theme.TitoTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    val appId = "com.zachallegretti.tito"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
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
                                navController = navController, auth = auth, db = db
                            )
                        }
                        composable(Routes.AUTH) {
                            // Pass auth and db to AuthScreen
                            AuthScreen(navController = navController, auth = auth, db = db)
                        }
                        composable(
                            route = Routes.CREATE_BET, // Route now includes username
                            arguments = listOf(navArgument("username") { type = NavType.StringType }),
                            // Explicitly define deep link for this route
                            deepLinks = listOf(navDeepLink { uriPattern = "android-app://androidx.navigation/create_bet/{username}" })
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username")
                            if (username != null) {
                                CreateBetScreen(navController = navController, auth = auth, db = db, hostUsername = username)
                            } else {
                                Toast.makeText(LocalContext.current, "Error: Host username missing.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                        composable(
                            route = Routes.BET_LOBBY,
                            arguments = listOf(navArgument("lobbyId") { type = NavType.StringType }),
                            // Explicitly define deep link for this route
                            deepLinks = listOf(navDeepLink { uriPattern = "android-app://androidx.navigation/bet_lobby/{lobbyId}" })
                        ) { backStackEntry ->
                            val lobbyId = backStackEntry.arguments?.getString("lobbyId")
                            if (lobbyId != null) {
                                BetLobbyScreen(navController = navController, auth = auth, db = db, lobbyId = lobbyId)
                            } else {
                                // Handle error: lobbyId is missing
                                Toast.makeText(LocalContext.current, "Error: Lobby ID missing.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() // Go back if no lobby ID
                            }
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
    fun AuthScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var authMessage by remember { mutableStateOf("") }
        val context = LocalContext.current

        // Observe auth state changes to navigate back to PointsScreen on success
        DisposableEffect(auth) {
            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser != null) {
                    // User signed in successfully, navigate back to Points screen
                    navController.navigate(Routes.POINTS) {
                        // Pop up to the Auth route to prevent going back to login
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            }
            auth.addAuthStateListener(authStateListener)
            onDispose {
                auth.removeAuthStateListener(authStateListener)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sign In / Sign Up",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            AuthForms(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                onSignIn = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    authMessage = "Sign in successful!"
                                } else {
                                    authMessage = "Sign in failed: ${task.exception?.message}"
                                }
                            }
                    } else {
                        authMessage = "Please enter email and password."
                    }
                },
                onSignUp = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    authMessage = "Sign up successful!"
                                    task.result?.user?.uid?.let { uid ->
                                        saveUserProfile(db, uid, email)
                                    }
                                } else {
                                    authMessage = "Sign up failed: ${task.exception?.message}"
                                }
                            }
                    } else {
                        authMessage = "Please enter email and password."
                    }
                }
            )

            if (authMessage.isNotBlank()) {
                Text(
                    text = authMessage,
                    color = if (authMessage.contains("failed")) Color.Red else Color.Green,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

    @Composable
    fun AuthForms(
        email: String,
        onEmailChange: (String) -> Unit,
        password: String,
        onPasswordChange: (String) -> Unit,
        onSignIn: () -> Unit,
        onSignUp: () -> Unit
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Sign In")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onSignUp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Sign Up")
        }
    }

    @Composable
    fun PointsScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
        val currentUser by remember(auth) { mutableStateOf(auth.currentUser) }
        var userDataContent by remember { mutableStateOf("Username") }
        var usernameDisplay by remember { mutableStateOf("Guest") } // This is for the display username

        val context = LocalContext.current

        // Effect to check auth state and navigate if not logged in
        LaunchedEffect(currentUser) {
            if (currentUser == null) {
                // If not logged in, navigate to the Auth screen
                navController.navigate(Routes.AUTH) {
                    // This ensures that if the user presses back from Auth, they go to MainMenu, not an empty PointsScreen
                    popUpTo(Routes.MAIN_MENU)
                }
            } else {
                // If logged in, fetch user data
                fetchUserData(db, currentUser!!.uid) { data ->
                    userDataContent = data
                }
                // Fetch user profile to get the username
                fetchUserProfile(db, currentUser!!.uid) { profile ->
                    usernameDisplay = profile["username"] as? String ?: "Guest"
                }
            }
        }

        // Observe auth state changes for real-time updates and re-fetching data
        DisposableEffect(auth) {
            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser != null) {
                    // User is now logged in or still logged in, fetch data
                    fetchUserData(db, firebaseAuth.currentUser!!.uid) { data ->
                        userDataContent = data
                    }
                    fetchUserProfile(db, firebaseAuth.currentUser!!.uid) { profile ->
                        usernameDisplay = profile["username"] as? String ?: "Guest"
                    }
                } else {
                    // User logged out, clear data and navigate back to Auth or MainMenu if needed
                    userDataContent = "No user data yet. Save something!"
                    // Optionally navigate back to Auth if they log out from this screen
                    // navController.navigate(Routes.AUTH) { popUpTo(Routes.MAIN_MENU) }
                }
            }
            auth.addAuthStateListener(authStateListener)
            onDispose {
                auth.removeAuthStateListener(authStateListener)
            }
        }


        if (currentUser != null) {
            // User is logged in, show the betting game options and user data
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome, ${currentUser?.email ?: "Guest"}!",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Your User ID: ${currentUser?.uid}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // User Data Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your Username",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = userDataContent,
                            onValueChange = { userDataContent = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                currentUser?.uid?.let { uid ->
                                    saveUserData(db, uid, userDataContent) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Username saved!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to save username.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save Username")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // New buttons for betting game
                Button(
                    onClick = { navController.navigate(Routes.createBetRoute(usernameDisplay)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Create Bet")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { Toast.makeText(context, "Join Bet clicked!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text("Join Bet")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        auth.signOut()
                        Toast.makeText(context, "Signed out successfully.", Toast.LENGTH_SHORT).show()
                        // After signing out, navigate back to the Auth screen or Main Menu
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.MAIN_MENU) { inclusive = false } // Keep Main Menu in back stack
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            }
        } else {
            // This else block is primarily for initial composition before LaunchedEffect triggers navigation.
            // In practice, the LaunchedEffect will quickly redirect to Routes.AUTH.
            Text("Checking authentication status...")
        }
    }

    // Function to save user profile (e.g., email) to Firestore
    fun saveUserProfile(db: FirebaseFirestore, uid: String, email: String) {
        val userProfile = hashMapOf(
            "email" to email,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("artifacts").document(appId).collection("users").document(uid)
            .collection("profile").document("userInfo")
            .set(userProfile)
            .addOnSuccessListener {
                println("User profile saved for $uid")
            }
            .addOnFailureListener { e ->
                println("Error saving user profile: $e")
            }
    }

    // Function to save user data to Firestore
    fun saveUserData(db: FirebaseFirestore, uid: String, data: String, onComplete: (Boolean) -> Unit) {
        val userDoc = hashMapOf(
            "username" to data,
            "lastUpdated" to System.currentTimeMillis()
        )
        db.collection("artifacts").document(appId).collection("users").document(uid)
            .collection("userData").document("myUserDoc")
            .set(userDoc)
            .addOnSuccessListener {
                println("User data saved for $uid")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                println("Error saving user data: $e")
                onComplete(false)
            }
    }

    // Function to fetch user data from Firestore
    fun fetchUserData(db: FirebaseFirestore, uid: String, onResult: (String) -> Unit) {
        db.collection("artifacts").document(appId).collection("users").document(uid)
            .collection("userData").document("myUserDoc")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onResult(document.getString("username") ?: "Default Username")
                } else {
                    onResult("Default Username")
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching user data: $e")
                onResult("Error fetching user data.")
            }
    }
}