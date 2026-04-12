package com.example.edureach1.repository

import com.example.edureach1.models.GameQuestion
import com.example.edureach1.utils.Constants.COLLECTION_GAME_QUESTIONS
import com.example.edureach1.utils.Constants.GAME_TOTAL_QUESTIONS
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
}