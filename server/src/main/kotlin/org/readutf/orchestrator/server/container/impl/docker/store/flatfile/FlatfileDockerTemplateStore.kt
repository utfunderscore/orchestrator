package org.readutf.orchestrator.server.container.impl.docker.store.flatfile

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.michaelbull.result.*
import org.readutf.orchestrator.server.Orchestrator
import org.readutf.orchestrator.server.container.impl.docker.DockerTemplate
import org.readutf.orchestrator.server.container.impl.docker.store.DockerTemplateStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files

class FlatfileDockerTemplateStore(
    private val dbFile: File,
) : DockerTemplateStore {
    private val templates = mutableMapOf<String, DockerTemplate>()

    init {
        if (!dbFile.exists()) {
            dbFile.createNewFile()
            Files.write(dbFile.toPath(), "{}".toByteArray())
        }

        templates.putAll(Orchestrator.objectMapper.readValue<Map<String, DockerTemplate>>(FileInputStream(dbFile)))
    }

    override fun saveTemplate(template: DockerTemplate): Result<Unit, Throwable> {
        templates[template.templateId] = template

        return saveFile()
    }

    override fun deleteTemplate(templateId: String): Result<Unit, Throwable> {
        templates.remove(templateId)

        return saveFile()
    }

    override fun getAllTemplates(
        offset: Long,
        limit: Int,
    ): List<String> = templates.keys.toList()

    override fun getTemplate(templateId: String): Result<DockerTemplate, Throwable> = templates[templateId]?.let {
        Ok(it)
    } ?: Err(Exception("Could not find template with that name"))

    private fun saveFile(): Result<Unit, Throwable> = runCatching {
        Orchestrator.objectMapper.writeValue(FileOutputStream(dbFile), templates)
    }
}
