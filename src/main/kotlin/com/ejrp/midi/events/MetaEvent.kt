package com.ejrp.midi.events

import com.ejrp.midi.InvalidMetaEventLength
import com.ejrp.midi.MissingBytes
import com.ejrp.midi.data.META_EVENT_STATUS_BYTE
import com.ejrp.midi.data.MetaEventsType
import com.ejrp.midi.data.MetaEventsType.*
import com.ejrp.midi.data.MidiKeySignature
import com.ejrp.midi.utils.*
import java.io.InputStream
import java.io.OutputStream

abstract class MetaEvent(deltaTime: VariableLengthQuantity, val type: Byte, val length: VariableLengthQuantity) :
    MidiTrackEvent(deltaTime) {
    val status = META_EVENT_STATUS_BYTE.toByte()

    // delta-time + status + type + length + length of data
    override fun totalSize() = super.totalSize() + 1u + 1u + length.length().toUInt() + length.quantity

    override fun toOutputStream(stream: OutputStream): OutputStream {
        this.deltaTime.toOutputStream(stream).write(status).write(type)
        return length.toOutputStream(stream)
    }

    companion object {
        internal fun fromInputStream(stream: InputStream, deltaTime: VariableLengthQuantity): MetaEvent {
            // The status byte is guaranteed to be 0xFF

            val type = stream.read()
            if (type == -1) throw MissingBytes("meta event", 1, 0)

            val length = VariableLengthQuantity.fromInputStream(stream)
            val data = stream.readNBytes(length.quantity.toInt())
            if (data.size != length.quantity.toInt())
                throw MissingBytes("meta event's data", length.quantity.toInt(), data.size)

            return when (type) {
                SEQUENCE_NUMBER.type -> SequenceNumber.fromByteArray(deltaTime, data)
                TEXT_EVENT.type -> TextEvent.fromByteArray(deltaTime, data)
                COPYRIGHT_NOTICE.type -> CopyrightNotice.fromByteArray(deltaTime, data)
                TRACK_NAME.type -> TrackName.fromByteArray(deltaTime, data)
                INSTRUMENT_NAME.type -> InstrumentName.fromByteArray(deltaTime, data)
                LYRIC.type -> Lyric.fromByteArray(deltaTime, data)
                MARKER.type -> Marker.fromByteArray(deltaTime, data)
                CUE_POINT.type -> CuePoint.fromByteArray(deltaTime, data)
                MIDI_CHANNEL_PREFIX.type -> MidiChannelPrefix.fromByteArray(deltaTime, data)
                END_OF_TRACK.type -> EndOfTrack.fromByteArray(deltaTime, length)
                SET_TEMPO.type -> SetTempo.fromByteArray(deltaTime, data)
                SMPTE_OFFSET.type -> SMPTEOffset.fromByteArray(deltaTime, data)
                TIME_SIGNATURE.type -> TimeSignature.fromByteArray(deltaTime, data)
                KEY_SIGNATURE.type -> KeySignature.fromByteArray(deltaTime, data)
                SEQUENCER_SPECIFIC.type -> SequencerSpecific.fromByteArray(deltaTime, data)
                else -> UnknownMetaEvent.fromByteArray(deltaTime, type.toByte(), data)
            }
        }
    }
}

class SequenceNumber(deltaTime: VariableLengthQuantity, val sequenceNumber: UShort) :
    MetaEvent(deltaTime, SEQUENCE_NUMBER.type.toByte(), VariableLengthQuantity(0x02u)) {

    override fun toOutputStream(stream: OutputStream): OutputStream {
        super.toOutputStream(stream).write(sequenceNumber.toByteArray())
        return stream
    }

    override fun toString(): String {
        return "SequenceNumber(deltaTime=${this@SequenceNumber.deltaTime}, status=${status.toHexString()}, type=${type.toHexString()}, sequenceNumber=$sequenceNumber)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SequenceNumber) return false
        if (!super.equals(other)) return false

        if (sequenceNumber != other.sequenceNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + sequenceNumber.hashCode()
        return result
    }

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray) =
            SequenceNumber(deltaTime, data.readU16FromBytes())
    }

}

abstract class MetaTextEvent(
    deltaTime: VariableLengthQuantity,
    val eventType: MetaEventsType,
    val text: String
) : MetaEvent(deltaTime, eventType.type.toByte(), VariableLengthQuantity(text.length.toUInt())) {

    override fun toOutputStream(stream: OutputStream): OutputStream {
        super.toOutputStream(stream).write(text.toByteArray(Charsets.US_ASCII))
        return stream
    }

    override fun toString(): String {
        return "${eventType.toClassName()}(deltaTime=$deltaTime, status=${status.toHexString()}, type=${type.toHexString()}, text=$text)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetaTextEvent) return false
        if (!super.equals(other)) return false

        if (eventType != other.eventType) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + eventType.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }

}

class TextEvent(deltaTime: VariableLengthQuantity, text: String) :
    MetaTextEvent(deltaTime, TEXT_EVENT, text) {

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray) =
            TextEvent(deltaTime, data.toString(Charsets.US_ASCII))
    }
}

class CopyrightNotice(deltaTime: VariableLengthQuantity, text: String) :
    MetaTextEvent(deltaTime, COPYRIGHT_NOTICE, text) {

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray) =
            CopyrightNotice(deltaTime, data.toString(Charsets.US_ASCII))
    }
}

class TrackName(deltaTime: VariableLengthQuantity, text: String) :
    MetaTextEvent(deltaTime, TRACK_NAME, text) {

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray) =
            TrackName(deltaTime, data.toString(Charsets.US_ASCII))
    }
}

class InstrumentName(deltaTime: VariableLengthQuantity, text: String) :
    MetaTextEvent(deltaTime, INSTRUMENT_NAME, text) {

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray) =
            InstrumentName(deltaTime, data.toString(Charsets.US_ASCII))
    }
}

class Lyric(deltaTime: VariableLengthQuantity, text: String) :
    MetaTextEvent(deltaTime, LYRIC, text) {

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray) =
            Lyric(deltaTime, data.toString(Charsets.US_ASCII))
    }
}

class Marker(deltaTime: VariableLengthQuantity, text: String) :
    MetaTextEvent(deltaTime, MARKER, text) {

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray) =
            Marker(deltaTime, data.toString(Charsets.US_ASCII))

    }
}

class CuePoint(deltaTime: VariableLengthQuantity, text: String) :
    MetaTextEvent(deltaTime, CUE_POINT, text) {

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray) =
            CuePoint(deltaTime, data.toString(Charsets.US_ASCII))
    }
}

class MidiChannelPrefix(deltaTime: VariableLengthQuantity, val channel: Byte) :
    MetaEvent(deltaTime, MIDI_CHANNEL_PREFIX.type.toByte(), VariableLengthQuantity(1u)) {
    override fun toOutputStream(stream: OutputStream) = super.toOutputStream(stream).write(channel)

    override fun toString(): String {
        return "MidiChannelPrefix(deltaTime=$deltaTime, status=${status.toHexString()}, type=${type.toHexString()}, channel=$channel)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MidiChannelPrefix) return false
        if (!super.equals(other)) return false

        if (channel != other.channel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + channel
        return result
    }

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray): MidiChannelPrefix {
            if (data.size != 1) throw InvalidMetaEventLength("midi channel prefix", 1, data.size)
            return MidiChannelPrefix(deltaTime, data[0])
        }
    }
}

class EndOfTrack(deltaTime: VariableLengthQuantity) :
    MetaEvent(deltaTime, END_OF_TRACK.type.toByte(), VariableLengthQuantity(0u)) {
    // We don't have to override the totalSize and toOutputStream functions because the length of the event is 0

    override fun toString(): String {
        return "EndOfTrackEvent(deltaTime=$deltaTime, status=${status.toHexString()}, type=${type.toHexString()}, length=$length)"
    }

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, length: VariableLengthQuantity): EndOfTrack {
            if (length.quantity != 0u) throw InvalidMetaEventLength("end of track", 0, length.quantity.toInt())
            return EndOfTrack(deltaTime)
        }

    }
}

class SetTempo(deltaTime: VariableLengthQuantity, val newTempo: UInt) :
    MetaEvent(deltaTime, SET_TEMPO.type.toByte(), VariableLengthQuantity(3u)) {

    override fun toOutputStream(stream: OutputStream): OutputStream {
        val data = newTempo.toByteArray()
        // Here newTempo is a 24 bit value so we cannot write the entire array to the stream
        return super.toOutputStream(stream).write(data[1]).write(data[2]).write(data[3])
    }

    override fun toString(): String {
        return "SetTempoEvent(deltaTime=$deltaTime, status=${status.toHexString()}, type=${type.toHexString()}, newTempo=$newTempo)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SetTempo) return false
        if (!super.equals(other)) return false

        if (newTempo != other.newTempo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + newTempo.hashCode()
        return result
    }

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray): SetTempo {
            if (data.size != 3) throw InvalidMetaEventLength("set tempo", 3, data.size)
            val tempo = byteArrayOf(0).plus(data).readU32FromBytes()
            return SetTempo(deltaTime, tempo)
        }
    }
}

class SMPTEOffset(
    deltaTime: VariableLengthQuantity,
    val hours: Byte,
    val minutes: Byte,
    val seconds: Byte,
    val frames: Byte,
    val fractionalFrames: Byte
) :
    MetaEvent(deltaTime, SMPTE_OFFSET.type.toByte(), VariableLengthQuantity(5u)) {

    override fun toOutputStream(stream: OutputStream) =
        super.toOutputStream(stream)
            .write(hours)
            .write(minutes)
            .write(seconds)
            .write(frames)
            .write(fractionalFrames)

    override fun toString(): String {
        return "SMPTEOffset(deltaTime=$deltaTime, status=${status.toHexString()}, type=${type.toHexString()}, " +
                "hours=$hours, minutes=$minutes, seconds=$seconds, frames=$frames, fractionalFrames: $fractionalFrames)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SMPTEOffset) return false
        if (!super.equals(other)) return false

        if (hours != other.hours) return false
        if (minutes != other.minutes) return false
        if (seconds != other.seconds) return false
        if (frames != other.frames) return false
        if (fractionalFrames != other.fractionalFrames) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + hours
        result = 31 * result + minutes
        result = 31 * result + seconds
        result = 31 * result + frames
        result = 31 * result + fractionalFrames
        return result
    }

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray): SMPTEOffset {
            if (data.size != 5) throw InvalidMetaEventLength("SMPTE offset", 5, data.size)
            return SMPTEOffset(deltaTime, data[0], data[1], data[2], data[3], data[4])
        }
    }
}

class TimeSignature(
    deltaTime: VariableLengthQuantity,
    val numerator: Byte,
    val denominator: Byte,
    val midiClockPerMetronomeClick: Byte,
    val numberOf32ndNotesInMidiQuarterNote: Byte
) :
    MetaEvent(deltaTime, TIME_SIGNATURE.type.toByte(), VariableLengthQuantity(4u)) {

    override fun toOutputStream(stream: OutputStream) =
        super.toOutputStream(stream)
            .write(numerator)
            .write(denominator)
            .write(midiClockPerMetronomeClick)
            .write(numberOf32ndNotesInMidiQuarterNote)

    override fun toString(): String {
        return "TimeSignature(deltaTime=$deltaTime, status=${status.toHexString()}, type=${type.toHexString()}, " +
                "numerator=$numerator, denominator=$denominator, midiClockPerMetronomeClick=$midiClockPerMetronomeClick, " +
                "numberOf32ndNotesInMidiQuarterNote=$numberOf32ndNotesInMidiQuarterNote)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeSignature) return false
        if (!super.equals(other)) return false

        if (numerator != other.numerator) return false
        if (denominator != other.denominator) return false
        if (midiClockPerMetronomeClick != other.midiClockPerMetronomeClick) return false
        if (numberOf32ndNotesInMidiQuarterNote != other.numberOf32ndNotesInMidiQuarterNote) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + numerator
        result = 31 * result + denominator
        result = 31 * result + midiClockPerMetronomeClick
        result = 31 * result + numberOf32ndNotesInMidiQuarterNote
        return result
    }

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray): TimeSignature {
            if (data.size != 4) throw InvalidMetaEventLength("time signature", 4, data.size)
            return TimeSignature(deltaTime, data[0], data[1], data[2], data[3])
        }
    }
}

class KeySignature(
    deltaTime: VariableLengthQuantity,
    val keySignature: MidiKeySignature,
    val mode: Byte
) :
    MetaEvent(deltaTime, KEY_SIGNATURE.type.toByte(), VariableLengthQuantity(2u)) {

    override fun toOutputStream(stream: OutputStream) =
        super.toOutputStream(stream)
            .write(keySignature.data)
            .write(mode)

    override fun toString(): String {
        return "KeySignature(deltaTime=$deltaTime, status=${status.toHexString()}, type=${type.toHexString()}, " +
                "keySignature=$keySignature, mode=$mode)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeySignature) return false
        if (!super.equals(other)) return false

        if (mode != other.mode) return false
        if (keySignature != other.keySignature) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + mode
        result = 31 * result + keySignature.hashCode()
        return result
    }

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray): KeySignature {
            if (data.size != 2) throw InvalidMetaEventLength("key signature", 2, data.size)
            return KeySignature(deltaTime, MidiKeySignature(data[0]), data[1])
        }
    }
}

class SequencerSpecific(deltaTime: VariableLengthQuantity, val data: ByteArray) :
    MetaEvent(deltaTime, SEQUENCER_SPECIFIC.type.toByte(), VariableLengthQuantity(data.size.toUInt())) {

    override fun toOutputStream(stream: OutputStream): OutputStream {
        super.toOutputStream(stream).write(data)
        return stream
    }

    override fun toString(): String {
        return "SequencerSpecific(deltaTime=$deltaTime, status=${status.toHexString()}, type=${type.toHexString()}, data=${data.toHexStringList()})"
    }

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, data: ByteArray) = SequencerSpecific(deltaTime, data)
    }
}

class UnknownMetaEvent(deltaTime: VariableLengthQuantity, type: Byte, val data: ByteArray) :
    MetaEvent(deltaTime, type, VariableLengthQuantity(data.size.toUInt())) {

    override fun toOutputStream(stream: OutputStream): OutputStream {
        super.toOutputStream(stream).write(data)
        return stream
    }

    override fun toString(): String {
        return "UnknownMetaEvent(deltaTime=$deltaTime, status=${status.toHexString()}, type=${type.toHexString()}, data=${data.toHexStringList()})"
    }

    companion object {
        internal fun fromByteArray(deltaTime: VariableLengthQuantity, type: Byte, data: ByteArray) =
            UnknownMetaEvent(deltaTime, type, data)
    }
}
