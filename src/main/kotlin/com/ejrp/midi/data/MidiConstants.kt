package com.ejrp.midi.data

import com.ejrp.midi.InvalidMidiDataByte
import com.ejrp.midi.utils.getBit
import com.ejrp.midi.utils.shl

data class MidiDataByte(val data: Byte) {
    init {
        if (data.getBit(7)) throw InvalidMidiDataByte("midi data byte", data)
    }

    override fun toString() = "$data"
}

data class MidiDataShort(val leastSignificantBits: MidiDataByte, val mostSignificantBits: MidiDataByte) {

    val value = (mostSignificantBits.data.toUShort() shl 7) or mostSignificantBits.data.toUShort()

    constructor(leastSignificantBits: Byte, mostSignificantBits: Byte) : this(
        MidiDataByte(leastSignificantBits),
        MidiDataByte(mostSignificantBits)
    )
}

typealias Pressure = MidiDataByte
typealias ControllerNumber = MidiDataByte
typealias ProgramNumber = MidiDataByte

/**
 * Represents a midi key, which is a positive byte (7 bit number).
 *
 * @property key The value associated to this Midi key
 * @constructor Create a Midi key with its associated byte value
 *
 * @see <a href=https://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html#BM0_>Appendix 1.3 of https://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html</a>
 */
enum class MidiKey(val key: Byte) {
    CMinus1(0), CSharpMinus1(1), DMinus1(2), DSharpMinus1(3), EMinus1(4), FMinus1(5), FSharpMinus1(6), GMinus1(7), GSharpMinus1(8), AMinus1(9), ASharpMinus1(10), BMinus1(11),
    C0(12), CSharp0(13), D0(14), DSharp0(15), E0(16), F0(17), FSharp0(18), G0(19), GSharp0(20), A0(21), ASharp0(22), B0(23),
    C1(24), CSharp1(25), D1(26), DSharp1(27), E1(28), F1(29), FSharp1(30), G1(31), GSharp1(32), A1(33), ASharp1(34), B1(35),
    C2(36), CSharp2(37), D2(38), DSharp2(39), E2(40), F2(41), FSharp2(42), G2(43), GSharp2(44), A2(45), ASharp2(46), B2(47),
    C3(48), CSharp3(49), D3(50), DSharp3(51), E3(52), F3(53), FSharp3(54), G3(55), GSharp3(56), A3(57), ASharp3(58), B3(59),
    C4(60), CSharp4(61), D4(62), DSharp4(63), E4(64), F4(65), FSharp4(66), G4(67), GSharp4(68), A4(69), ASharp4(70), B4(71),
    C5(72), CSharp5(73), D5(74), DSharp5(75), E5(76), F5(77), FSharp5(78), G5(79), GSharp5(80), A5(81), ASharp5(82), B5(83),
    C6(84), CSharp6(85), D6(86), DSharp6(87), E6(88), F6(89), FSharp6(90), G6(91), GSharp6(92), A6(93), ASharp6(94), B6(95),
    C7(96), CSharp7(97), D7(98), DSharp7(99), E7(100), F7(101), FSharp7(102), G7(103), GSharp7(104), A7(105), ASharp7(106), B7(107),
    C8(108), CSharp8(109), D8(110), DSharp8(111), E8(112), F8(113), FSharp8(114), G8(115), GSharp8(116), A8(117), ASharp8(118), B8(119),
    C9(120), CSharp9(121), D9(122), DSharp9(123), E9(124), F9(125), FSharp9(126), G9(127);

    override fun toString(): String = name.replace("Sharp", "#")

    companion object {
        fun fromKey(value: Byte): MidiKey {
            if (value.getBit(7)) throw InvalidMidiDataByte("midi key", value)
            return entries.first { it.key == value }
        }
    }
}

/**
 * Represents a midi velocity, which is a positive byte (7 bit number).
 * The constants in the companion objects are subjective and are just to be used as a baseline
 *
 * @property velocity The velocity associated with the dynamic marking
 * @constructor Create a Midi velocity
 *
 * @see <a href=https://en.wikipedia.org/wiki/Dynamics_(music)>https://en.wikipedia.org/wiki/Dynamics_(music)</a>
 */
data class MidiVelocity(val velocity: Byte) {

    init {
        if (velocity.getBit(7)) throw InvalidMidiDataByte("midi velocity", velocity)
    }

    companion object {
        val Niente = MidiVelocity(0) // italian for nothing
        val Pianississimo = MidiVelocity(16)
        val Pianissimo = MidiVelocity(32)
        val Piano = MidiVelocity(48)
        val MezzoPiano = MidiVelocity(64)
        val MezzoForte = MidiVelocity(80)
        val Forte = MidiVelocity(96)
        val Fortissimo = MidiVelocity(112)
        val Fortississimo = MidiVelocity(127)
    }
}

data class MidiKeySignature(val data: Byte) {
    companion object {
        val SEVEN_FLATS = MidiKeySignature(-7)
        val SIX_FLATS = MidiKeySignature(-6)
        val FIVE_FLATS = MidiKeySignature(-5)
        val FOUR_FLATS = MidiKeySignature(-4)
        val THREE_FLATS = MidiKeySignature(-3)
        val TWO_FLATS = MidiKeySignature(-2)
        val ONE_FLAT = MidiKeySignature(-1)
        val NATURAL = MidiKeySignature(0)
        val ONE_SHARP = MidiKeySignature(1)
        val TWO_SHARPS = MidiKeySignature(2)
        val THREE_SHARPS = MidiKeySignature(3)
        val FOUR_SHARPS = MidiKeySignature(4)
        val FIVE_SHARPS = MidiKeySignature(5)
        val SIX_SHARPS = MidiKeySignature(6)
        val SEVEN_SHARPS = MidiKeySignature(7)
    }
}

/**
 * Is the ASCII word 'MThd' in a 32-bit value.
 * This is the ASCII type of the header chunk.
 */
const val MThd_MAGIC = 0x4D546864u // 'MThd'

/**
 * Is the ASCII word 'MTrk' in a 32-bit value.
 * This is the ASCII type of a track chunk.
 */
const val MTrk_MAGIC = 0x4D54726Bu // 'MTrk'

/**
 * A midi chunk always start with two 32-bit values in big endian.
 * The first four bytes is the type of the chunk, like 'MThd' for the header chunk.
 * The last four bytes is length in bytes of the data in the chunk.
 * For example, the header chunk has a fixed total length of 14 bytes.
 * The first 8 bytes for be 'MThd', the chunk type, followed by the chunk
 * length, which would be total length minus metadata length, so 14-8 = 6.
 * 6 would need to be written in a 32-bit big endian number so it would be 00 00 00 06.
 *
 * Therefore the first 8 bytes of the header chunk is, in hex: 4D 54 68 64 00 00 00 06
 */
const val CHUNK_METADATA_LENGTH = 8u

/**
 * The header chunk has a fixed data length of 6 bytes. These 6 bytes are seperated
 * into three 16-bit values. The first 16-bit value is the type of the midi file.
 * The second 16-bit value is the number of tracks in the midi file.
 * The last 16-bit value is the division, which holds information about the timing
 * of the midi events.
 */
const val HEADER_CHUNK_LENGTH = 6u

/**
 * The byte which indicates a meta-event
 */
const val META_EVENT_STATUS_BYTE = 0xFFu

/**
 * The status bits of the seven midi channel messages
 */
enum class MidiChannelMessageStatus(val status: UInt) {
    NOTE_OFF(0b1000u),
    NOTE_ON(0b1001u),
    POLYPHONIC_KEY_PRESSURE(0b1010u),
    CONTROL_CHANGE(0b1011u),
    PROGRAM_CHANGE(0b1100u),
    CHANNEL_PRESSURE(0b1101u),
    PITCH_WHEEL_CHANGE(0b1110u)
}

/**
 * The type byte of meta events
 */
enum class MetaEventsType(val type: Int) {
    SEQUENCE_NUMBER(0x00),
    TEXT_EVENT(0x01),
    COPYRIGHT_NOTICE(0x02),
    TRACK_NAME(0x03),
    INSTRUMENT_NAME(0x04),
    LYRIC(0x04),
    MARKER(0x05),
    CUE_POINT(0x07),
    MIDI_CHANNEL_PREFIX(0x20),
    END_OF_TRACK(0x2F),
    SET_TEMPO(0x51),
    SMPTE_OFFSET(0x54),
    TIME_SIGNATURE(0x58),
    KEY_SIGNATURE(0x59),
    SEQUENCER_SPECIFIC(0x7F);

    fun toClassName() = name.substringBefore("_").cap() + name.substringAfter("_", "").cap()
}

fun String.cap(): String {
    if (this.isBlank()) return this
    return first().uppercase() + substring(1).lowercase()
}