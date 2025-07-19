package com.zachallegretti.tito

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

val appId = "com.zachallegretti.tito"


// Blocking version for use in coroutines where await() is needed
suspend fun fetchUserProfileBlocking(db: FirebaseFirestore, uid: String): Map<String, Any> {
    return try {
        val document = db.collection("artifacts").document(appId).collection("users").document(uid)
            .collection("profile").document("userInfo")
            .get()
            .await()
        document.data ?: emptyMap()
    } catch (e: Exception) {
        println("Error fetching user profile blocking: $e")
        emptyMap()
    }
}

// Function to fetch user profile (including username) from Firestore
fun fetchUserProfile(db: FirebaseFirestore, uid: String, onResult: (Map<String, Any>) -> Unit) {
    db.collection("artifacts").document(appId).collection("users").document(uid)
        .collection("profile").document("userInfo")
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                onResult(document.data ?: emptyMap())
            } else {
                onResult(emptyMap())
            }
        }
        .addOnFailureListener { e ->
            println("Error fetching user profile: $e")
            onResult(emptyMap())
        }
}