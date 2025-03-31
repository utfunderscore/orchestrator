package org.readutf.orchestrator.proxy.api

import org.readutf.orchestrator.common.template.ServiceTemplate
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface TemplateService {

    @PUT("/template/{name}")
    suspend fun createDockerTemplate(@Path("name") name: String, @Body serviceTemplate: ServiceTemplate): ServiceTemplate

    @DELETE("/template/{name}")
    suspend fun deleteDockerTemplate(@Path("name") name: String)

    @GET("/template")
    suspend fun listTemplates(): List<String>
}
