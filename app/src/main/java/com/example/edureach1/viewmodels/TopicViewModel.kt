package com.example.edureach1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edureach1.models.SubjectTopic
import com.example.edureach1.network.GeminiContent
import com.example.edureach1.network.GeminiPart
import com.example.edureach1.network.GeminiRequest
import com.example.edureach1.network.RetrofitInstance
import com.example.edureach1.repository.TopicRepository
import com.example.edureach1.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class TopicListState {
    object Loading : TopicListState()
    data class Success(val topics: List<SubjectTopic>) : TopicListState()
    data class Empty(val message: String) : TopicListState()
    data class Error(val message: String) : TopicListState()
}

sealed class TopicFeedbackState {
    object Idle : TopicFeedbackState()
    object Loading : TopicFeedbackState()
    data class Success(val feedback: String) : TopicFeedbackState()
    data class Error(val message: String) : TopicFeedbackState()
}

class TopicViewModel : ViewModel() {

    private val repository = TopicRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _topicListState = MutableLiveData<TopicListState>()
    val topicListState: LiveData<TopicListState> = _topicListState

    private val _feedbackState = MutableLiveData<TopicFeedbackState>(TopicFeedbackState.Idle)
    val feedbackState: LiveData<TopicFeedbackState> = _feedbackState

    private var lastRequestTime = 0L
    private val cooldownMs = 10_000L

    fun loadTopics(subject: String, classLevel: String) {
        viewModelScope.launch {
            _topicListState.value = TopicListState.Loading
            try {
                val topics = repository.getTopicsForSubject(subject, classLevel)
                if (topics.isEmpty()) {
                    _topicListState.value = TopicListState.Empty(
                        "No topics added yet for $subject.\nAsk your teacher to add topics."
                    )
                } else {
                    _topicListState.value = TopicListState.Success(topics)
                }
            } catch (e: Exception) {
                _topicListState.value = TopicListState.Error(
                    e.message ?: "Failed to load topics"
                )
            }
        }
    }

    fun getAiFeedback(topicTitle: String, subject: String, studentInput: String) {
        if (studentInput.trim().length < 10) {
            _feedbackState.value = TopicFeedbackState.Error(
                "Please write at least a sentence about what you know."
            )
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastRequestTime < cooldownMs) {
            val secondsLeft = ((cooldownMs - (now - lastRequestTime)) / 1000) + 1
            _feedbackState.value = TopicFeedbackState.Error(
                "Please wait ${secondsLeft}s before trying again."
            )
            return
        }
        lastRequestTime = now

        _feedbackState.value = TopicFeedbackState.Loading

        val prompt = """
            You are a friendly and encouraging teacher for school students in India.
            
            Subject: $subject
            Topic: $topicTitle
            Student wrote: $studentInput
            
            The student has written what they know about this topic.
            Please provide feedback that:
            - Starts by appreciating what they got right
            - Points out any missing key concepts gently
            - Suggests 1-2 things they should also remember about this topic
            - Ends with an encouraging line
            - Is written in simple English a school student can understand
            - Is 4-5 sentences maximum
            
            Do not use markdown or bullet points. Write in plain flowing text.
        """.trimIndent()

        viewModelScope.launch {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    )
                )
                val response = RetrofitInstance.geminiApi.generateContent(
                    apiKey = Constants.GEMINI_API_KEY,
                    request = request
                )
                val feedbackText = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?: "Good effort! Keep practicing."

                _feedbackState.value = TopicFeedbackState.Success(feedbackText)
                awardXP()

            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("429") == true ->
                        "Too many requests. Please wait a minute and try again."
                    e.message?.contains("403") == true ->
                        "API key error. Please contact your teacher."
                    e.message?.contains("UnknownHost") == true ||
                            e.message?.contains("Unable to resolve") == true ->
                        "No internet connection. Please check your network."
                    else -> "Something went wrong. Please try again."
                }
                _feedbackState.value = TopicFeedbackState.Error(msg)
            }
        }
    }

    private fun awardXP() {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val userRef = db.collection("users").document(uid)
                val userDoc = userRef.get().await()
                val currentXp = userDoc.getLong("xp")?.toInt() ?: 0
                userRef.update("xp", currentXp + Constants.XP_LESSON_COMPLETE).await()
            } catch (e: Exception) {
                // Silent fail — XP not critical
            }
        }
    }

    fun resetFeedback() {
        _feedbackState.value = TopicFeedbackState.Idle
    }
}