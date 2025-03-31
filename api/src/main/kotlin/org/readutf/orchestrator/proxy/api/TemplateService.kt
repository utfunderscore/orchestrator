package org.readutf.orchestrator.proxy.api

import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TemplateService {

    @POST("/api/template/{name}")
    suspend fun createTemplate(@Path("name") name: String, @Body templateBody: TemplateBody): ServiceTemplate

    @PUT("/api/template/{name}")
    suspend fun updateTemplate(@Path("name") name: String, @Body templateBody: TemplateBody): ServiceTemplate

    @GET("/api/template/{name}")
    suspend fun getTemplate(@Path("name") name: String): ServiceTemplate

    @GET("/api/template")
    suspend fun listTemplates(): List<String>
}
