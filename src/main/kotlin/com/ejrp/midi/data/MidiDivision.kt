package com.ejrp.midi.data

import com.ejrp.midi.InvalidSMPTEFramesPerSecond
import com.ejrp.midi.MissingBytes
import com.ejrp.midi.utils.*
import java.io.InputStream
import java.io.OutputStream
import kotlin.jvm.Throws

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
