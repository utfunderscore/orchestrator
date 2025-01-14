package org.readutf.orchestrator.server.container.impl.docker.store.flatfile

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.michaelbull.result.*
import org.readutf.orchestrator.common.utils.SResult
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

    override fun saveTemplate(template: DockerTemplate): SResult<Unit> {
        templates[template.templateId] = template

        return saveFile()
    }

    override fun deleteTemplate(templateId: String): Result<Unit, String> {
        templates.remove(templateId)

        return saveFile()
    }

    override fun getAllTemplates(
        offset: Long,
        limit: Int,
    ): List<String> = templates.keys.toList()

    override fun getTemplate(templateId: String): SResult<DockerTemplate> =
        templates[templateId]?.let {
            Ok(it)
        } ?: Err("Could not find template with that name")

    private fun saveFile(): Result<Unit, String> =
        runCatching {
            Orchestrator.objectMapper.writeValue(FileOutputStream(dbFile), templates)
        }.mapError {
            it.toString()
        }
}
