package com.iamfiro.clwari.feature.note.model

data class Note (
    val name: String,

    val duration: Long,

    val createdAt: Long,
)

val dummy_notes = listOf<Note>(
    Note("발표 주제 '킥'과 관련된 아이디어 논의", 827842, 1703128475),
    Note("발표 주제 '킥'과 관련된 아이디어 논의", 827842, 1703128475),
    Note("발표 주제 '킥'과 관련된 아이디어 논의", 827842, 1703128475),
)