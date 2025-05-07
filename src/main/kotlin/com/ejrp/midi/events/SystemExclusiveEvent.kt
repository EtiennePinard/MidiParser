package com.ejrp.midi.events

import com.ejrp.midi.MissingBytes
import com.ejrp.midi.utils.*
import java.io.InputStream
import java.io.OutputStream

/**
 * A system exclusive message only has two valid format: F0 or F7.
 * The format of a SysEx message simply means the starting byte of the message.
 *
 * @property byte The byte associated to the format
 * @constructor Create a system exclusive format with the specified byte
 */
enum class SystemExclusiveFormat(val byte: Byte) : Serialize {
    F0_FORMAT(0xF0.toByte()),
    F7_FORMAT(0xF7.toByte());

    override fun toOutputStream(stream: OutputStream) = stream.write(byte)

    // This is just for convenience
    val status: UInt = byte.toUByte().toUInt()
}

/**
 * A system exclusive event in a track chunk.
 *
 * @property messageFormat The format of the SysEx event
 * @property data The data associated to the event
 * @constructor Creates a SysEx event with the specified delta-time, format and data
 *
 * @param deltaTime The delta-time associated to the event
 */
class SystemExclusiveEvent(
    deltaTime: VariableLengthQuantity,
    val messageFormat: SystemExclusiveFormat,
    val data: ByteArray
) : MidiTrackEvent(deltaTime) {

    override fun toOutputStream(stream: OutputStream): OutputStream {
        super.toOutputStream(stream) // Writing the delta time
        messageFormat.toOutputStream(stream) // F0 or F7
        VariableLengthQuantity(data.size.toUInt()).toOutputStream(stream) // Writing the length of the message
        stream.write(data) // The data of the message
        return stream
    }

    override fun totalSize() =
        super.totalSize() + 1u + VariableLengthQuantity(data.size.toUInt()).length().toUInt() + data.size.toUInt()

    override fun toString(): String {
        return "SystemExclusiveEvent(deltaTime=$deltaTime, format=${messageFormat.status.toHexString()}, length=${
            VariableLengthQuantity(
                data.size.toUInt()
            )
        } data=${data.toHexStringList()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SystemExclusiveEvent) return false
        if (!super.equals(other)) return false

        if (messageFormat != other.messageFormat) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + messageFormat.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    companion object {
        internal fun fromInputStream(
            stream: InputStream,
            deltaTime: VariableLengthQuantity,
            status: UInt
        ): SystemExclusiveEvent {
            // The length of the sysex is stored as a variable length quantity
            val length = VariableLengthQuantity.fromInputStream(stream)

            val data = stream.readNBytes(length.quantity.toInt())
            if (data.size != length.quantity.toInt()) {
                throw MissingBytes("system exclusive event", length.quantity.toInt(), data.size)
            }

            return when (status) {
                SystemExclusiveFormat.F0_FORMAT.status -> SystemExclusiveEvent(
                    deltaTime,
                    SystemExclusiveFormat.F0_FORMAT,
                    data
                )

                SystemExclusiveFormat.F7_FORMAT.status -> SystemExclusiveEvent(
                    deltaTime,
                    SystemExclusiveFormat.F7_FORMAT,
                    data
                )

                // Note: This code path should never be executed because the status is guaranteed to be 0xF7 or 0xF0
                else -> throw RuntimeException("This message should never be seen by anyone in the code. This means that something really bad has happened")
            }
        }
    }
}