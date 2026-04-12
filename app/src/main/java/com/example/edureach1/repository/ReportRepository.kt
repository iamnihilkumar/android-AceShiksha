package com.example.edureach1.repository

import com.example.edureach1.models.GameAttempt
import com.example.edureach1.models.GameTypeStat
import com.example.edureach1.models.StudentReport
import com.example.edureach1.models.SubjectStat
import com.example.edureach1.models.User
import com.example.edureach1.utils.Constants.COLLECTION_GAME_ATTEMPTS
import com.example.edureach1.utils.Constants.COLLECTION_QUIZZES
import com.example.edureach1.utils.Constants.COLLECTION_QUIZ_ATTEMPTS
import com.example.edureach1.utils.Constants.COLLECTION_USERS
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ReportRepository {

    private val db = Firebase.firestore

    // ─── Game Attempts ───────────────────────────────────────────────

    suspend fun saveGameAttempt(attempt: GameAttempt): Boolean {
        return try {
            val ref = db.collection(COLLECTION_GAME_ATTEMPTS).document()
            ref.set(attempt.copy(attemptId = ref.id)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ─── Student Report ──────────────────────────────────────────────

    suspend fun getStudentReport(studentUid: String, classLevel: String): StudentReport {
        return try {
            // 1. Fetch quiz attempts
            val quizAttempts = db.collection(COLLECTION_QUIZ_ATTEMPTS)
                .whereEqualTo("studentUid", studentUid)
                .get()
                .await()
                .documents

            // 2. Fetch game attempts
            val gameAttempts = db.collection(COLLECTION_GAME_ATTEMPTS)
                .whereEqualTo("studentUid", studentUid)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(GameAttempt::class.java) }

            // 3. Fetch quizzes to get subject info
            val quizIds = quizAttempts.mapNotNull { it.getString("quizId") }.distinct()
            val quizSubjectMap = mutableMapOf<String, String>()
            if (quizIds.isNotEmpty()) {
                // Firestore whereIn limit is 30
                quizIds.chunked(30).forEach { chunk ->
                    db.collection(COLLECTION_QUIZZES)
                        .whereIn("quizId", chunk)
                        .get()
                        .await()
                        .documents
                        .forEach { doc ->
                            val qId = doc.getString("quizId") ?: return@forEach
                            val subject = doc.getString("subject") ?: "General"
                            quizSubjectMap[qId] = subject
                        }
                }
            }

            // 4. Aggregate quiz stats
            var totalQuizScore = 0
            var totalQuizQuestions = 0
            var bestQuizScore = 0
            var lastQuizDate = 0L
            val subjectMap = mutableMapOf<String, SubjectStat>()

            quizAttempts.forEach { doc ->
                val correct = (doc.getLong("correctAnswers") ?: 0).toInt()  // ← use this
                val total = (doc.getLong("totalQuestions") ?: 0).toInt()
                val attemptedAt = doc.getLong("attemptedAt") ?: 0L
                val quizId = doc.getString("quizId") ?: ""
                val subject = quizSubjectMap[quizId] ?: "General"

                totalQuizScore += correct                                    // ← was `score`
                totalQuizQuestions += total
                if (attemptedAt > lastQuizDate) lastQuizDate = attemptedAt
                val pct = if (total > 0) (correct * 100) / total else 0     // ← was `score`
                if (pct > bestQuizScore) bestQuizScore = pct

                val existing = subjectMap[subject] ?: SubjectStat(subject = subject)
                subjectMap[subject] = existing.copy(
                    quizzesAttempted = existing.quizzesAttempted + 1,
                    totalScore = existing.totalScore + correct,             // ← was `score`
                    totalQuestions = existing.totalQuestions + total
                )
            }

            // 5. Aggregate game stats
            var totalGameScore = 0
            var totalGameQuestions = 0
            var totalXpFromGames = 0
            val gameTypeMap = mutableMapOf<String, GameTypeStat>()

            gameAttempts.forEach { attempt ->
                totalGameScore += attempt.score
                totalGameQuestions += attempt.totalQuestions
                totalXpFromGames += attempt.xpEarned

                val existing = gameTypeMap[attempt.gameType]
                    ?: GameTypeStat(gameType = attempt.gameType)
                gameTypeMap[attempt.gameType] = existing.copy(
                    gamesPlayed = existing.gamesPlayed + 1,
                    totalScore = existing.totalScore + attempt.score,
                    totalQuestions = existing.totalQuestions + attempt.totalQuestions
                )
            }

            // 6. Fetch user for XP/level/streak
            val userDoc = db.collection(COLLECTION_USERS)
                .document(studentUid).get().await()
            val xp = (userDoc.getLong("xp") ?: 0).toInt()
            val level = (userDoc.getLong("level") ?: 1).toInt()
            val streak = (userDoc.getLong("streak") ?: 0).toInt()

            StudentReport(
                totalQuizzesAttempted = quizAttempts.size,
                totalQuizScore = totalQuizScore,
                totalQuizQuestions = totalQuizQuestions,
                bestQuizScore = bestQuizScore,
                lastQuizDate = lastQuizDate,
                totalGamesPlayed = gameAttempts.size,
                totalGameScore = totalGameScore,
                totalGameQuestions = totalGameQuestions,
                totalXpFromGames = totalXpFromGames,
                subjectStats = subjectMap.values.sortedByDescending { it.quizzesAttempted },
                gameTypeStats = gameTypeMap.values.sortedByDescending { it.gamesPlayed },
                totalXp = xp,
                level = level,
                streak = streak
            )
        } catch (e: Exception) {
            StudentReport()
        }
    }

    // ─── Leaderboard ─────────────────────────────────────────────────

    suspend fun getTopUsers(limit: Int = 20): List<User> {
        return try {
            db.collection(COLLECTION_USERS)
                .whereEqualTo("role", "student")
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserRank(studentUid: String, userXp: Int): Int {
        return try {
            val count = db.collection(COLLECTION_USERS)
                .whereEqualTo("role", "student")
                .whereGreaterThan("xp", userXp)
                .get()
                .await()
                .size()
            count + 1
        } catch (e: Exception) {
            -1
        }
    }
}
