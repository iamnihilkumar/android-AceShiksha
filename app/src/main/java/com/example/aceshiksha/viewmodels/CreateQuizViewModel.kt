package com.nikhil.aceshiksha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikhil.aceshiksha.models.Quiz
import com.nikhil.aceshiksha.models.QuizQuestion
import com.nikhil.aceshiksha.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

sealed class QuizCreateState {
    object Idle : QuizCreateState()
    object Loading : QuizCreateState()
    data class QuizCreated(val quizId: String) : QuizCreateState()
    object QuestionAdded : QuizCreateState()
    object Published : QuizCreateState()
    data class Error(val message: String) : QuizCreateState()
}

class CreateQuizViewModel : ViewModel() {

    private val repo = QuizRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableLiveData<QuizCreateState>(QuizCreateState.Idle)
    val state: LiveData<QuizCreateState> = _state

    var currentQuizId: String = ""
    var questionCount: Int = 0

    fun createQuiz(title: String, subject: String, classLevel: String) {
        val uid = auth.currentUser?.uid ?: return
        _state.value = QuizCreateState.Loading
        viewModelScope.launch {
            try {
                val quiz = Quiz(
                    title = title,
                    subject = subject,
                    classLevel = classLevel,
                    createdBy = uid,
                    createdAt = System.currentTimeMillis(),
                    isPublished = false
                )
                val quizId = repo.createQuiz(quiz)
                currentQuizId = quizId
                _state.value = QuizCreateState.QuizCreated(quizId)
            } catch (e: Exception) {
                _state.value = QuizCreateState.Error(e.message ?: "Failed to create quiz")
            }
        }
    }

    fun addQuestion(
        questionText: String,
        optionA: String, optionB: String,
        optionC: String, optionD: String,
        correctAnswer: String
    ) {
        if (currentQuizId.isEmpty()) return
        _state.value = QuizCreateState.Loading
        viewModelScope.launch {
            try {
                val question = QuizQuestion(
                    quizId = currentQuizId,
                    questionText = questionText,
                    optionA = optionA,
                    optionB = optionB,
                    optionC = optionC,
                    optionD = optionD,
                    correctAnswer = correctAnswer,
                    orderIndex = questionCount
                )
                repo.addQuestion(question)
                questionCount++
                _state.value = QuizCreateState.QuestionAdded
            } catch (e: Exception) {
                _state.value = QuizCreateState.Error(e.message ?: "Failed to add question")
            }
        }
    }

    fun publishQuiz() {
        if (currentQuizId.isEmpty()) return
        _state.value = QuizCreateState.Loading
        viewModelScope.launch {
            try {
                repo.publishQuiz(currentQuizId)
                _state.value = QuizCreateState.Published
            } catch (e: Exception) {
                _state.value = QuizCreateState.Error(e.message ?: "Failed to publish")
            }
        }
    }
}