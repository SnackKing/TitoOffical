package com.zachallegretti.tito

import android.widget.Toast
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Composable
fun CreateBetScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore, hostUsername: String) { // Added hostUsername parameter
    var betTitle by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create New Bet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = betTitle,
            onValueChange = { betTitle = it },
            label = { Text("Bet Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (betTitle.isNotBlank() && currentUser != null) {
                    coroutineScope.launch {
                        try {
                            val lobbyId = UUID.randomUUID().toString() // Unique ID for the lobby document
                            val joinCode = generateJoinCode() // Generate a short join code

                            // Fetch host's username
                            val hostProfile = fetchUserProfileBlocking(db, currentUser.uid)
                            val hostUsername = hostProfile["username"] as? String ?: "Unknown Host"

                            val lobbyData = hashMapOf(
                                "betTitle" to betTitle,
                                "hostUid" to currentUser.uid,
                                "hostUsername" to hostUsername, // Store host's username directly
                                "joinCode" to joinCode,
                                "players" to mapOf(currentUser.uid to hostUsername), // Initialize with host
                                "createdAt" to System.currentTimeMillis()
                            )

                            db.collection("betLobbies").document(lobbyId)
                                .set(lobbyData)
                                .await() // Wait for the operation to complete

                            Toast.makeText(context, "Lobby created with code: $joinCode", Toast.LENGTH_LONG).show()
                            navController.navigate(Routes.betLobbyRoute(lobbyId)) {
                                popUpTo(Routes.POINTS) { inclusive = false } // Keep Points screen in back stack
                            }

                        } catch (e: Exception) {
                            Toast.makeText(context, "Error creating lobby: ${e.message}", Toast.LENGTH_LONG).show()
                            println("Error creating lobby: $e")
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter a bet title and ensure you are logged in.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Start Lobby")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text("Back")
        }
    }
}

// Generates a simple 6-character alphanumeric join code
fun generateJoinCode(): String {
    val chars = ('A'..'Z') + ('0'..'9')
    return (1..6).map { chars.random() }.joinToString("")
}

@Composable
fun BetLobbyScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore, lobbyId: String) {
    var betTitle by remember { mutableStateOf("Loading...") }
    var joinCode by remember { mutableStateOf("Loading...") }
    var players by remember { mutableStateOf(mapOf<String, String>()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = auth.currentUser

    DisposableEffect(lobbyId, db) {
        val lobbyRef = db.collection("betLobbies").document(lobbyId)
        val subscription = lobbyRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(context, "Error listening to lobby: ${e.message}", Toast.LENGTH_LONG).show()
                println("Listen failed: $e")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                betTitle = data?.get("betTitle") as? String ?: "Unknown Bet"
                joinCode = data?.get("joinCode") as? String ?: "N/A"
                val playersMap = data?.get("players") as? Map<String, String> ?: emptyMap()
                players = playersMap

                if (currentUser != null && !players.containsKey(currentUser.uid)) {
                    coroutineScope.launch {
                        val userProfile = fetchUserProfileBlocking(db, currentUser.uid)
                        val username = userProfile["username"] as? String ?: "Unknown Player"
                        val updatedPlayers = players.toMutableMap()
                        updatedPlayers[currentUser.uid] = username
                        try {
                            lobbyRef.update("players", updatedPlayers).await()
                            Toast.makeText(context, "Joined lobby: $betTitle", Toast.LENGTH_SHORT).show()
                        } catch (updateError: Exception) {
                            Toast.makeText(context, "Failed to auto-join lobby: ${updateError.message}", Toast.LENGTH_LONG).show()
                            println("Failed to auto-join lobby: $updateError")
                        }
                    }
                }

            } else {
                Toast.makeText(context, "Lobby not found or deleted.", Toast.LENGTH_LONG).show()
                navController.popBackStack(Routes.POINTS, inclusive = false)
            }
        }

        onDispose {
            subscription.remove()
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
            text = "Bet Lobby: $betTitle",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Join Code: $joinCode",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Players in Lobby:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (players.isEmpty()) {
                    Text("No players yet...", color = Color.Gray)
                } else {
                    players.forEach { (uid, username) ->
                        Text("- $username (ID: ${uid.take(4)}...)", fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                Toast.makeText(context, "Share button clicked! (Not yet implemented)", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Share Join Code")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (currentUser != null) {
                    coroutineScope.launch {
                        try {
                            val lobbyRef = db.collection("betLobbies").document(lobbyId)
                            val currentPlayers = players.toMutableMap()
                            currentPlayers.remove(currentUser.uid)
                            lobbyRef.update("players", currentPlayers).await()
                            Toast.makeText(context, "Left lobby.", Toast.LENGTH_SHORT).show()
                            navController.popBackStack(Routes.POINTS, inclusive = false)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error leaving lobby: ${e.message}", Toast.LENGTH_LONG).show()
                            println("Error leaving lobby: $e")
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Leave Lobby")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text("Back")
        }
    }
}