package com.ejrp.midi.data

import com.ejrp.midi.*
import com.ejrp.midi.events.*
import com.ejrp.midi.utils.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

open class MidiChunk(val type: UInt, val length: UInt) : Serialize {

    fun totalChunkSize() = CHUNK_METADATA_LENGTH + length

    override fun toOutputStream(stream: OutputStream): OutputStream {
        // Writing the type and the length
        stream.write(type.toByteArray())
        stream.write(length.toByteArray())
        return stream
    }

    override fun toString(): String {
        return "MidiChunk(type=${type.toByteArray().toString(Charsets.US_ASCII)}, length=$length)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MidiChunk) return false

        if (type != other.type) return false
        if (length != other.length) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + length.hashCode()
        return result
    }

    companion object : DeserializeStream<MidiChunk> {
        @Throws(MissingBytes::class)
        override fun fromInputStream(serialized: InputStream): MidiChunk {
            val data = serialized.readNBytes(8)
            // special case for empty files
            if (data.size == 0) throw EmptyFile()

            if (data.size != CHUNK_METADATA_LENGTH.toInt()) throw MissingBytes("midi chunk", 8, data.size)

            return MidiChunk(data.readU32FromBytes(), data.readU32FromBytes(4))
        }

    }
}

data class HeaderChunk(val format: UShort, val numberOfTrackChunks: UShort, val midiDivision: MidiDivision) :
    MidiChunk(MThd_MAGIC, HEADER_CHUNK_LENGTH) {

    override fun toOutputStream(stream: OutputStream): OutputStream {
        super.toOutputStream(stream) // Writing the type and the length
        stream.write(format.toByteArray())
        stream.write(numberOfTrackChunks.toByteArray())
        return midiDivision.toOutputStream(stream)
    }

    override fun toString(): String {
        return "HeaderChunk(type=${type.toByteArray().toString(Charsets.US_ASCII)}, length=$length, " +
                "format=$format, numberOfTrackChunks=$numberOfTrackChunks, division=$midiDivision)"
    }

    companion object : DeserializeStream<HeaderChunk> {
        @Throws(
            InvalidChunkType::class,
            MissingBytes::class,
            InvalidMidiFileFormat::class,
            InvalidNumberOfTracksForFormat0File::class
        )
        override fun fromInputStream(serialized: InputStream): HeaderChunk {
            val midiChunk = MidiChunk.fromInputStream(serialized)
            // The stream is now advanced to the data of this midi chunk
            if (midiChunk.type != MThd_MAGIC) throw InvalidChunkType("header", MThd_MAGIC, midiChunk.type)

            if (midiChunk.length != HEADER_CHUNK_LENGTH) throw InvalidHeaderChunkLength(midiChunk.length)


            val format = serialized.readNBytes(2)
            if (format.size != 2) throw MissingBytes("midi file format", 2, format.size)

            val fileFormat = format.readU16FromBytes()
            if (fileFormat != 0u.toUShort() && fileFormat != 1u.toUShort() && fileFormat != 2u.toUShort())
                throw InvalidMidiFileFormat(fileFormat)

            val numTracks = serialized.readNBytes(2)
            if (numTracks.size != 2) throw MissingBytes("number for the amount of track chunks", 2, numTracks.size)

            val numberOfTracks = numTracks.readU16FromBytes()
            if (fileFormat == 0u.toUShort() && numberOfTracks != 1u.toUShort())
                throw InvalidNumberOfTracksForFormat0File(numberOfTracks)

            return HeaderChunk(
                fileFormat,
                numTracks.readU16FromBytes(),
                MidiDivision.fromInputStream(serialized)
            )
        }
    }
}

data class TrackChunk(val events: List<MidiTrackEvent>) :
    MidiChunk(MTrk_MAGIC, events.sumOf { it.totalSize() }) {

    override fun toOutputStream(stream: OutputStream): OutputStream {
        stream.write(type.toByteArray()) // Writing the type
        // Calculating the length
        var arrayDataLength = totalChunkSize() - CHUNK_METADATA_LENGTH
        val bytes = ByteArrayOutputStream()

        var runningStatus: Byte = 0
        for (event in events) {
            when (event) {
                is SystemExclusiveEvent, is MetaEvent -> event.toOutputStream(bytes)
                is ChannelMessage -> {
                    if (runningStatus != event.status) {
                        // running status is the same as the current event status, so we change the running status
                        runningStatus = event.status
                        event.toOutputStream(bytes, true)
                    } else {
                        arrayDataLength-- // We have to subtract one byte from the length
                        event.toOutputStream(bytes, false)
                    }
                }

                else -> {
                    throw IllegalStateException("The midi event $event is not a channel message, meta event or system exclusive event.")
                }
            }
        }

        stream.write(arrayDataLength.toByteArray()) // Writing the size of track chunk
        stream.write(bytes.toByteArray())
        return stream
    }

    override fun toString(): String {
        return "TrackChunk(type=${type.toByteArray().toString(Charsets.US_ASCII)}, length=$length, events=$events)"
    }

    companion object : DeserializeStream<TrackChunk> {
        @Throws(MissingBytes::class, InvalidChunkType::class, MissingMidiChannelMessageStatus::class)
        override fun fromInputStream(serialized: InputStream): TrackChunk {
            val midiChunk = MidiChunk.fromInputStream(serialized)
            if (midiChunk.type != MTrk_MAGIC) throw InvalidChunkType("track", MTrk_MAGIC, midiChunk.type)

            val events = ArrayList<MidiTrackEvent>()

            var runningStatus = 0u
            var bytesRead = 0u
            var subtract: UInt

            /*
            This is to handle the case that we want to transmit multiple sysex messages
            at different times. We need to check that we end the last message with the F7 byte and that they are not
            other types of messages in between the sysex messages. Here's an example of this from the smf specs:

                F0 03 43 12 00                  Sysex starts with F0
                81 48                           200-tick delta-time
                F7 06 43 12 00 43 12 00         Subsequent sysex start with F7
                64                              100-tick delta-time
                F7 04 43 12 00 F7               The last sysex end with F7
            */
            // TODO: Add the check with this sysex event
            var startedSysexWithF0 = false
            var correctlyEndedTheSysexEvent = true

            while (bytesRead < midiChunk.length) {
                // We assume that the first byte is a variable length quantity
                val deltaTime = VariableLengthQuantity.fromInputStream(serialized)

                // Checking running status
                val nextByte = serialized.read()
                if (nextByte == -1) throw MissingBytes("midi track event", 1, 0)

                if ((nextByte shr 4) == 0xF && nextByte.toUInt() != SystemExclusiveFormat.F0_FORMAT.status
                    && nextByte.toUInt() != SystemExclusiveFormat.F7_FORMAT.status
                    && nextByte.toUInt() != META_EVENT_STATUS_BYTE
                ) {
                    throw IllegalMessageStatusByte(nextByte.toByte())
                }

                val event: MidiTrackEvent = when (nextByte.toUInt()) {
                    // If this is a meta event or a sysex event, don't use running status and don't reset it to 0
                    META_EVENT_STATUS_BYTE -> {
                        if (!correctlyEndedTheSysexEvent) {
                            throw SystemExclusiveFormatF0IncorrectEnding()
                        }
                        startedSysexWithF0 = false

                        subtract = 0u
                        MetaEvent.fromInputStream(
                            serialized,
                            deltaTime
                        )
                    }

                    SystemExclusiveFormat.F0_FORMAT.status -> {
                        subtract = 0u

                        if (startedSysexWithF0 && !correctlyEndedTheSysexEvent) throw MultiPacketedSystemExclusiveMessageError()

                        startedSysexWithF0 = true

                        val sysex = SystemExclusiveEvent.fromInputStream(
                            serialized,
                            deltaTime,
                            nextByte.toUInt()
                        )

                        correctlyEndedTheSysexEvent = sysex.data.last() == 0xF7.toByte()

                        sysex
                    }

                    SystemExclusiveFormat.F7_FORMAT.status -> {
                        subtract = 0u
                        val sysex = SystemExclusiveEvent.fromInputStream(
                            serialized,
                            deltaTime,
                            nextByte.toUInt()
                        )

                        if (startedSysexWithF0 && sysex.data.last() == 0xF7.toByte()) {
                            correctlyEndedTheSysexEvent = true
                        }

                        sysex
                    }

                    else -> {
                        if (!correctlyEndedTheSysexEvent) throw SystemExclusiveFormatF0IncorrectEnding()

                        startedSysexWithF0 = false

                        var firstByte = nextByte
                        if (nextByte.toUByte().getBit(7)) {
                            // bit 7 is set, therefore this is a status byte, and we need to update the running status
                            runningStatus = nextByte.toUInt()
                            firstByte = serialized.read()
                            subtract = 0u
                            if (firstByte == -1) throw MissingBytes("midi track event", 1, 0)

                        } else {
                            subtract = 1u
                        }

                        if (!runningStatus.getBit(7)) throw MissingMidiChannelMessageStatus()

                        ChannelMessage.fromStreamPartiallyDeserialized(
                            serialized,
                            deltaTime,
                            runningStatus,
                            firstByte.toByte()
                        )
                    }
                }
                bytesRead += event.totalSize() - subtract
                events.add(event)
            }

            // It is not possible that bytesRead is smaller than midiChunk.length\
            // This means that in our case, bytesRead > midiChunk.length is the same thing as bytesRead != midiChunk.length
            if (bytesRead > midiChunk.length) throw TrackChunkLengthTooSmall(midiChunk.length, bytesRead)

            if (events.isEmpty()) throw EmptyMidiTrackEvents()

            if (events.last() !is EndOfTrack) throw InvalidFinalEventInTrack(events.last()::class.simpleName)

            return TrackChunk(events)
        }
    }
}

interface DeserializeMidiSequence {
    fun fromInputStream(serialized: InputStream, numberOfTracks: Int): MidiSequence
}

data class MidiSequence(val tracks: List<TrackChunk>) : Serialize {
    override fun toOutputStream(stream: OutputStream): OutputStream {
        tracks.forEach { it.toOutputStream(stream) }
        return stream
    }

    companion object : DeserializeMidiSequence {
        override fun fromInputStream(serialized: InputStream, numberOfTracks: Int): MidiSequence {
            val tracks = ArrayList<TrackChunk>(numberOfTracks)
            repeat(numberOfTracks) {
                tracks.add(TrackChunk.fromInputStream(serialized))
            }
            return MidiSequence(tracks)
        }
    }
}
