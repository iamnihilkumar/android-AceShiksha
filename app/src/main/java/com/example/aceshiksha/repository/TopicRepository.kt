package com.nikhil.aceshiksha.repository

import com.nikhil.aceshiksha.models.SubjectTopic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TopicRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getTopicsForSubject(subject: String, classLevel: String): List<SubjectTopic> {
        val snapshot = db.collection("subject_topics")
            .whereEqualTo("subject", subject)
            .whereEqualTo("classLevel", classLevel)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(SubjectTopic::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun createTopic(topic: SubjectTopic): Boolean {
        return try {
            db.collection("subject_topics").add(topic).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTopic(topicId: String): Boolean {
        return try {
            db.collection("subject_topics").document(topicId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentTeacherUid(): String? = auth.currentUser?.uid
}