package com.iamfiro.clari.core.mapper

import android.util.Log
import com.iamfiro.clari.core.network.dto.NoteDto
import com.iamfiro.clari.core.network.dto.NoteListItemDto
import com.iamfiro.clari.core.network.dto.SpeakerDto
import com.iamfiro.clari.core.network.dto.TranscriptContent
import com.iamfiro.clari.feature.note.model.AiSummary
import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.note.model.NoteType
import com.iamfiro.clari.feature.note.model.Speaker
import com.iamfiro.clari.feature.note.model.TranscriptLine
import com.iamfiro.clari.feature.note.model.TranscriptWord
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "NoteMapper"

object NoteMapper {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val isoFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH:mm")
    
    /**
     * NoteListItemDto -> Note (리스트용)
     */
    fun fromListItemDto(dto: NoteListItemDto): Note {
        val createdAt = parseDateTime(dto.createdAt)
        
        return Note(
            id = dto.id,
            type = NoteType.READY, // 리스트에서는 기본적으로 READY
            name = dto.title,
            duration = dto.durationInSeconds.toLong() * 1000,
            createdAt = createdAt,
            recordedAtText = createdAt.format(displayFormatter)
        )
    }
    
    /**
     * NoteDto -> Note (상세용)
     */
    fun fromDto(dto: NoteDto): Note {
        val createdAt = parseDateTime(dto.createdAt)
        val noteType = when (dto.recordingStatus) {
            "completed" -> NoteType.READY
            else -> NoteType.NOT_READY
        }
        
        // AI Summary 파싱
        val aiSummary = dto.aiSummary?.let {
            AiSummary(
                title = "AI 요약",
                content = it
            )
        }
        
        // Speakers 매핑
        val speakers = dto.speakers?.map { 
            Speaker(
                id = it.speaker_id,
                label = it.speaker_name
            )
        } ?: emptyList()
        
        val speakerMap = speakers.associateBy { it.id }
        
        // Transcript 파싱 (새로운 words 구조 우선)
        val (transcripts, formattedText) = parseTranscript(dto.content, speakerMap)
        
        return Note(
            id = dto.id,
            type = noteType,
            name = dto.title,
            duration = dto.durationInSeconds.toLong() * 1000,
            createdAt = createdAt,
            recordedAtText = createdAt.format(displayFormatter),
            aiSummary = aiSummary,
            transcripts = transcripts,
            recordingUrl = dto.recordingUrl,
            speakers = speakers,
            formattedText = formattedText
        )
    }
    
    private fun parseDateTime(isoString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(isoString, isoFormatter)
        } catch (e: Exception) {
            try {
                // ISO 형식에서 Z 제거하고 파싱 시도
                val cleaned = isoString.replace("Z", "").replace(Regex("\\.\\d+"), "")
                LocalDateTime.parse(cleaned)
            } catch (e2: Exception) {
                LocalDateTime.now()
            }
        }
    }
    
    /**
     * content JSON 파싱하여 TranscriptLine 리스트와 포맷된 텍스트 반환
     */
    private fun parseTranscript(
        content: String?,
        speakerMap: Map<String, Speaker>
    ): Pair<List<TranscriptLine>?, String?> {
        if (content.isNullOrEmpty()) return Pair(null, null)
        
        return try {
            val transcriptContent = json.decodeFromString<TranscriptContent>(content)
            
            // 새로운 words 구조가 있는 경우
            if (transcriptContent.words.isNotEmpty()) {
                val transcripts = parseWordsToTranscripts(transcriptContent.words, speakerMap)
                Pair(transcripts, transcriptContent.formatted_text)
            } 
            // 레거시 segments 구조
            else if (transcriptContent.segments.isNotEmpty()) {
                val transcripts = transcriptContent.segments.map { segment ->
                    val speaker = speakerMap[segment.speaker_id] 
                        ?: Speaker(id = segment.speaker_id)
                    
                    TranscriptLine(
                        speaker = speaker,
                        timeSec = segment.start.toInt(),
                        text = segment.text,
                        startMs = (segment.start * 1000).toLong(),
                        endMs = (segment.end * 1000).toLong()
                    )
                }
                Pair(transcripts, null)
            }
            else {
                Pair(null, transcriptContent.formatted_text)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Transcript 파싱 실패", e)
            Pair(null, null)
        }
    }
    
    /**
     * words 배열을 스피커별 TranscriptLine으로 그룹화
     */
    private fun parseWordsToTranscripts(
        words: List<com.iamfiro.clari.core.network.dto.TranscriptWord>,
        speakerMap: Map<String, Speaker>
    ): List<TranscriptLine> {
        if (words.isEmpty()) return emptyList()
        
        val result = mutableListOf<TranscriptLine>()
        var currentSpeakerId: String? = null
        var currentText = StringBuilder()
        var startMs: Long = 0
        var endMs: Long = 0
        var startTimeSec: Int = 0
        
        for (word in words) {
            if (word.type == "spacing") {
                currentText.append(word.text)
                continue
            }
            
            if (currentSpeakerId == null) {
                // 첫 번째 단어
                currentSpeakerId = word.speaker_id
                currentText.append(word.text)
                startMs = (word.start * 1000).toLong()
                startTimeSec = word.start.toInt()
                endMs = (word.end * 1000).toLong()
            } else if (word.speaker_id == currentSpeakerId) {
                // 같은 스피커 - 텍스트 이어붙이기
                currentText.append(word.text)
                endMs = (word.end * 1000).toLong()
            } else {
                // 스피커 변경 - 현재까지의 내용 저장하고 새로 시작
                val speaker = speakerMap[currentSpeakerId] 
                    ?: Speaker(id = currentSpeakerId)
                
                result.add(
                    TranscriptLine(
                        speaker = speaker,
                        timeSec = startTimeSec,
                        text = currentText.toString().trim(),
                        startMs = startMs,
                        endMs = endMs
                    )
                )
                
                // 새 스피커로 리셋
                currentSpeakerId = word.speaker_id
                currentText = StringBuilder(word.text)
                startMs = (word.start * 1000).toLong()
                startTimeSec = word.start.toInt()
                endMs = (word.end * 1000).toLong()
            }
        }
        
        // 마지막 스피커의 텍스트 추가
        if (currentSpeakerId != null && currentText.isNotEmpty()) {
            val speaker = speakerMap[currentSpeakerId] 
                ?: Speaker(id = currentSpeakerId)
            
            result.add(
                TranscriptLine(
                    speaker = speaker,
                    timeSec = startTimeSec,
                    text = currentText.toString().trim(),
                    startMs = startMs,
                    endMs = endMs
                )
            )
        }
        
        return result
    }
    
    /**
     * words 배열을 개별 TranscriptWord로 변환 (하이라이트용)
     */
    fun parseWordsForHighlight(
        content: String?,
        speakerMap: Map<String, Speaker>
    ): List<TranscriptWord> {
        if (content.isNullOrEmpty()) return emptyList()
        
        return try {
            val transcriptContent = json.decodeFromString<TranscriptContent>(content)
            transcriptContent.words.map { word ->
                TranscriptWord(
                    text = word.text,
                    startMs = (word.start * 1000).toLong(),
                    endMs = (word.end * 1000).toLong(),
                    speakerId = word.speaker_id,
                    isSpacing = word.type == "spacing"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Words 파싱 실패", e)
            emptyList()
        }
    }
    
    /**
     * Speaker -> SpeakerDto
     */
    fun toSpeakerDto(speaker: Speaker): SpeakerDto {
        return SpeakerDto(
            speaker_id = speaker.id,
            speaker_name = speaker.label
        )
    }
}

