package com.iamfiro.clari.screen.note.detail

import com.iamfiro.clari.feature.note.model.Note

data class NoteDetailUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val isMediaReady: Boolean = false,
    val isBuffering: Boolean = false,
    val currentTranscriptIndex: Int = -1,
    val currentWordIndex: Int = -1 // 현재 재생 중인 단어 인덱스
)