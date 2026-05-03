package com.nikhil.aceshiksha.repository

import com.nikhil.aceshiksha.models.GameQuestion
import com.nikhil.aceshiksha.utils.Constants.COLLECTION_GAME_QUESTIONS
import com.nikhil.aceshiksha.utils.Constants.GAME_TOTAL_QUESTIONS
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class GameRepository {

    private val db = Firebase.firestore

    suspend fun addGameQuestion(question: GameQuestion): Boolean {
        return try {
            val ref = db.collection(COLLECTION_GAME_QUESTIONS).document()
            ref.set(question.copy(questionId = ref.id)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getGameQuestions(classLevel: String, gameType: String): List<GameQuestion> {
        return try {
            db.collection(COLLECTION_GAME_QUESTIONS)
                .whereEqualTo("classLevel", classLevel)
                .whereEqualTo("gameType", gameType)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(GameQuestion::class.java) }
                .shuffled()
                .take(GAME_TOTAL_QUESTIONS)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addXpToUser(userId: String, xpToAdd: Int): Boolean {
        return try {
            db.collection("users").document(userId)
                .update("xp", com.google.firebase.firestore.FieldValue.increment(xpToAdd.toLong()))
                .await()
            true
        } catch (e: Exception) {
            try {
                // xp field doesn't exist yet — create it
                db.collection("users").document(userId)
                    .set(mapOf("xp" to xpToAdd),
                        com.google.firebase.firestore.SetOptions.merge())
                    .await()
                true
            } catch (e2: Exception) { false }
        }
    }
    suspend fun updateStreak(userId: String): Boolean {
        return try {
            val userRef = db.collection("users").document(userId)
            val userDoc = userRef.get().await()

            val todayStart = getStartOfDayMillis()
            val lastActivityDate = userDoc.getLong("lastActivityDate") ?: 0L
            val currentStreak = (userDoc.getLong("streak") ?: 0L).toInt()

            val newStreak = when {
                lastActivityDate == todayStart ->
                    currentStreak  // already updated today, no change

                lastActivityDate == todayStart - java.util.concurrent.TimeUnit.DAYS.toMillis(1) ->
                    currentStreak + 1  // played yesterday, extend streak

                else -> 1  // missed days, reset
            }

            userRef.update(
                mapOf(
                    "streak"           to newStreak,
                    "lastActivityDate" to todayStart
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getStartOfDayMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}