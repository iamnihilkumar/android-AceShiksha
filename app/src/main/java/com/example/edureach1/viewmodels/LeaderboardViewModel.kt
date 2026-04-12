package com.example.edureach1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edureach1.models.User
import com.example.edureach1.repository.ReportRepository
import kotlinx.coroutines.launch

data class LeaderboardData(
    val topUsers: List<User>,
    val currentUserRank: Int,
    val currentUserUid: String
)

sealed class LeaderboardState {
    object Loading : LeaderboardState()
    data class Success(val data: LeaderboardData) : LeaderboardState()
    data class Error(val message: String) : LeaderboardState()
}

class LeaderboardViewModel : ViewModel() {

    private val repository = ReportRepository()

    private val _leaderboardState = MutableLiveData<LeaderboardState>()
    val leaderboardState: LiveData<LeaderboardState> = _leaderboardState

    fun loadLeaderboard(currentUser: User) {
        _leaderboardState.value = LeaderboardState.Loading
        viewModelScope.launch {
            val topUsers = repository.getTopUsers(20)
            val rank = repository.getUserRank(currentUser.uid, currentUser.xp)
            _leaderboardState.value = LeaderboardState.Success(
                LeaderboardData(
                    topUsers = topUsers,
                    currentUserRank = rank,
                    currentUserUid = currentUser.uid
                )
            )
        }
    }
}
