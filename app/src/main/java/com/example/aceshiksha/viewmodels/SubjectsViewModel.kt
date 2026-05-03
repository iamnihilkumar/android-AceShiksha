package com.nikhil.aceshiksha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikhil.aceshiksha.models.Subject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SubjectsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _subjects = MutableLiveData<List<Subject>>()
    val subjects: LiveData<List<Subject>> = _subjects

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _classLevel = MutableLiveData<String>()   // ← String not Int
    val classLevel: LiveData<String> = _classLevel

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val uid = auth.currentUser?.uid ?: return@launch

                val userDoc = db.collection("users").document(uid).get().await()

                // Handle classLevel as either String or Long/Int
                val classLevel = when (val cl = userDoc.get("classLevel")) {
                    is String -> cl
                    is Long   -> cl.toString()
                    is Int    -> cl.toString()
                    else      -> "8"
                }
                _classLevel.value = classLevel

                val snapshot = db.collection("subjects")
                    .whereEqualTo("classLevel", classLevel)
                    .get()
                    .await()

                val subjectList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Subject::class.java)?.copy(id = doc.id)
                }
                _subjects.value = subjectList

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load subjects"
            } finally {
                _loading.value = false
            }
        }
    }


}