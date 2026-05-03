package com.nikhil.aceshiksha.repository

import com.nikhil.aceshiksha.models.Quiz
import com.nikhil.aceshiksha.models.QuizQuestion
import com.nikhil.aceshiksha.models.StudentQuizAttempt
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class QuizRepository {

    private val db = FirebaseFirestore.getInstance()
    private val quizzesCol = db.collection("quizzes")
    private val questionsCol = db.collection("quiz_questions")
    private val attemptsCol = db.collection("student_quiz_attempts")

    suspend fun createQuiz(quiz: Quiz): String {
        val docRef = quizzesCol.document()
        val quizWithId = quiz.copy(quizId = docRef.id)
        val map = mapOf(
            "quizId" to quizWithId.quizId,
            "title" to quizWithId.title,
            "subject" to quizWithId.subject,
            "classLevel" to quizWithId.classLevel,
            "createdBy" to quizWithId.createdBy,
            "createdAt" to quizWithId.createdAt,
            "isPublished" to quizWithId.isPublished
        )
        docRef.set(map).await()
        return docRef.id
    }

    suspend fun addQuestion(question: QuizQuestion) {
        val docRef = questionsCol.document()
        val map = mapOf(
            "questionId" to docRef.id,
            "quizId" to question.quizId,
            "questionText" to question.questionText,
            "optionA" to question.optionA,
            "optionB" to question.optionB,
            "optionC" to question.optionC,
            "optionD" to question.optionD,
            "correctAnswer" to question.correctAnswer,
            "orderIndex" to question.orderIndex
        )
        docRef.set(map).await()
    }

    suspend fun publishQuiz(quizId: String) {
        quizzesCol.document(quizId).update("isPublished", true).await()
    }

    suspend fun getQuizzesByTeacher(teacherUid: String): List<Quiz> {
        val snapshot = quizzesCol
            .whereEqualTo("createdBy", teacherUid)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            Quiz(
                quizId = doc.getString("quizId") ?: "",
                title = doc.getString("title") ?: "",
                subject = doc.getString("subject") ?: "",
                classLevel = doc.getString("classLevel") ?: "",
                createdBy = doc.getString("createdBy") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L,
                isPublished = doc.getBoolean("isPublished") ?: false
            )
        }
    }

    suspend fun getQuestionsForQuiz(quizId: String): List<QuizQuestion> {
        val snapshot = questionsCol
            .whereEqualTo("quizId", quizId)
            .orderBy("orderIndex")
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            QuizQuestion(
                questionId = doc.getString("questionId") ?: "",
                quizId = doc.getString("quizId") ?: "",
                questionText = doc.getString("questionText") ?: "",
                optionA = doc.getString("optionA") ?: "",
                optionB = doc.getString("optionB") ?: "",
                optionC = doc.getString("optionC") ?: "",
                optionD = doc.getString("optionD") ?: "",
                correctAnswer = doc.getString("correctAnswer") ?: "",
                orderIndex = (doc.getLong("orderIndex") ?: 0L).toInt()
            )
        }
    }

    suspend fun getPublishedQuizzesForClass(classLevel: String): List<Quiz> {
        val snapshot = quizzesCol
            .whereEqualTo("classLevel", classLevel)
            .whereEqualTo("isPublished", true)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            Quiz(
                quizId = doc.getString("quizId") ?: "",
                title = doc.getString("title") ?: "",
                subject = doc.getString("subject") ?: "",
                classLevel = doc.getString("classLevel") ?: "",
                createdBy = doc.getString("createdBy") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L,
                isPublished = doc.getBoolean("isPublished") ?: false
            )
        }
    }

    suspend fun saveAttempt(attempt: StudentQuizAttempt) {
        val docRef = attemptsCol.document()
        val map = mapOf(
            "attemptId" to docRef.id,
            "quizId" to attempt.quizId,
            "studentUid" to attempt.studentUid,
            "score" to attempt.score,
            "totalQuestions" to attempt.totalQuestions,
            "correctAnswers" to attempt.correctAnswers,
            "attemptedAt" to attempt.attemptedAt
        )
        docRef.set(map).await()
    }

    suspend fun getAttemptsForStudent(studentUid: String): List<StudentQuizAttempt> {
        val snapshot = attemptsCol
            .whereEqualTo("studentUid", studentUid)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            StudentQuizAttempt(
                attemptId = doc.getString("attemptId") ?: "",
                quizId = doc.getString("quizId") ?: "",
                studentUid = doc.getString("studentUid") ?: "",
                score = (doc.getLong("score") ?: 0L).toInt(),
                totalQuestions = (doc.getLong("totalQuestions") ?: 0L).toInt(),
                correctAnswers = (doc.getLong("correctAnswers") ?: 0L).toInt(),
                attemptedAt = doc.getLong("attemptedAt") ?: 0L
            )
        }
    }
}