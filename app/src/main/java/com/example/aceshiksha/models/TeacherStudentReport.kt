package com.nikhil.aceshiksha.models

data class TeacherStudentReport(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val classLevel: String = "",
    val xp: Int = 0,
    val streak: Int = 0
)