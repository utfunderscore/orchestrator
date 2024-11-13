package org.readutf.orchestrator.server.server.type.store.impl

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.type.ServerTemplate
import org.readutf.orchestrator.server.server.type.store.TemplateStore
import org.readutf.orchestrator.shared.utils.Result
import java.io.File

class YamlTemplateStore(
    baseDir: File,
) : TemplateStore {
    private val logger = KotlinLogging.logger {}

    private val objectMapper = YAMLMapper().registerKotlinModule()
    val templatesFolder = File(baseDir, "templates")

    init {
        templatesFolder.mkdirs()
    }

    override fun saveTemplate(serverTemplate: ServerTemplate): Result<Unit, String> {
        try {
            val file = File(templatesFolder, "${serverTemplate.templateId}.yml")
            if (!file.exists()) file.createNewFile()
            objectMapper.writeValue(file, serverTemplate)
            return Result.empty()
        } catch (e: Exception) {
            logger.error(e) { "Failed to save template" }
            return Result.failure("Failed to save template: ${e.message}")
        }
    }

    override fun loadTemplate(templateId: String): Result<ServerTemplate, String> {
        val file = File(templatesFolder, "$templateId.yml")
        if (!file.exists()) return Result.failure("Template file does not exist.")
        return Result.success(objectMapper.readValue(file, ServerTemplate::class.java))
    }

    override fun deleteTemplate(templateId: String): Result<Unit, String> {
        val file = File(templatesFolder, "$templateId.yml")
        if (!file.exists()) return Result.failure("Template file does not exist.")
        if (!file.delete()) return Result.failure("Failed to delete template file.")
        return Result.empty()
    }

    override fun loadTemplates(): List<ServerTemplate> {
        val templates = mutableListOf<ServerTemplate>()
        templatesFolder.listFiles()?.forEach {
            try {
                templates.add(objectMapper.readValue(it, ServerTemplate::class.java))
            } catch (e: Exception) {
                logger.error(e) { "Failed to load template ${it.name}" }
            }
        }
        return templates
    }
}
