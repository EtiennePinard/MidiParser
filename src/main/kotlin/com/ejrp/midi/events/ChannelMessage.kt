package com.ejrp.midi.events

import com.ejrp.midi.MissingBytes
import com.ejrp.midi.data.ControllerNumber
import com.ejrp.midi.data.MidiChannelMessageStatus.*
import com.ejrp.midi.data.MidiDataByte
import com.ejrp.midi.data.MidiDataShort
import com.ejrp.midi.data.MidiKey
import com.ejrp.midi.data.MidiVelocity
import com.ejrp.midi.data.Pressure
import com.ejrp.midi.data.ProgramNumber
import com.ejrp.midi.utils.*
import com.ejrp.midi.utils.write
import java.io.InputStream
import java.io.OutputStream
import kotlin.experimental.and
import kotlin.experimental.or

internal interface DeserializeChannelMessage {

    /**
     * Deserializes a channel message from a stream. The partial part is because
     * we need to read the status and the first byte of the channel message because of
     * potential running status of midi channel messages.
     *
     * @param stream The stream to read the next bytes from
     * @param deltaTime The delta-time of this event
     * @param status The status byte of this event
     * @param firstByte The first byte of this event
     * @return The deserialized channel message
     */
    fun fromStreamPartiallyDeserialized(
        stream: InputStream,
        deltaTime: VariableLengthQuantity,
        status: UInt,
        firstByte: Byte
    ): ChannelMessage
}

interface SerializeChannelMessage {
    /**
     * Serializes a channel message and writes it to an output stream
     *
     * @param stream The stream to write the serialized data to
     * @param writeStatusByte Determines if the status byte of this channel message is written or not.
     * Can be used to do running status optimization when writing midi tracks to an output stream
     *
     * @return The stream that was written to
     */
    fun toOutputStream(stream: OutputStream, writeStatusByte: Boolean): OutputStream
}

abstract class ChannelMessage(deltaTime: VariableLengthQuantity, val status: Byte) : MidiTrackEvent(deltaTime),
    SerializeChannelMessage {
    val type = status shr 4
    val channel = status and 0x0F

    override fun totalSize() = super.totalSize() + 1u // delta-time plus 1 for the status byte

    /**
     * Writes this channel message to an output stream. Uses the method from the SerializeChannelMessage
     * interface with the writeStatusByte parameter set to true. For more control call the method from
     * the SerializeChannelMessage interface directly
     *
     * @param stream The stream to write the data to
     *
     * @return The stream that was written to
     */
    override fun toOutputStream(stream: OutputStream) = toOutputStream(stream, true)

    override fun toOutputStream(stream: OutputStream, writeStatusByte: Boolean): OutputStream {
        deltaTime.toOutputStream(stream)
        if (writeStatusByte) stream.write(status)
        return stream
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChannelMessage) return false
        if (!super.equals(other)) return false

        if (status != other.status) return false
        if (type != other.type) return false
        if (channel != other.channel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + status
        result = 31 * result + type
        result = 31 * result + channel
        return result
    }

    companion object : DeserializeChannelMessage {
        override fun fromStreamPartiallyDeserialized(
            stream: InputStream,
            deltaTime: VariableLengthQuantity,
            status: UInt,
            firstByte: Byte
        ): ChannelMessage {
            return when (status shr 4) {
                NOTE_ON.status -> NoteOn.fromStreamPartiallyDeserialized(
                    stream, deltaTime, status, firstByte
                )

                NOTE_OFF.status -> NoteOff.fromStreamPartiallyDeserialized(
                    stream, deltaTime, status, firstByte
                )

                POLYPHONIC_KEY_PRESSURE.status -> PolyphonicKeyPressure.fromStreamPartiallyDeserialized(
                    stream, deltaTime, status, firstByte
                )

                CONTROL_CHANGE.status ->
                    ControlChange.fromStreamPartiallyDeserialized(
                        stream, deltaTime, status, firstByte
                    )

                PROGRAM_CHANGE.status ->
                    ProgramChange.fromStreamPartiallyDeserialized(
                        stream, deltaTime, status, firstByte
                    )

                CHANNEL_PRESSURE.status ->
                    ChannelPressure.fromStreamPartiallyDeserialized(
                        stream, deltaTime, status, firstByte
                    )

                PITCH_WHEEL_CHANGE.status ->
                    PitchWheelChange.fromStreamPartiallyDeserialized(
                        stream, deltaTime, status, firstByte
                    )

                // Note: This code path should never be executed because the status is guaranteed to not be 0xFF, 0xF0 or 0xF7
                else -> throw RuntimeException("This message should never be seen by anyone in the code. This means that something really bad has happened")
            }
        }
    }
}

class NoteOff(deltaTime: VariableLengthQuantity, channel: Byte, val key: MidiKey, val velocity: MidiVelocity) :
    ChannelMessage(deltaTime, (NOTE_OFF.status shl 4).toByte() or channel) {

    override fun totalSize() = super.totalSize() + 2u

    override fun toOutputStream(stream: OutputStream, writeStatusByte: Boolean) =
        super.toOutputStream(stream, writeStatusByte).write(key.key).write(velocity.velocity)

    override fun toString(): String {
        return "NoteOffEvent(tickFromStart=$deltaTime, type=${type.toHexString()}, channel: ${channel.toHexString()}, " +
                "key=$key, velocity=$velocity)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NoteOff) return false
        if (!super.equals(other)) return false

        if (key != other.key) return false
        if (velocity != other.velocity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + velocity.hashCode()
        return result
    }

    companion object : DeserializeChannelMessage {
        override fun fromStreamPartiallyDeserialized(
            stream: InputStream,
            deltaTime: VariableLengthQuantity,
            status: UInt,
            firstByte: Byte
        ): NoteOff {
            val velocity = stream.read()
            if (velocity == -1) throw MissingBytes("note off event", 1, 0)
            val channel = status and 0x0Fu
            return NoteOff(deltaTime, channel.toByte(), MidiKey.fromKey(firstByte), MidiVelocity(velocity.toByte()))
        }
    }
}

class NoteOn(deltaTime: VariableLengthQuantity, channel: Byte, val key: MidiKey, val velocity: MidiVelocity) :
    ChannelMessage(deltaTime, (NOTE_ON.status shl 4).toByte() or channel) {

    override fun totalSize() = super.totalSize() + 2u

    override fun toOutputStream(stream: OutputStream, writeStatusByte: Boolean) =
        super.toOutputStream(stream, writeStatusByte).write(key.key).write(velocity.velocity)

    override fun toString(): String {
        return "NoteOnEvent(tickFromStart=$deltaTime, type=${type.toHexString()}, channel: ${channel.toHexString()}, " +
                "key=$key, velocity=$velocity)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NoteOn) return false
        if (!super.equals(other)) return false

        if (key != other.key) return false
        if (velocity != other.velocity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + velocity.hashCode()
        return result
    }

    companion object : DeserializeChannelMessage {
        override fun fromStreamPartiallyDeserialized(
            stream: InputStream,
            deltaTime: VariableLengthQuantity,
            status: UInt,
            firstByte: Byte
        ): NoteOn {
            val velocity = stream.read()
            if (velocity == -1) throw MissingBytes("note on event", 1, 0)
            val channel = status and 0x0Fu
            return NoteOn(deltaTime, channel.toByte(), MidiKey.fromKey(firstByte), MidiVelocity(velocity.toByte()))
        }
    }
}

class PolyphonicKeyPressure(
    deltaTime: VariableLengthQuantity,
    channel: Byte,
    val key: MidiKey,
    val pressure: Pressure
) :
    ChannelMessage(deltaTime, (POLYPHONIC_KEY_PRESSURE.status shl 4).toByte() or channel) {
    override fun totalSize() = super.totalSize() + 2u

    override fun toOutputStream(stream: OutputStream, writeStatusByte: Boolean) =
        super.toOutputStream(stream, writeStatusByte).write(key.key).write(pressure.data)

    override fun toString(): String {
        return "PolyphonicKeyPressure(tickFromStart=$deltaTime, type=${type.toHexString()}, channel: ${channel.toHexString()}, " +
                "key=$key, pressure=$pressure)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PolyphonicKeyPressure) return false
        if (!super.equals(other)) return false

        if (key != other.key) return false
        if (pressure != other.pressure) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + pressure.hashCode()
        return result
    }

    companion object : DeserializeChannelMessage {
        override fun fromStreamPartiallyDeserialized(
            stream: InputStream,
            deltaTime: VariableLengthQuantity,
            status: UInt,
            firstByte: Byte
        ): PolyphonicKeyPressure {
            val pressure = stream.read()
            if (pressure == -1) throw MissingBytes("polyphonic key pressure event", 1, 0)
            val channel = status and 0x0Fu
            return PolyphonicKeyPressure(
                deltaTime,
                channel.toByte(),
                MidiKey.fromKey(firstByte),
                Pressure(pressure.toByte())
            )
        }
    }
}

class ControlChange(
    deltaTime: VariableLengthQuantity,
    channel: Byte,
    val controllerNumber: ControllerNumber,
    val newValue: MidiDataByte
) : ChannelMessage(deltaTime, (CONTROL_CHANGE.status shl 4).toByte() or channel) {
    override fun totalSize() = super.totalSize() + 2u

    override fun toOutputStream(stream: OutputStream, writeStatusByte: Boolean) =
        super.toOutputStream(stream, writeStatusByte).write(controllerNumber.data).write(newValue.data)

    override fun toString(): String {
        return "ControlChange(tickFromStart=$deltaTime, type=${type.toHexString()}, channel: ${channel.toHexString()}, " +
                "controllerNumber=$controllerNumber, newValue=$newValue)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ControlChange) return false
        if (!super.equals(other)) return false

        if (controllerNumber != other.controllerNumber) return false
        if (newValue != other.newValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + controllerNumber.hashCode()
        result = 31 * result + newValue.hashCode()
        return result
    }

    companion object : DeserializeChannelMessage {
        override fun fromStreamPartiallyDeserialized(
            stream: InputStream,
            deltaTime: VariableLengthQuantity,
            status: UInt,
            firstByte: Byte
        ): ControlChange {
            val newValue = stream.read()
            if (newValue == -1) throw MissingBytes("control change event", 1, 0)
            val channel = status and 0x0Fu
            return ControlChange(
                deltaTime,
                channel.toByte(),
                ControllerNumber(firstByte),
                MidiDataByte(newValue.toByte())
            )
        }
    }
}

class ProgramChange(deltaTime: VariableLengthQuantity, channel: Byte, val programNumber: ProgramNumber) :
    ChannelMessage(deltaTime, (PROGRAM_CHANGE.status shl 4).toByte() or channel) {
    override fun totalSize() = super.totalSize() + 1u

    override fun toOutputStream(stream: OutputStream, writeStatusByte: Boolean) =
        super.toOutputStream(stream, writeStatusByte).write(programNumber.data)

    override fun toString(): String {
        return "ProgramChange(tickFromStart=$deltaTime, type=${type.toHexString()}, channel: ${channel.toHexString()}, " +
                "programNumber=$programNumber)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProgramChange) return false
        if (!super.equals(other)) return false

        if (programNumber != other.programNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + programNumber.hashCode()
        return result
    }

    companion object : DeserializeChannelMessage {
        override fun fromStreamPartiallyDeserialized(
            stream: InputStream,
            deltaTime: VariableLengthQuantity,
            status: UInt,
            firstByte: Byte
        ): ProgramChange {
            val channel = status and 0x0Fu
            return ProgramChange(deltaTime, channel.toByte(), ProgramNumber(firstByte))
        }
    }
}

class ChannelPressure(deltaTime: VariableLengthQuantity, channel: Byte, val pressure: Pressure) :
    ChannelMessage(deltaTime, (CHANNEL_PRESSURE.status shl 4).toByte() or channel) {
    override fun totalSize() = super.totalSize() + 1u

    override fun toOutputStream(stream: OutputStream, writeStatusByte: Boolean) =
        super.toOutputStream(stream, writeStatusByte).write(pressure.data)

    override fun toString(): String {
        return "ChannelPressure(tickFromStart=$deltaTime, type=${type.toHexString()}, channel: ${channel.toHexString()}, " +
                "pressure=$pressure)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChannelPressure) return false
        if (!super.equals(other)) return false

        if (pressure != other.pressure) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + pressure.hashCode()
        return result
    }

    companion object : DeserializeChannelMessage {
        override fun fromStreamPartiallyDeserialized(
            stream: InputStream,
            deltaTime: VariableLengthQuantity,
            status: UInt,
            firstByte: Byte
        ): ChannelPressure {
            val channel = status and 0x0Fu
            return ChannelPressure(deltaTime, channel.toByte(), ProgramNumber(firstByte))
        }
    }
}

class PitchWheelChange(deltaTime: VariableLengthQuantity, channel: Byte, val newValue: MidiDataShort) :
    ChannelMessage(deltaTime, (PITCH_WHEEL_CHANGE.status shl 4).toByte() or channel) {


    override fun totalSize() = super.totalSize() + 2u

    override fun toOutputStream(stream: OutputStream, writeStatusByte: Boolean) =
        super.toOutputStream(stream, writeStatusByte)
            .write(newValue.leastSignificantBits.data) // least significant bits
            .write(newValue.mostSignificantBits.data) // most significant bits

    override fun toString(): String {
        return "PitchWheelChange(tickFromStart=$deltaTime, type=${type.toHexString()}, channel: ${channel.toHexString()}, " +
                "newValue=$newValue)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PitchWheelChange) return false
        if (!super.equals(other)) return false

        if (newValue != other.newValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + newValue.hashCode()
        return result
    }

    companion object : DeserializeChannelMessage {
        override fun fromStreamPartiallyDeserialized(
            stream: InputStream,
            deltaTime: VariableLengthQuantity,
            status: UInt,
            firstByte: Byte
        ): PitchWheelChange {
            val secondByte = stream.read()
            if (secondByte == -1) throw MissingBytes("pitch wheel change event", 1, 0)
            val newValue = MidiDataShort(firstByte, secondByte.toByte())
            val channel = status and 0x0Fu
            return PitchWheelChange(
                deltaTime,
                channel.toByte(),
                newValue
            )
        }
    }
}
