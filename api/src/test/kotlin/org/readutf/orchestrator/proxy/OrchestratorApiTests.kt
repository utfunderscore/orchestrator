package org.readutf.orchestrator.proxy

import retrofit2.Retrofit
import kotlin.test.Test

class OrchestratorApiTests {

    val retrofit = Retrofit.Builder().baseUrl("http://localhost:9393").build()

    val orchestratorApi = OrchestratorApi("localhost")

    @Test
    fun `create a template`() {
        orchestratorApi.createService("api-test1", "orchestrator-dev:latest", listOf(8080), hashMapOf())
    }

    @Test
    fun `delete a template`() {
    }
}
