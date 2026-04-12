package com.example.edureach1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edureach1.models.StudentReport
import com.example.edureach1.repository.ReportRepository
import kotlinx.coroutines.launch

sealed class ReportState {
    object Loading : ReportState()
    data class Success(val report: StudentReport) : ReportState()
    data class Error(val message: String) : ReportState()
}

class StudentReportViewModel : ViewModel() {

    private val repository = ReportRepository()

    private val _reportState = MutableLiveData<ReportState>()
    val reportState: LiveData<ReportState> = _reportState

    fun loadReport(studentUid: String, classLevel: String) {
        _reportState.value = ReportState.Loading
        viewModelScope.launch {
            val report = repository.getStudentReport(studentUid, classLevel)
            _reportState.value = ReportState.Success(report)
        }
    }
}
