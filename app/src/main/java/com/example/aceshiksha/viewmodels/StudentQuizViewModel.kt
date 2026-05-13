package com.nikhil.aceshiksha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikhil.aceshiksha.models.Quiz
import com.nikhil.aceshiksha.models.QuizQuestion
import com.nikhil.aceshiksha.models.StudentQuizAttempt
import com.nikhil.aceshiksha.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class QuizAttemptState {
    object Idle : QuizAttemptState()
    object Loading : QuizAttemptState()
    data class QuizzesLoaded(val quizzes: List<Quiz>) : QuizAttemptState()
    data class AttemptReady(val questions: List<QuizQuestion>) : QuizAttemptState()
    data class AttemptFinished(val score: Int, val total: Int) : QuizAttemptState()
    data class Error(val message: String) : QuizAttemptState()
}

class StudentQuizViewModel : ViewModel() {

    private val repo = QuizRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableLiveData<QuizAttemptState>(QuizAttemptState.Idle)
    val state: LiveData<QuizAttemptState> = _state

    var currentQuestions: List<QuizQuestion> = emptyList()
    var currentQuizId: String = ""
    var score: Int = 0
    var currentIndex: Int = 0

    fun loadQuizzesForStudent() {
        val uid = auth.currentUser?.uid ?: return
        _state.value = QuizAttemptState.Loading
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                val classLevel = userDoc.getString("classLevel") ?: ""
                val quizzes = repo.getPublishedQuizzesForClass(classLevel)
                _state.value = QuizAttemptState.QuizzesLoaded(quizzes)
            } catch (e: Exception) {
                _state.value = QuizAttemptState.Error(e.message ?: "Failed to load quizzes")
            }
        }
    }

    fun startQuiz(quizId: String) {
        currentQuizId = quizId
        score = 0
        currentIndex = 0
        _state.value = QuizAttemptState.Loading
        viewModelScope.launch {
            try {
                val questions = repo.getQuestionsForQuiz(quizId)
                currentQuestions = questions
                _state.value = QuizAttemptState.AttemptReady(questions)
            } catch (e: Exception) {
                _state.value = QuizAttemptState.Error(e.message ?: "Failed to load questions")
            }
        }
    }

    fun submitAnswer(selected: String) {
        val question = currentQuestions[currentIndex]
        if (selected == question.correctAnswer) score++
        currentIndex++
    }

    fun finishQuiz() {
        val uid = auth.currentUser?.uid ?: return
        val total = currentQuestions.size
        _state.value = QuizAttemptState.Loading
        viewModelScope.launch {
            try {
                val attempt = StudentQuizAttempt(
                    quizId = currentQuizId,
                    studentUid = uid,
                    score = score,
                    totalQuestions = total,
                    correctAnswers = score,
                    attemptedAt = System.currentTimeMillis()
                )
                repo.saveAttempt(attempt)

                // Update XP
                val xpEarned = score * 10
                if (xpEarned > 0) {
                    db.collection("users").document(uid)
                        .update("xp", com.google.firebase.firestore.FieldValue.increment(xpEarned.toLong()))
                        .await()
                }

                updateStreak(uid)

                _state.value = QuizAttemptState.AttemptFinished(score, total)
            } catch (e: Exception) {
                _state.value = QuizAttemptState.Error(e.message ?: "Failed to save attempt")
            }
        }
    }

    /**
     * Streak logic:
     * - store a `lastActivityDate` (epoch ms, start-of-day) in the user document.
     * - If the student completes a quiz today and hasn't already been counted today → streak + 1
     * - If they missed yesterday (gap > 1 day) → streak resets to 1
     * - If they already completed a quiz today → streak unchanged (no double-count)
     */
    private suspend fun updateStreak(uid: String) {
        val userRef = db.collection("users").document(uid)
        val userDoc = userRef.get().await()

        val todayStart = getStartOfDayMillis()
        val lastActivityDate = userDoc.getLong("lastActivityDate") ?: 0L
        val currentStreak = (userDoc.getLong("streak") ?: 0L).toInt()

        val newStreak = when {
            // Already completed a quiz today — don't change streak
            lastActivityDate == todayStart -> currentStreak

            // Completed yesterday — extend streak
            lastActivityDate == todayStart - TimeUnit.DAYS.toMillis(1) -> currentStreak + 1

            // Missed one or more days — reset to 1
            else -> 1
        }

        userRef.update(
            mapOf(
                "streak" to newStreak,
                "lastActivityDate" to todayStart
            )
        ).await()
    }

    /** Returns midnight (00:00:00) of today in the device's local timezone, as epoch ms. */
    private fun getStartOfDayMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}