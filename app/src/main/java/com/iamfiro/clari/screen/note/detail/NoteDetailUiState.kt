package com.iamfiro.clari.screen.note.detail

import com.iamfiro.clari.feature.note.component.DetectedTerm
import com.iamfiro.clari.feature.note.model.Note

data class LinkedProject(
    val id: String,
    val name: String
)

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
    val currentWordIndex: Int = -1,

    val linkedProjects: List<LinkedProject> = emptyList(),
    val availableKeywords: Map<String, DetectedTerm> = emptyMap(),
    val displayedTerms: List<DetectedTerm> = emptyList(),

    val shouldTriggerHaptic: Boolean = false,
    val isLoadingKeywords: Boolean = false
)