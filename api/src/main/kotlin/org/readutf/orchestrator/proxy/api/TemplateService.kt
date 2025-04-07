package org.readutf.orchestrator.proxy.api

import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateBody
import org.readutf.orchestrator.common.template.TemplateName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TemplateService {

    @POST("/api/template/{name}")
    suspend fun createTemplate(@Path("name") name: String, @Body templateBody: TemplateBody): ServiceTemplate

    @PUT("/api/template/{name}")
    suspend fun updateTemplate(@Path("name") name: String, @Body templateBody: TemplateBody)

    @PUT("/api/template/{name}/port")
    suspend fun addPort(@Path("name") name: String, @Body body: Map<String, Any>)

    @DELETE("/api/template/{name}/port")
    suspend fun removePort(@Path("name") name: String, @Body body: Map<String, Any>)

    @PUT("/api/template/{name}/image")
    suspend fun setImage(@Path("name") name: String, @Body body: Map<String, Any>)

    @PUT("/api/template/{name}/env")
    suspend fun setEnvironmentVariable(
        @Path("name") name: String,
        @Body body: Map<String, Any>,
    ): ServiceTemplate

    @DELETE("/api/template/{name}/env")
    suspend fun removeEnvironmentVariable(
        @Path("name") name: String,
        @Body body: Map<String, Any>,
    ): ServiceTemplate

    @DELETE("/api/template/{name}")
    suspend fun deleteTemplate(@Path("name") name: String)

    @GET("/api/template/{name}")
    suspend fun getTemplate(@Path("name") name: TemplateName): ServiceTemplate

    @GET("/api/template")
    suspend fun listTemplates(): List<ServiceTemplate>
}
