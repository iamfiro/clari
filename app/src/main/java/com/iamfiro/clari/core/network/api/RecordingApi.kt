package com.iamfiro.clari.core.network.api

import com.iamfiro.clari.core.network.dto.CancelSessionRequest
import com.iamfiro.clari.core.network.dto.CreateSessionRequest
import com.iamfiro.clari.core.network.dto.MessageResponse
import com.iamfiro.clari.core.network.dto.RecordingUrlResponse
import com.iamfiro.clari.core.network.dto.SessionResponse
import com.iamfiro.clari.core.network.dto.StopSessionRequest
import com.iamfiro.clari.core.network.dto.StopSessionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RecordingApi {
    
    @POST("notes/session")
    suspend fun createSession(@Body request: CreateSessionRequest): SessionResponse
    
    @POST("notes/session/stop")
    suspend fun stopSession(@Body request: StopSessionRequest): StopSessionResponse
    
    @POST("notes/session/cancel")
    suspend fun cancelSession(@Body request: CancelSessionRequest): MessageResponse
    
    @GET("notes/record/{noteId}")
    suspend fun getRecordingUrl(@Path("noteId") noteId: String): RecordingUrlResponse
}


