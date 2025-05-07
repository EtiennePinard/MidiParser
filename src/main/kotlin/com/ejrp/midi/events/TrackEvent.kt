package com.ejrp.midi.events

import com.ejrp.midi.utils.Serialize
import com.ejrp.midi.utils.VariableLengthQuantity
import java.io.OutputStream

/**
 * A midi track event is an event located inside a track chunk. Every
 * midi event has a delta-time associated with it.
 *
 * @property deltaTime The delta-time associated to the midi event
 * @constructor Create a midi track event with the specified dela-time
 */
abstract class MidiTrackEvent(val deltaTime: VariableLengthQuantity) : Serialize {

    override fun toOutputStream(stream: OutputStream) = deltaTime.toOutputStream(stream)

    /**
     * Computes the total size of the midi track event.
     *
     * @return The total size of the midi track event
     */
    open fun totalSize() = deltaTime.length().toUInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MidiTrackEvent) return false

        if (deltaTime != other.deltaTime) return false

        return true
    }

    override fun hashCode() = deltaTime.hashCode()
}
