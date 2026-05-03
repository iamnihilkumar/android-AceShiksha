package com.nikhil.aceshiksha.models

data class QuizQuestion(
    val questionId: String = "",
    val quizId: String = "",
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String = "",   // "A", "B", "C", or "D"
    val orderIndex: Int = 0
)