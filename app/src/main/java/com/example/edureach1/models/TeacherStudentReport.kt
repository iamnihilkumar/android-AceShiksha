package com.example.edureach1.models

data class TeacherStudentReport(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val classLevel: String = "",
    val xp: Int = 0,
    val streak: Int = 0
)