//package com.example.edureach1.repository
//
//import com.example.edureach1.models.User
//import com.example.edureach1.utils.Constants
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//
//class AuthRepository {
//
//    private val auth = FirebaseAuth.getInstance()
//    private val db = FirebaseFirestore.getInstance()
//
//    suspend fun login(email: String, password: String): Result<User> {
//        return try {
//            val result = auth.signInWithEmailAndPassword(email, password).await()
//            val uid = result.user?.uid ?: return Result.failure(Exception("Login failed"))
//            val userDoc = db.collection(Constants.COLLECTION_USERS).document(uid).get().await()
//            val user = userDoc.toObject(User::class.java) ?: return Result.failure(Exception("User not found"))
//            Result.success(user)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun register(
//        name: String,
//        email: String,
//        password: String,
//        role: String,
//        classLevel: String
//    ): Result<User> {
//        return try {
//            val result = auth.createUserWithEmailAndPassword(email, password).await()
//            val uid = result.user?.uid ?: return Result.failure(Exception("Registration failed"))
//            val user = User(
//                uid = uid,
//                name = name,
//                email = email,
//                role = role,
//                classLevel = classLevel,
//                xp = 0,
//                streak = 0,
//                level = 1
//            )
//            db.collection(Constants.COLLECTION_USERS).document(uid).set(user).await()
//            Result.success(user)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    fun getCurrentUser() = auth.currentUser
//
//    fun logout() = auth.signOut()
//}

//
//package com.example.edureach1.repository
//
//import com.example.edureach1.models.User
//import com.example.edureach1.utils.Constants
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//
//class AuthRepository {
//
//    private val auth = FirebaseAuth.getInstance()
//    private val db = FirebaseFirestore.getInstance()
//
//    suspend fun login(email: String, password: String): Result<User> {
//        return try {
//            val result = auth.signInWithEmailAndPassword(email, password).await()
//            val uid = result.user?.uid ?: return Result.failure(Exception("Login failed"))
//            val userDoc = db.collection(Constants.COLLECTION_USERS).document(uid).get().await()
//            val user = mapDocumentToUser(userDoc.data, uid)
//                ?: return Result.failure(Exception("User not found"))
//            Result.success(user)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun register(
//        name: String,
//        email: String,
//        password: String,
//        role: String,
//        classLevel: String
//    ): Result<User> {
//        return try {
//            val result = auth.createUserWithEmailAndPassword(email, password).await()
//            val uid = result.user?.uid ?: return Result.failure(Exception("Registration failed"))
//            val user = User(
//                uid = uid,
//                name = name,
//                email = email,
//                role = role,
//                classLevel = classLevel,
//                xp = 0,
//                streak = 0,
//                level = 1
//            )
//            db.collection(Constants.COLLECTION_USERS).document(uid).set(user).await()
//            Result.success(user)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // Safely maps Firestore document to User regardless of classLevel type
//    private fun mapDocumentToUser(data: Map<String, Any>?, uid: String): User? {
//        if (data == null) return null
//        return User(
//            uid = uid,
//            name = data["name"] as? String ?: "",
//            email = data["email"] as? String ?: "",
//            role = data["role"] as? String ?: "student",
//            classLevel = when (val cl = data["classLevel"]) {
//                is String -> cl
//                is Long   -> cl.toString()    // handles int64 from Firestore
//                is Int    -> cl.toString()    // handles int
//                else      -> ""
//            },
//            xp = when (val xp = data["xp"]) {
//                is Long -> xp.toInt()
//                is Int  -> xp
//                else    -> 0
//            },
//            streak = when (val s = data["streak"]) {
//                is Long -> s.toInt()
//                is Int  -> s
//                else    -> 0
//            },
//            level = when (val l = data["level"]) {
//                is Long -> l.toInt()
//                is Int  -> l
//                else    -> 1
//            },
//            badges = @Suppress("UNCHECKED_CAST")
//            (data["badges"] as? List<String>) ?: emptyList()
//        )
//    }
//
//    fun getCurrentUser() = auth.currentUser
//
//    fun logout() = auth.signOut()
//
//    fun getUserData(uid: String, onResult: (User?) -> Unit) {
//        db.collection(Constants.COLLECTION_USERS).document(uid)
//            .get()
//            .addOnSuccessListener { doc ->
//                val user = mapDocumentToUser(doc.data, uid)
//                onResult(user)
//            }
//            .addOnFailureListener {
//                onResult(null)
//            }
//    }
//}

package com.example.edureach1.repository

import com.example.edureach1.models.User
import com.example.edureach1.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String, selectedRole: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Login failed"))
            val userDoc = db.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            val user = mapDocumentToUser(userDoc.data, uid)
                ?: return Result.failure(Exception("User not found"))

            // ── Role mismatch check ──────────────────────────────────────────

            if (selectedRole.isNotEmpty() && user.role != selectedRole) {
                auth.signOut()
                val msg = if (user.role == "teacher")
                    "You are registered as a teacher. Please use the Teacher Login page."
                else
                    "You are registered as a student. Please use the Student Login page."
                return Result.failure(Exception(msg))
            }

            // In login(), after role mismatch check, ADD THIS:
            if (result.user?.isEmailVerified == false) {
                auth.signOut()
                return Result.failure(Exception("EMAIL_NOT_VERIFIED"))
            }
            // ────────────────────────────────────────────────────────────────

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        classLevel: String
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Registration failed"))
            val user = User(
                uid = uid,
                name = name,
                email = email,
                role = role,
                classLevel = classLevel,
                xp = 0,
                streak = 0,
                level = 1
            )
            db.collection(Constants.COLLECTION_USERS).document(uid).set(user).await()
            result.user?.sendEmailVerification()?.await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapDocumentToUser(data: Map<String, Any>?, uid: String): User? {
        if (data == null) return null
        return User(
            uid = uid,
            name = data["name"] as? String ?: "",
            email = data["email"] as? String ?: "",
            role = data["role"] as? String ?: "student",
            classLevel = when (val cl = data["classLevel"]) {
                is String -> cl
                is Long   -> cl.toString()
                is Int    -> cl.toString()
                else      -> ""
            },
            xp = when (val xp = data["xp"]) {
                is Long -> xp.toInt()
                is Int  -> xp
                else    -> 0
            },
            streak = when (val s = data["streak"]) {
                is Long -> s.toInt()
                is Int  -> s
                else    -> 0
            },
            level = when (val l = data["level"]) {
                is Long -> l.toInt()
                is Int  -> l
                else    -> 1
            },
            badges = @Suppress("UNCHECKED_CAST")
            (data["badges"] as? List<String>) ?: emptyList()
        )
    }

    fun getCurrentUser() = auth.currentUser

    fun logout() = auth.signOut()

    fun getUserData(uid: String, onResult: (User?) -> Unit) {
        db.collection(Constants.COLLECTION_USERS).document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val user = mapDocumentToUser(doc.data, uid)
                onResult(user)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("No user signed in"))
            user.sendEmailVerification().await()
            auth.signOut() // sign out after resending — they still need to verify
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}