package com.iamfiro.clari.core.Repository

import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.note.model.NoteType
import com.iamfiro.clari.feature.note.model.AiSummary
import com.iamfiro.clari.feature.note.model.Speaker
import com.iamfiro.clari.feature.note.model.TranscriptLine
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.UUID

class NoteRepository {
    // TODO: 추 후에 실제 API로 변경
    private val mockNotes = mutableListOf(
        Note(
            id = "note_001",
            type = NoteType.READY,
            name = "발표 주제 '킥'과 관련된 아이디어 논의",
            duration = 827_842,
            createdAt = LocalDateTime.parse("2025-12-23T14:30"),
            recordedAtText = "2024년 4월 12일 12:11",
            aiSummary = AiSummary(
                title = "AI 요약",
                content = "이번 회의에서는 AWS 기반 서비스 아키텍처 설계 방향과 Terraform을 활용한 IaC 도입 필요성에 대해 논의했으며, 초기에는 단순한 구조로 시작하되 확장성과 관리 효율을 고려한 설계를 목표로 하기로 했다."
            ),
            transcripts = listOf(
                TranscriptLine(Speaker(1), 0, "오늘 발표 주제는 킥인데, 일단 전체적인 방향부터 얘기해볼게요."),
                TranscriptLine(Speaker(2), 4, "네, 킥을 기술적으로 풀어내는 게 핵심인 거죠?"),
                TranscriptLine(Speaker(1), 8, "맞아요. 단순 아이디어 말고 실제 구현 가능성까지 봐야 해요.")
            )
        ),
        Note(
            id = "note_002",
            type = NoteType.READY,
            name = "AWS 아키텍처 설계 리뷰",
            duration = 1_532_120,
            createdAt = LocalDateTime.parse("2024-12-01T10:05"),
            recordedAtText = "2024년 12월 1일 10:05",
            aiSummary = AiSummary(
                title = "AI 요약",
                content = "AWS 아키텍처 설계에 대한 리뷰 회의에서 주요 인프라 구성 요소와 비용 최적화 방안을 논의했습니다."
            ),
            transcripts = listOf(
                TranscriptLine(Speaker(1), 0, "오늘은 AWS 아키텍처 설계를 리뷰해볼게요."),
                TranscriptLine(Speaker(2), 5, "EC2 인스턴스 타입은 어떻게 선택할까요?")
            )
        ),
        Note(
            id = "note_003",
            type = NoteType.NOT_READY,
            name = "클라우드 스터디 1주차",
            duration = 3_215_000,
            createdAt = LocalDateTime.parse("2024-12-03T19:40")
        ),
        Note(
            id = "note_004",
            type = NoteType.NOT_READY,
            name = "Terraform 상태 파일 설명",
            duration = 642_300,
            createdAt = LocalDateTime.parse("2024-12-05T16:10")
        ),
    )

    private val noteIdMap = mutableMapOf<Note, String>()

    init {
        mockNotes.forEachIndexed { index, note ->
            noteIdMap[note] = "note_${index + 1}"
        }
    }

    suspend fun getAllNotes(): List<Note> {
        delay(1000)
        return mockNotes.toList()
    }

    suspend fun getNoteById(noteId: String): Note? {
        val note = noteIdMap.entries.find { it.value == noteId }?.key
        return if (note != null) {
            note.copy(
                id = noteId,
                recordedAtText = note.createdAt.toString(),
                aiSummary = AiSummary(
                    title = "AI 요약",
                    content = "이번 회의에서는 ${note.name}에 대해 논의했습니다."
                ),
                transcripts = emptyList()
            )
        } else {
            null
        }
    }

    suspend fun createNote(note: Note): Note {
        val newNote = note.copy()
        mockNotes.add(newNote)
        val newId = UUID.randomUUID().toString()
        noteIdMap[newNote] = newId
        return newNote
    }

    suspend fun updateNote(noteId: String, name: String?): Note? {
        val noteEntry = noteIdMap.entries.find { it.value == noteId }
        if (noteEntry == null) return null

        val oldNote = noteEntry.key
        val noteIndex = mockNotes.indexOf(oldNote)
        if (noteIndex == -1) return null

        val updatedNote = oldNote.copy(
            name = name ?: oldNote.name
        )

        mockNotes[noteIndex] = updatedNote
        noteIdMap.remove(oldNote)
        noteIdMap[updatedNote] = noteId

        return updatedNote
    }
}