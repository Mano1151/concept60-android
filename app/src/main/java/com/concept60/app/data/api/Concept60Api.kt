package com.concept60.app.data.api

import com.concept60.app.data.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Concept60Api {

    @POST("api/concept")
    suspend fun getConcept(@Body request: ConceptRequest): ConceptResponse

    @POST("api/video")
    suspend fun getVideo(@Body request: ConceptRequest): VideoResponse

    @POST("api/qa/pdf-question")
    suspend fun askPdfQuestion(@Body request: PdfQuestionRequest): PdfAnswerResponse

    @GET("api/trending")
    suspend fun getTrending(): List<TrendingConcept>

    @GET("api/history")
    suspend fun getSavedSearches(): List<Map<String, Any>>

    @DELETE("api/history/{id}")
    suspend fun deleteSavedSearch(@Path("id") id: String)
}
