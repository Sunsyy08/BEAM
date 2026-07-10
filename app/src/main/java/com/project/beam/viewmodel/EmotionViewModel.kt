package com.project.beam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.beam.data.emotion.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.project.beam.data.slogan.SloganRepository

data class HomeUiState(
    val records: List<RecordResponse> = emptyList(),
    val emotionCards: List<EmotionCardUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class EmotionCardUi(
    val name: String,
    val emoji: String,
    val count: Int,
    val records: List<RecordResponse>
)

sealed class RecordSubmitState {
    object Idle : RecordSubmitState()
    object Loading : RecordSubmitState()
    object Success : RecordSubmitState()
    data class Error(val message: String) : RecordSubmitState()
}

class EmotionViewModel : ViewModel() {

    private val repository = EmotionRepository()

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState

    private val _submitState = MutableStateFlow<RecordSubmitState>(RecordSubmitState.Idle)
    val submitState: StateFlow<RecordSubmitState> = _submitState

    private val _monthlyStats = MutableStateFlow<List<MonthlyEmotionResponse>>(emptyList())
    val monthlyStats: StateFlow<List<MonthlyEmotionResponse>> = _monthlyStats
    private val _deleteState = MutableStateFlow<Boolean?>(null)
    val deleteState: StateFlow<Boolean?> = _deleteState

    private val _lastCategory = MutableStateFlow<String?>(null)
    val lastCategory: StateFlow<String?> = _lastCategory

    private val _slogan = MutableStateFlow<String?>(null)
    val slogan: StateFlow<String?> = _slogan

    // 감정 이모지 매핑
    private val emotionEmojiMap = mapOf(
        "행복" to "☀️",
        "슬픔" to "💧",
        "우울" to "🌧",
        "짜증" to "😤",
        "외로움" to "🌙"
    )

    fun loadHomeData() {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true)
            repository.getMyRecords().fold(
                onSuccess = { records ->
                    // 감정별로 그룹핑
                    val grouped = records.groupBy { it.category }
                    val emotionCards = grouped.map { (category, categoryRecords) ->
                        EmotionCardUi(
                            name = category,
                            emoji = emotionEmojiMap[category] ?: "❓",
                            count = categoryRecords.size,
                            records = categoryRecords
                        )
                    }.sortedByDescending { it.count }

                    _homeState.value = HomeUiState(
                        records = records,
                        emotionCards = emotionCards,
                        isLoading = false
                    )
                },
                onFailure = {
                    _homeState.value = _homeState.value.copy(
                        isLoading = false,
                        error = it.message
                    )
                }
            )
        }
    }

    fun createRecord(content: String) {
        viewModelScope.launch {
            _submitState.value = RecordSubmitState.Loading
            repository.createRecord(content).fold(
                onSuccess = { record ->
                    _lastCategory.value = record.category  // 추가
                    _submitState.value = RecordSubmitState.Success
                    loadHomeData()
                },
                onFailure = {
                    _submitState.value = RecordSubmitState.Error(it.message ?: "등록 실패")
                }
            )
        }
    }

    fun clearLastCategory() {
        _lastCategory.value = null
    }

    fun loadMonthlyStats() {
        viewModelScope.launch {
            repository.getMonthlyStats().fold(
                onSuccess = { _monthlyStats.value = it },
                onFailure = { }
            )
        }
    }

    fun loadSlogan(context: Context) {
        viewModelScope.launch {
            val repository = SloganRepository(context)
            repository.getTodaySlogan().fold(
                onSuccess = { _slogan.value = it },
                onFailure = { _slogan.value = null }
            )
        }
    }

    fun deleteRecord(recordId: Int) {
        viewModelScope.launch {
            repository.deleteRecord(recordId).fold(
                onSuccess = {
                    _deleteState.value = true
                    loadHomeData()
                },
                onFailure = {
                    _deleteState.value = false
                }
            )
        }
    }

    fun resetDeleteState() {
        _deleteState.value = null
    }

    fun resetSubmitState() {
        _submitState.value = RecordSubmitState.Idle
    }
}