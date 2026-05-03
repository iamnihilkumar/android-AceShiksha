package com.nikhil.aceshiksha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikhil.aceshiksha.models.GameAttempt
import com.nikhil.aceshiksha.models.GameQuestion
import com.nikhil.aceshiksha.repository.GameRepository
import com.nikhil.aceshiksha.repository.ReportRepository
import com.nikhil.aceshiksha.utils.Constants.XP_GAME_COMPLETE
import com.nikhil.aceshiksha.utils.Constants.XP_GAME_CORRECT
import kotlinx.coroutines.launch

sealed class GameState {
    object Loading : GameState()
    object NoQuestions : GameState()
    data class Playing(val question: GameQuestion, val index: Int, val total: Int) : GameState()
    data class Finished(val correct: Int, val total: Int, val xpEarned: Int) : GameState()
    data class Error(val message: String) : GameState()
}

class GameViewModel : ViewModel() {

    private val repository = GameRepository()
    private val reportRepository = ReportRepository()

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private val _answerResult = MutableLiveData<Boolean?>()
    val answerResult: LiveData<Boolean?> = _answerResult

    private var questionList = listOf<GameQuestion>()
    private var allQuestions = listOf<GameQuestion>()

    private var totalCorrect = 0
    private var totalWrong = 0

    private var currentIndex = 0
    private var correctCount = 0

    private var currentGameType = ""
    private var currentClassLevel = ""
    private var currentStudentUid = ""

    fun loadQuestions(classLevel: String, gameType: String, studentUid: String = "") {
        currentGameType = gameType
        currentClassLevel = classLevel
        currentStudentUid = studentUid
        _gameState.value = GameState.Loading
        viewModelScope.launch {
            val result = repository.getGameQuestions(classLevel, gameType)
            if (result.isEmpty()) {
                _gameState.value = GameState.NoQuestions
            } else {
                questionList = result
                currentIndex = 0
                correctCount = 0
                showCurrentQuestion()
            }
        }
    }

    fun submitAnswer(selected: String) {
        val question = questionList.getOrNull(currentIndex) ?: return
        val isCorrect = selected == question.correctAnswer
        if (isCorrect) correctCount++
        _answerResult.value = isCorrect
    }

    fun nextQuestion() {
        _answerResult.value = null
        currentIndex++
        if (currentIndex >= questionList.size) {
            val xp = (correctCount * XP_GAME_CORRECT) + XP_GAME_COMPLETE
            _gameState.value = GameState.Finished(correctCount, questionList.size, xp)
            saveGameAttempt(correctCount, questionList.size, xp)
        } else {
            showCurrentQuestion()
        }
    }

    private fun showCurrentQuestion() {
        val question = questionList[currentIndex]
        _gameState.value = GameState.Playing(question, currentIndex + 1, questionList.size)
    }

    fun getCurrentQuestion(): GameQuestion? = questionList.getOrNull(currentIndex)

    fun loadNextQuestion() {
        if (allQuestions.isEmpty()) {
            _gameState.value = GameState.NoQuestions
            return
        }
        val question = allQuestions.random()
        _gameState.value = GameState.Playing(question, 0, allQuestions.size)
    }

    fun recordCorrect() {
        totalCorrect++
    }

    fun recordWrong() {
        totalWrong++
    }

    fun loadQuestionsForMaze(classLevel: String, gameType: String, studentUid: String = "") {
        currentGameType = gameType
        currentClassLevel = classLevel
        currentStudentUid = studentUid
        totalCorrect = 0
        totalWrong = 0
        _gameState.value = GameState.Loading
        viewModelScope.launch {
            val result = repository.getGameQuestions(classLevel, gameType)
            if (result.isEmpty()) {
                _gameState.value = GameState.NoQuestions
            } else {
                allQuestions = result
                questionList = result
                loadNextQuestion()
            }
        }
    }

    fun finishMazeGame(correct: Int, total: Int) {
        val xp = (correct * XP_GAME_CORRECT) + XP_GAME_COMPLETE
        _gameState.value = GameState.Finished(correct, total, xp)
        saveGameAttempt(correct, total, xp)
    }

    private fun saveGameAttempt(correct: Int, total: Int, xpEarned: Int) {
        if (currentStudentUid.isEmpty()) return
        val attempt = GameAttempt(
            studentUid = currentStudentUid,
            gameType = currentGameType,
            classLevel = currentClassLevel,
            score = correct,
            totalQuestions = total,
            xpEarned = xpEarned,
            playedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            reportRepository.saveGameAttempt(attempt)
            repository.addXpToUser(currentStudentUid, xpEarned)
            repository.updateStreak(currentStudentUid)
        }
    }
}