package com.nikhil.aceshiksha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikhil.aceshiksha.models.SubjectTopic
import com.nikhil.aceshiksha.repository.TopicRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

sealed class CreateTopicState {
    object Idle : CreateTopicState()
    object Loading : CreateTopicState()
    object Success : CreateTopicState()
    data class Error(val message: String) : CreateTopicState()
}

class CreateTopicViewModel : ViewModel() {

    private val repository = TopicRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableLiveData<CreateTopicState>(CreateTopicState.Idle)
    val state: LiveData<CreateTopicState> = _state

    val subjects = listOf("Mathematics", "Science", "English", "History", "Geography")
    val classLevels = listOf("6", "7", "8", "9", "10", "11")

    fun createTopic(
        title: String,
        subject: String,
        classLevel: String,
        description: String
    ) {
        if (title.trim().isEmpty()) {
            _state.value = CreateTopicState.Error("Topic title cannot be empty")
            return
        }

        _state.value = CreateTopicState.Loading

        val topic = SubjectTopic(
            title = title.trim(),
            subject = subject,
            classLevel = classLevel,
            description = description.trim(),
            createdBy = auth.currentUser?.uid ?: "",
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val success = repository.createTopic(topic)
            _state.value = if (success) CreateTopicState.Success
            else CreateTopicState.Error("Failed to create topic. Try again.")
        }
    }

    fun resetState() {
        _state.value = CreateTopicState.Idle
    }
}