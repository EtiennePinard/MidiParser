package com.ejrp.midi

import com.ejrp.midi.data.HeaderChunk
import com.ejrp.midi.data.MidiSequence
import com.ejrp.midi.utils.DeserializeStream
import com.ejrp.midi.utils.Serialize
import java.io.InputStream
import java.io.OutputStream

/**
 * A class that represents a standard midi file
 * @property headerChunk The header chunk of this midi file
 * @property midiSequence Data class that contains all the track chunks of the midi file
 * @constructor Create a Midi file with the provided header chunk and midi sequence
 * @see <a href="https://midi.org/standard-midi-files-specification">https://midi.org/standard-midi-files-specification</a>
 */
data class StandardMidiFile(val headerChunk: HeaderChunk, val midiSequence: MidiSequence) : Serialize {

    /**
     * Returns the expanded string representation of this standard midi file.
     * This string representation includes more detailed information of the header chunk
     * and all midi events in the file listed in order.
     *
     * @return The expanded string representation of this standard midi file
     */
    fun expandedStringRepresentation(): String {
        val builder = StringBuilder()
        builder.append("Maximum file size: ${maximumFileSize()} bytes\n")
        builder.append("Header chunk:\n")
        builder.append("\tType: ${headerChunk.type}\n")
        builder.append("\tLength: ${headerChunk.length}\n")
        builder.append("\tFile Format: ${headerChunk.format}\n")
        builder.append("\tNumber of tracks: ${headerChunk.numberOfTrackChunks}\n")
        builder.append("\tDivision: ${headerChunk.midiDivision}\n")
        builder.append("Sequence:\n")
        for (i in midiSequence.tracks.indices) {
            val midiTrackEvents = midiSequence.tracks[i]
            builder.append("\tTrack #${midiSequence.tracks.indexOf(midiTrackEvents)}:\n")
            midiTrackEvents.events.forEach { midiTrackEvent ->
                builder.append("\t\t$midiTrackEvent\n")
            }
        }
        return builder.toString()
    }

    /**
     * Computes the maximum size in bytes of the entire file.
     * This is the maximum size because there is no running status optimization used in this
     * size calculation
     *
     * @return The maximum size in bytes of the entire file
     */
    fun maximumFileSize() = headerChunk.totalChunkSize() + midiSequence.tracks.sumOf { it.totalChunkSize() }

    override fun toOutputStream(stream: OutputStream): OutputStream {
        headerChunk.toOutputStream(stream)
        return midiSequence.toOutputStream(stream)
    }

    companion object : DeserializeStream<StandardMidiFile> {
        override fun fromInputStream(serialized: InputStream): StandardMidiFile {
            val headerChunk = HeaderChunk.fromInputStream(serialized)
            val midiSequence = MidiSequence.fromInputStream(serialized, headerChunk.numberOfTrackChunks.toInt())
            return StandardMidiFile(headerChunk, midiSequence)
        }
    }
}
