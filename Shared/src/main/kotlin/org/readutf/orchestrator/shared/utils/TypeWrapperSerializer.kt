package org.readutf.orchestrator.shared.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*

class TypeWrapperSerializer : JsonSerializer<Any>() {
    override fun serialize(
        data: Any,
        generator: JsonGenerator,
        provider: SerializerProvider,
    ) {
        generator.writeStartObject()
        generator.writeStringField("__type", data::class.java.canonicalName)
        generator.writeObjectField("data", data)
        generator.writeEndObject()
    }
}

class TypeWrapperDeserializer : JsonDeserializer<Any>() {
    override fun deserialize(
        parser: JsonParser,
        context: DeserializationContext,
    ): Any {
        val node: JsonNode = parser.codec.readTree(parser)

        val type = Class.forName(node.get("__type").asText())

        val rootNode = node.get("data")

        val jacksonType: JavaType = context.typeFactory.constructType(type)
        val deserializer: JsonDeserializer<*> = context.findRootValueDeserializer(jacksonType)
        val nodeParser: JsonParser = rootNode.traverse(context.parser.codec)
        nodeParser.nextToken()
        return deserializer.deserialize(nodeParser, context)
    }
}
