package com.ejrp.midi.events

import com.ejrp.midi.utils.Serialize
import com.ejrp.midi.utils.VariableLengthQuantity
import java.io.InputStream
import java.io.OutputStream

internal interface DeserializeNonChannelMessages {
    fun fromStreamPartiallyDeserialized(stream: InputStream, deltaTime: VariableLengthQuantity, status: UInt): MidiTrackEvent
}

abstract class MidiTrackEvent(val deltaTime: VariableLengthQuantity) : Serialize {

    override fun toOutputStream(stream: OutputStream) = deltaTime.toOutputStream(stream)

    open fun totalSize() = deltaTime.length().toUInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MidiTrackEvent) return false

        if (deltaTime != other.deltaTime) return false

        return true
    }

    override fun hashCode() = deltaTime.hashCode()
}
