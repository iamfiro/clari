package com.iamfiro.clari.core.repository

import com.iamfiro.clari.core.mapper.NoteMapper
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.network.dto.SpeakerDto
import com.iamfiro.clari.core.network.dto.UpdateNoteRequest
import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.note.model.Speaker

class NoteRepository {
    
    companion object {
        @Volatile
        private var INSTANCE: NoteRepository? = null
        
        fun getInstance(): NoteRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NoteRepository().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 노트 목록 조회
     */
    suspend fun getAllNotes(
        limit: Int = 10,
        sort: String = "recent_used"
    ): Result<List<Note>> {
        return try {
            val response = ApiClient.noteApi.getNotes(limit = limit, sort = sort)
            val notes = response.notes.map { NoteMapper.fromListItemDto(it) }
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 노트 상세 조회
     */
    suspend fun getNoteById(noteId: String): Result<Note> {
        return try {
            val response = ApiClient.noteApi.getNote(noteId)
            val note = NoteMapper.fromDto(response.note)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 노트 제목 수정
     */
    suspend fun updateNoteTitle(noteId: String, title: String): Result<Note> {
        return try {
            val request = UpdateNoteRequest(title = title)
            val response = ApiClient.noteApi.updateNote(noteId, request)
            val note = NoteMapper.fromDto(response.note)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 노트 스피커 수정
     */
    suspend fun updateNoteSpeakers(noteId: String, speakers: List<Speaker>): Result<Note> {
        return try {
            val speakerDtos = speakers.map { NoteMapper.toSpeakerDto(it) }
            val request = UpdateNoteRequest(speakers = speakerDtos)
            val response = ApiClient.noteApi.updateNote(noteId, request)
            val note = NoteMapper.fromDto(response.note)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 노트 삭제
     */
    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            ApiClient.noteApi.deleteNote(noteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


