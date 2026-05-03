package com.nikhil.aceshiksha.models

data class GameQuestion(
    val questionId: String = "",
    val teacherUid: String = "",
    val classLevel: String = "",
    val subject: String = "",
    val gameType: String = "",
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String = "",
    val createdAt: Long = 0L
)