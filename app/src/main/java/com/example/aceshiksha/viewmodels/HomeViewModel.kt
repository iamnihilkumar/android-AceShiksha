package com.nikhil.aceshiksha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikhil.aceshiksha.models.User
import com.nikhil.aceshiksha.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class QuizAccuracyData(val accuracy: Int, val quizzesTaken: Int)

class HomeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _quizAccuracy = MutableLiveData<QuizAccuracyData>()
    val quizAccuracy: LiveData<QuizAccuracyData> = _quizAccuracy

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                // Source.SERVER forces a fresh fetch, bypassing Firestore's local cache
                val doc = db.collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .get(Source.SERVER)
                    .await()
                val user = doc.toObject(User::class.java) ?: run {
                    _error.value = "Could not load user data"
                    return@launch
                }
                _user.value = user
            } catch (e: Exception) {
                _error.value = e.message ?: "Something went wrong"
            }
        }
    }

    fun loadQuizAccuracy(studentUid: String) {
        viewModelScope.launch {
            try {
                val docs = db.collection("student_quiz_attempts")
                    .whereEqualTo("studentUid", studentUid)
                    .get().await().documents

                val totalCorrect = docs.sumOf {
                    (it.getLong("correctAnswers") ?: 0L).toInt()
                }
                val totalQ = docs.sumOf {
                    (it.getLong("totalQuestions") ?: 0L).toInt()
                }
                val accuracy = if (totalQ > 0) (totalCorrect * 100 / totalQ) else 0

                _quizAccuracy.value = QuizAccuracyData(
                    accuracy = accuracy,
                    quizzesTaken = docs.size
                )
            } catch (e: Exception) {
                _quizAccuracy.value = QuizAccuracyData(accuracy = 0, quizzesTaken = 0)
            }
        }
    }

    fun getXpProgress(xp: Int): Int = xp % 100
    fun getLevel(xp: Int): Int = (xp / 100) + 1
}