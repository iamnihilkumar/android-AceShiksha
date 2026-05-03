package com.nikhil.aceshiksha.models

data class Lesson(
    val id: String = "",
    val subjectId: String = "",
    val title: String = "",
    val videoUrl: String = "",
    val question: String = "",
    val classLevel: Int = 0,
    val orderIndex: Int = 0
)