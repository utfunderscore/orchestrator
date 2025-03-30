package org.readutf.orchestrator.proxy.api

import org.readutf.orchestrator.common.template.docker.DockerTemplate
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface TemplateService {

    @PUT("/template/{name}")
    suspend fun createDockerTemplate(@Path("name") name: String, @Body dockerTemplate: DockerTemplate): DockerTemplate

    @DELETE("/template/{name}")
    suspend fun deleteDockerTemplate(@Path("name") name: String)

    @GET("/template")
    suspend fun listTemplates(): List<String>
}
