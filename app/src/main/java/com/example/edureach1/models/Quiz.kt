package com.example.edureach1.models

data class Quiz(
    val quizId: String = "",
    val title: String = "",
    val subject: String = "",
    val classLevel: String = "",
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val isPublished: Boolean = false
)