package com.iamfiro.clari.core.network.api

import com.iamfiro.clari.core.network.dto.MessageResponse
import com.iamfiro.clari.core.network.dto.NoteResponse
import com.iamfiro.clari.core.network.dto.NotesResponse
import com.iamfiro.clari.core.network.dto.UpdateNoteRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NoteApi {
    
    @GET("notes")
    suspend fun getNotes(
        @Query("limit") limit: Int = 10,
        @Query("sort") sort: String = "recent_used"
    ): NotesResponse
    
    @GET("notes/{id}")
    suspend fun getNote(@Path("id") id: String): NoteResponse
    
    @PATCH("notes/{id}")
    suspend fun updateNote(
        @Path("id") id: String,
        @Body request: UpdateNoteRequest
    ): NoteResponse
    
    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): MessageResponse
}


