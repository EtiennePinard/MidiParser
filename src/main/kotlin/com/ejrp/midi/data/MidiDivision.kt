package com.ejrp.midi.data

import com.ejrp.midi.InvalidSMPTEFramesPerSecond
import com.ejrp.midi.MissingBytes
import com.ejrp.midi.utils.*
import java.io.InputStream
import java.io.OutputStream
import kotlin.jvm.Throws

/**
 * This interface is mainly used for the fromInputStream from its companion object and assuring that
 * every midi division implements the Serialize interface.
 */
interface MidiDivision : Serialize {

    companion object : DeserializeStream<MidiDivision> {
        @Throws(MissingBytes::class)
        override fun fromInputStream(serialized: InputStream): MidiDivision {
            val data = serialized.readNBytes(2)
            if (data.size != 2) throw MissingBytes("division", 2, data.size)

            return if (data.readU16FromBytes().getBit(15)) SMPTEDivision.fromByteArray(data)
            else TicksPerQuarterNoteDivision.fromByteArray(data)
        }
    }
}

/**
 * The ticks per quarter note division tells how many midi ticks are in a quarter note.
 * More ticks means more precision for midi events. A standard value for this is 96 (0x60).
 *
 * @property tickPerQuarterNote The number of midi ticks per quarter note
 * @constructor Create a ticks per quarter note division with the specified ticks per quarter note
 */
data class TicksPerQuarterNoteDivision(val tickPerQuarterNote: UShort) : MidiDivision {
    override fun toOutputStream(stream: OutputStream): OutputStream {
        stream.write(tickPerQuarterNote.toByteArray())
        return stream
    }

    companion object {
        // The size of serialized should be guaranteed to be 2
        internal fun fromByteArray(serialized: ByteArray) = TicksPerQuarterNoteDivision(serialized.readU16FromBytes())
    }
}

/**
 * The SMPTE division is rarer to see in midi files. It specifies the number of frames per second
 * and the number of sub frames per frame.
 *
 * @property framesPerSecond The number of frames per second
 * @property subFramesPerFrame The number of sub frame per frame
 * @constructor Create a SMPTE division with the specified frames per second and sub frames per frame
 */
data class SMPTEDivision(val framesPerSecond: Byte, val subFramesPerFrame: Byte) : MidiDivision {

    override fun toOutputStream(stream: OutputStream) = stream.write(framesPerSecond).write(subFramesPerFrame)

    companion object {
        // The size of serialized should be guaranteed to be 2
        internal fun fromByteArray(serialized: ByteArray): SMPTEDivision {
            val smpteFormat = serialized[0]
            if (smpteFormat.toInt() != -24 && smpteFormat.toInt() != -25 && smpteFormat.toInt() != -29 &&
                smpteFormat.toInt() != -30
            ) throw InvalidSMPTEFramesPerSecond(smpteFormat)

            val ticksPerFrame = serialized[1]
            return SMPTEDivision(smpteFormat, ticksPerFrame)
        }

    }
}
