package com.nikhil.aceshiksha.models

data class StudentQuizAttempt(
    val attemptId: String = "",
    val quizId: String = "",
    val studentUid: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val attemptedAt: Long = 0L
)