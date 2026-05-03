package com.nikhil.aceshiksha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikhil.aceshiksha.models.Quiz
import com.nikhil.aceshiksha.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class TeacherDashboardViewModel : ViewModel() {

    private val repo = QuizRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _quizzes = MutableLiveData<List<Quiz>>(emptyList())
    val quizzes: LiveData<List<Quiz>> = _quizzes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadMyQuizzes() {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repo.getQuizzesByTeacher(uid)
                _quizzes.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load quizzes"
            } finally {
                _isLoading.value = false
            }
        }
    }
}