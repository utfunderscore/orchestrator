@file:Suppress("ktlint:standard:filename")

package org.readutf.orchestrator.shared.kryo

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.Serializer
import com.esotericsoftware.kryo.kryo5.io.Input
import com.esotericsoftware.kryo.kryo5.io.Output
import java.util.UUID

class UUIDSerializer : Serializer<UUID>() {
    override fun write(
        kryo: Kryo,
        output: Output,
        uuid: UUID,
    ) {
        output.writeLong(uuid.mostSignificantBits)
        output.writeLong(uuid.leastSignificantBits)
    }

    override fun read(
        kryo: Kryo,
        input: Input,
        clazz: Class<out UUID>,
    ): UUID = UUID(input.readLong(), input.readLong())
}
