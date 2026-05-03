package com.nikhil.aceshiksha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nikhil.aceshiksha.models.TeacherStudentReport
import com.google.firebase.firestore.FirebaseFirestore

class TeacherStudentListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _students = MutableLiveData<List<TeacherStudentReport>>()
    val students: LiveData<List<TeacherStudentReport>> = _students

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allStudents: List<TeacherStudentReport> = emptyList()

    fun loadStudents() {
        _isLoading.value = true
        db.collection("users")
            .whereEqualTo("role", "student")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        TeacherStudentReport(
                            userId = doc.id,
                            name = doc.getString("name") ?: "Unknown",
                            email = doc.getString("email") ?: "",
                            classLevel = doc.getString("classLevel") ?: "",
                            xp = (doc.getLong("xp") ?: 0L).toInt(),
                            streak = (doc.getLong("streak") ?: 0L).toInt()
                        )
                    } catch (e: Exception) { null }
                }.sortedBy { it.name }
                allStudents = list
                _students.value = list
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message
                _isLoading.value = false
            }
    }

    fun filterStudents(query: String) {
        _students.value = if (query.isBlank()) {
            allStudents
        } else {
            allStudents.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true) ||
                        it.classLevel.contains(query, ignoreCase = true)
            }
        }
    }
}