package com.ejrp.midi

import com.ejrp.midi.data.*
import com.ejrp.midi.events.*
import com.ejrp.midi.utils.VariableLengthQuantity
import com.ejrp.midi.utils.toByteArray

private fun format0MidiSequence(events: List<MidiTrackEvent>) = MidiSequence(listOf(TrackChunk(events)))

private val basicFormat0HeaderChunkBytes = MThd_MAGIC.toByteArray() // chunk type
    .plus(HEADER_CHUNK_LENGTH.toByteArray()) // chunk length
    .plus(0u.toUShort().toByteArray()) // format
    .plus(1u.toUShort().toByteArray()) // number of tracks
    .plus(96u.toUShort().toByteArray()) // ticks per quarter note division
private val basicFormat0HeaderChunk = HeaderChunk(0u, 1u, TicksPerQuarterNoteDivision(96u))

data class ValidMidiTestFile(val bytes: ByteArray, val parsedMidiFile: StandardMidiFile, val testMessage: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValidMidiTestFile) return false

        if (!bytes.contentEquals(other.bytes)) return false
        if (parsedMidiFile != other.parsedMidiFile) return false
        if (testMessage != other.testMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + parsedMidiFile.hashCode()
        result = 31 * result + testMessage.hashCode()
        return result
    }

}

private val noteOnAndNoteOff = ValidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray()) // type
        .plus(byteArrayOf(0, 0, 0, 20)) // size
        .plus(
            byteArrayOf(
                0, // delta-time of 0
                (MidiChannelMessageStatus.NOTE_ON.status shl 4).toByte(),
                MidiKey.C4.key,
                MidiVelocity.Piano.velocity,

                96, // delta-time of 96
                (MidiChannelMessageStatus.NOTE_OFF.status shl 4).toByte(),
                MidiKey.C4.key,
                MidiVelocity.Niente.velocity,

                0, // delta-time of 0
                (MidiChannelMessageStatus.NOTE_ON.status shl 4).toByte(),
                MidiKey.CSharpMinus1.key,
                MidiVelocity.Piano.velocity,

                96, // delta-time of 96
                (MidiChannelMessageStatus.NOTE_OFF.status shl 4).toByte(),
                MidiKey.CSharpMinus1.key,
                MidiVelocity.Niente.velocity,

                0, // delta time of 0
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0 // end of track has length of 0
            )
        ),
    StandardMidiFile(
        basicFormat0HeaderChunk, format0MidiSequence(
            listOf(
                NoteOn(
                    VariableLengthQuantity(0u), 0, MidiKey.C4,
                    MidiVelocity.Piano
                ),
                NoteOff(
                    VariableLengthQuantity(96u), 0, MidiKey.C4,
                    MidiVelocity.Niente
                ),
                NoteOn(
                    VariableLengthQuantity(0u), 0, MidiKey.CSharpMinus1,
                    MidiVelocity.Piano
                ),
                NoteOff(
                    VariableLengthQuantity(96u), 0, MidiKey.CSharpMinus1,
                    MidiVelocity.Niente
                ),
                EndOfTrack(VariableLengthQuantity(0u))
            )
        )
    ),
    "Basic format 0 midi file with note on and note off event"
)

private val smpteDivision = ValidMidiTestFile(
    MThd_MAGIC.toByteArray() // chunk type
        .plus(HEADER_CHUNK_LENGTH.toByteArray()) // chunk length
        .plus(0u.toUShort().toByteArray()) // format
        .plus(1u.toUShort().toByteArray()) // number of tracks
        .plus(byteArrayOf(-30, 40)) // SMPTE division
        .plus(MTrk_MAGIC.toByteArray()) // type
        .plus(byteArrayOf(0, 0, 0, 4)) // size
        .plus(
            byteArrayOf(
                0, // delta time of 0
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0 // end of track has length of 0
            )
        ),
    StandardMidiFile(
        HeaderChunk(0u, 1u, SMPTEDivision(-30, 40)),
        format0MidiSequence(listOf(EndOfTrack(VariableLengthQuantity(0u))))
    ),
    "SMPTE division"
)


private val basicRunningStatus = ValidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray()) // type
        .plus(byteArrayOf(0, 0, 0, 17)) // size
        .plus(
            byteArrayOf(
                0, // delta-time of 0
                (MidiChannelMessageStatus.NOTE_ON.status shl 4).toByte(),
                MidiKey.C4.key,
                MidiVelocity.Piano.velocity,

                96, // delta-time of 96
                MidiKey.C4.key,
                MidiVelocity.Niente.velocity,

                0, // delta-time of 0
                MidiKey.CSharpMinus1.key,
                MidiVelocity.Piano.velocity,

                96, // delta-time of 96
                MidiKey.CSharpMinus1.key,
                MidiVelocity.Niente.velocity,

                0, // delta time of 0
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0 // end of track has length of 0
            )
        ),
    StandardMidiFile(
        basicFormat0HeaderChunk, format0MidiSequence(
            listOf(
                NoteOn(
                    VariableLengthQuantity(0u), 0, MidiKey.C4,
                    MidiVelocity.Piano
                ),
                NoteOn(
                    VariableLengthQuantity(96u), 0, MidiKey.C4,
                    MidiVelocity.Niente
                ),
                NoteOn(
                    VariableLengthQuantity(0u), 0, MidiKey.CSharpMinus1,
                    MidiVelocity.Piano
                ),
                NoteOn(
                    VariableLengthQuantity(96u), 0, MidiKey.CSharpMinus1,
                    MidiVelocity.Niente
                ),
                EndOfTrack(VariableLengthQuantity(0u))
            )

        )
    ),
    "Note on running status"
)

private val runningStatusInterruptedByMetaAndSySexEvent = ValidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray()) // type
        .plus(byteArrayOf(0, 0, 0, 36)) // size
        .plus(
            byteArrayOf(
                0, // delta-time of 0
                (MidiChannelMessageStatus.NOTE_ON.status shl 4).toByte(),
                MidiKey.C4.key,
                MidiVelocity.Piano.velocity,

                96, // delta-time of 96
                MidiKey.C4.key,
                MidiVelocity.Niente.velocity,

                0, // delta-time of 0
                SystemExclusiveFormat.F0_FORMAT.byte,
                3, // Length
                0x35, // random data 0
                0x34, // random data 1
                0xF7.toByte(), // Required ending byte

                0, // delta-time of 0
                MidiKey.CSharpMinus1.key,
                MidiVelocity.Piano.velocity,

                96, // delta-time of 96
                MidiKey.CSharpMinus1.key,
                MidiVelocity.Niente.velocity,

                0, // delta time of 0
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.TEXT_EVENT.type.toByte(),
                3, // text event length
                'H'.code.toByte(),
                'i'.code.toByte(),
                '!'.code.toByte(),

                0, // delta-time of 0
                MidiKey.ASharp5.key,
                MidiVelocity.MezzoPiano.velocity,

                96, // delta-time of 96
                MidiKey.ASharp5.key,
                MidiVelocity.Niente.velocity,

                0, // delta-time of 0
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0 // end of track has length of 0
            )
        ),
    StandardMidiFile(
        basicFormat0HeaderChunk, format0MidiSequence(
            listOf(
                NoteOn(
                    VariableLengthQuantity(0u), 0, MidiKey.C4,
                    MidiVelocity.Piano
                ),
                NoteOn(
                    VariableLengthQuantity(96u), 0, MidiKey.C4,
                    MidiVelocity.Niente
                ),
                SystemExclusiveEvent(
                    VariableLengthQuantity(0u),
                    SystemExclusiveFormat.F0_FORMAT,
                    byteArrayOf(0x35, 0x34, 0xF7.toByte())
                ),
                NoteOn(
                    VariableLengthQuantity(0u), 0, MidiKey.CSharpMinus1,
                    MidiVelocity.Piano
                ),
                NoteOn(
                    VariableLengthQuantity(96u), 0, MidiKey.CSharpMinus1,
                    MidiVelocity.Niente
                ),
                TextEvent(
                    VariableLengthQuantity(0u),
                    "Hi!"
                ),
                NoteOn(
                    VariableLengthQuantity(0u), 0, MidiKey.ASharp5,
                    MidiVelocity.MezzoPiano
                ),
                NoteOn(
                    VariableLengthQuantity(96u), 0, MidiKey.ASharp5,
                    MidiVelocity.Niente
                ),
                EndOfTrack(VariableLengthQuantity(0u))
            )

        )
    ),
    "Note on running status interrupted by meta event and system exclusive event"
)

private val allChannelMessage = ValidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray()) // type
        .plus(byteArrayOf(0, 0, 0, 38)) // size
        .plus(
            byteArrayOf(
                0, // delta-time of 0
                ((MidiChannelMessageStatus.NOTE_OFF.status shl 4) or 0u).toByte(),
                MidiKey.A5.key,
                50,

                0xFF.toByte(),
                0xFF.toByte(),
                0xFF.toByte(),
                0x7F, // delta-time of 0x0FFFFFFF
                ((MidiChannelMessageStatus.NOTE_ON.status shl 4) or 1u).toByte(),
                MidiKey.A5.key,
                42,

                0xFF.toByte(),
                0xFF.toByte(),
                0x7F, // delta-time of 0x001FFFFF
                ((MidiChannelMessageStatus.POLYPHONIC_KEY_PRESSURE.status shl 4) or 2u).toByte(),
                MidiKey.A5.key,
                0x42, // Pressure value

                0x81.toByte(),
                0x00, // delta-time of 0x80
                ((MidiChannelMessageStatus.CONTROL_CHANGE.status shl 4) or 3u).toByte(),
                3, // controller value
                0x42, // new value

                0x81.toByte(),
                0x80.toByte(),
                0x00, // delta-time of 0x4000
                ((MidiChannelMessageStatus.PROGRAM_CHANGE.status shl 4) or 4u).toByte(),
                5, // program number

                0x39, // delta-time of 0x39
                ((MidiChannelMessageStatus.CHANNEL_PRESSURE.status shl 4) or 5u).toByte(),
                127, // pressure value

                0x23, // delta-time of 0x23
                ((MidiChannelMessageStatus.PITCH_WHEEL_CHANGE.status shl 4) or 6u).toByte(),
                0x7F, // least significant bits
                0x67, // most significant bits

                0, // delta-time of 0
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0 // end of track has length of 0
            )
        ),
    StandardMidiFile(
        basicFormat0HeaderChunk,
        format0MidiSequence(
            listOf(
                NoteOff(VariableLengthQuantity(0u), 0, MidiKey.A5, MidiVelocity(50)),
                NoteOn(VariableLengthQuantity(0x0FFFFFFFu), 1, MidiKey.A5, MidiVelocity(42)),
                PolyphonicKeyPressure(VariableLengthQuantity(0x001FFFFFu), 2, MidiKey.A5, Pressure(0x42)),
                ControlChange(VariableLengthQuantity(0x80u), 3, ControllerNumber(3), MidiDataByte(0x42)),
                ProgramChange(VariableLengthQuantity(0x4000u), 4, ProgramNumber(5)),
                ChannelPressure(VariableLengthQuantity(0x39u), 5, Pressure(127)),
                PitchWheelChange(VariableLengthQuantity(0x23u), 6, MidiDataShort(0x7F, 0x67)),
                EndOfTrack(VariableLengthQuantity(0u))
            )
        )
    ),
    "All midi channel messages"
)

// This test file was generated by ChatGPT and rechecked by a human
private val allMetaEvents = ValidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray()) // type
        .plus(byteArrayOf(0, 0, 0, 101)) // size
        .plus(
            byteArrayOf(
                // Sequence Number
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.SEQUENCE_NUMBER.type.toByte(),
                0x02,
                0x00,
                0x01,

                // Text Event ("Hello")
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.TEXT_EVENT.type.toByte(),
                0x05,
                'H'.code.toByte(),
                'e'.code.toByte(),
                'l'.code.toByte(),
                'l'.code.toByte(),
                'o'.code.toByte(),

                // Copyright Notice
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.COPYRIGHT_NOTICE.type.toByte(),
                0x03,
                'C'.code.toByte(),
                'o'.code.toByte(),
                'p'.code.toByte(),

                // Track Name
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.TRACK_NAME.type.toByte(),
                0x04,
                'T'.code.toByte(),
                'r'.code.toByte(),
                'a'.code.toByte(),
                'k'.code.toByte(),

                // Instrument Name
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.INSTRUMENT_NAME.type.toByte(),
                0x03,
                'P'.code.toByte(),
                'i'.code.toByte(),
                'a'.code.toByte(),

                // Marker
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.MARKER.type.toByte(),
                0x02,
                'M'.code.toByte(),
                '1'.code.toByte(),

                // Cue Point
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.CUE_POINT.type.toByte(),
                0x02,
                'C'.code.toByte(),
                '1'.code.toByte(),

                // MIDI Channel Prefix
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.MIDI_CHANNEL_PREFIX.type.toByte(),
                0x01,
                0x03,

                // Set Tempo (500000 microseconds per quarter note)
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.SET_TEMPO.type.toByte(),
                0x03,
                0x07,
                0xA1.toByte(),
                0x20,

                // SMPTE Offset
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.SMPTE_OFFSET.type.toByte(),
                0x05,
                0x00,
                0x01,
                0x02,
                0x03,
                0x04,

                // Time Signature
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.TIME_SIGNATURE.type.toByte(),
                0x04,
                0x04,
                0x02,
                0x18,
                0x08,

                // Key Signature
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.KEY_SIGNATURE.type.toByte(),
                0x02,
                MidiKeySignature.SIX_FLATS.data,
                0x01,

                // Sequencer Specific (2 arbitrary bytes)
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.SEQUENCER_SPECIFIC.type.toByte(),
                0x02,
                0x55,
                0x66,

                // Unknown Meta Event (type 0x6A)
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                0x6A,
                0x03,
                0x10,
                0x20,
                0x30,

                // End Of Track
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0x00
            )
        ),
    StandardMidiFile(
        basicFormat0HeaderChunk,
        format0MidiSequence(
            listOf(
                SequenceNumber(VariableLengthQuantity(0u), 1u),
                TextEvent(VariableLengthQuantity(0u), "Hello"),
                CopyrightNotice(VariableLengthQuantity(0u), "Cop"),
                TrackName(VariableLengthQuantity(0u), "Trak"),
                InstrumentName(VariableLengthQuantity(0u), "Pia"),
                Marker(VariableLengthQuantity(0u), "M1"),
                CuePoint(VariableLengthQuantity(0u), "C1"),
                MidiChannelPrefix(VariableLengthQuantity(0u), 3),
                SetTempo(VariableLengthQuantity(0u), 500_000u),
                SMPTEOffset(VariableLengthQuantity(0u), 0, 1, 2, 3, 4),
                TimeSignature(VariableLengthQuantity(0u), 4, 2, 24, 8),
                KeySignature(VariableLengthQuantity(0u), MidiKeySignature.SIX_FLATS, 1),
                SequencerSpecific(VariableLengthQuantity(0u), byteArrayOf(0x55, 0x66)),
                UnknownMetaEvent(VariableLengthQuantity(0u), 0x6A, byteArrayOf(0x10, 0x20, 0x30)),
                EndOfTrack(VariableLengthQuantity(0u))
            )
        )
    ),
    "All implemented MIDI meta events, including unknown meta type"
)

private val correctSysexMessages = ValidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 37))
        .plus(
            byteArrayOf(
                0x00,
                SystemExclusiveFormat.F0_FORMAT.byte,
                0x2,
                0x42,
                0xF7.toByte(), // The F0 sysex event is done

                0x00,
                SystemExclusiveFormat.F7_FORMAT.byte,
                0x2,
                0x34,
                0x35, // We don't need to end with F7 if we start a sysex event with F7

                // Starting the example of multi-packet sysex message from the midi spec documents
                0x00,
                SystemExclusiveFormat.F0_FORMAT.byte,
                0x03,
                0x43,
                0x12,
                0x00,

                0x81.toByte(),
                0x48,
                SystemExclusiveFormat.F7_FORMAT.byte,
                0x06,
                0x43,
                0x12,
                0x00,
                0x43,
                0x12,
                0x00,

                0x64,
                SystemExclusiveFormat.F7_FORMAT.byte,
                0x04,
                0x43,
                0x12,
                0x00,
                0xF7.toByte(),

                // End Of Track
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0x00
            )
        ),
    StandardMidiFile(
        basicFormat0HeaderChunk,
        format0MidiSequence(
            listOf(
                SystemExclusiveEvent(
                    VariableLengthQuantity(0u),
                    SystemExclusiveFormat.F0_FORMAT,
                    byteArrayOf(
                        0x42,
                        0xF7.toByte()
                    )
                ),
                SystemExclusiveEvent(
                    VariableLengthQuantity(0u),
                    SystemExclusiveFormat.F7_FORMAT,
                    byteArrayOf(
                        0x34,
                        0x35
                    )
                ),
                SystemExclusiveEvent(
                    VariableLengthQuantity(0u),
                    SystemExclusiveFormat.F0_FORMAT,
                    byteArrayOf(
                        0x43,
                        0x12,
                        0x00,
                    )
                ),
                SystemExclusiveEvent(
                    VariableLengthQuantity(200u),
                    SystemExclusiveFormat.F7_FORMAT,
                    byteArrayOf(
                        0x43,
                        0x12,
                        0x00,
                        0x43,
                        0x12,
                        0x00,
                    )
                ),
                SystemExclusiveEvent(
                    VariableLengthQuantity(100u),
                    SystemExclusiveFormat.F7_FORMAT,
                    byteArrayOf(
                        0x43,
                        0x12,
                        0x00,
                        0xF7.toByte()
                    )
                ),
                EndOfTrack(VariableLengthQuantity(0u))
            )
        )
    ),
    "Valid sysex messages with multi-packet sysex message"
)

private val format1MidiFile = ValidMidiTestFile(
    MThd_MAGIC.toByteArray() // chunk type
        .plus(HEADER_CHUNK_LENGTH.toByteArray()) // chunk length
        .plus(1u.toUShort().toByteArray()) // format
        .plus(2u.toUShort().toByteArray()) // number of tracks
        .plus(96u.toUShort().toByteArray()) // ticks per quarter note division
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 11))
        .plus(
            byteArrayOf(
                0x00,
                (MidiChannelMessageStatus.NOTE_ON.status shl 4).toByte(),
                MidiKey.C4.key,
                MidiVelocity.Piano.velocity,

                96,
                MidiKey.C4.key,
                MidiVelocity.Niente.velocity,

                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0x00
            )
        )
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 11))
        .plus(
            byteArrayOf(
                0x00,
                (MidiChannelMessageStatus.NOTE_ON.status shl 4).toByte(),
                MidiKey.D7.key,
                MidiVelocity.Piano.velocity,

                96,
                MidiKey.D7.key,
                MidiVelocity.Niente.velocity,

                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0x00
            )
        ),
    StandardMidiFile(
        HeaderChunk(1u, 2u, TicksPerQuarterNoteDivision(96u)),
        MidiSequence(
            listOf(
                TrackChunk(
                    listOf(
                        NoteOn(
                            VariableLengthQuantity(0u), 0, MidiKey.C4,
                            MidiVelocity.Piano
                        ),
                        NoteOn(
                            VariableLengthQuantity(96u), 0, MidiKey.C4,
                            MidiVelocity.Niente
                        ),
                        EndOfTrack(VariableLengthQuantity(0u))
                    )
                ),
                TrackChunk(
                    listOf(
                        NoteOn(
                            VariableLengthQuantity(0u), 0, MidiKey.D7,
                            MidiVelocity.Piano
                        ),
                        NoteOn(
                            VariableLengthQuantity(96u), 0, MidiKey.D7,
                            MidiVelocity.Niente
                        ),
                        EndOfTrack(VariableLengthQuantity(0u))
                    )
                )
            )
        )
    ),
    "Format 1 standard midi file"
)

private val format2MidiFile = ValidMidiTestFile(
    MThd_MAGIC.toByteArray() // chunk type
        .plus(HEADER_CHUNK_LENGTH.toByteArray()) // chunk length
        .plus(2u.toUShort().toByteArray()) // format
        .plus(2u.toUShort().toByteArray()) // number of tracks
        .plus(96u.toUShort().toByteArray()) // ticks per quarter note division
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 11))
        .plus(
            byteArrayOf(
                0x00,
                (MidiChannelMessageStatus.NOTE_ON.status shl 4).toByte(),
                MidiKey.C4.key,
                MidiVelocity.Piano.velocity,

                96,
                MidiKey.C4.key,
                MidiVelocity.Niente.velocity,

                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0x00
            )
        )
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 11))
        .plus(
            byteArrayOf(
                0x00,
                (MidiChannelMessageStatus.NOTE_ON.status shl 4).toByte(),
                MidiKey.D7.key,
                MidiVelocity.Piano.velocity,

                96,
                MidiKey.D7.key,
                MidiVelocity.Niente.velocity,

                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0x00
            )
        ),
    StandardMidiFile(
        HeaderChunk(2u, 2u, TicksPerQuarterNoteDivision(96u)),
        MidiSequence(
            listOf(
                TrackChunk(
                    listOf(
                        NoteOn(
                            VariableLengthQuantity(0u), 0, MidiKey.C4,
                            MidiVelocity.Piano
                        ),
                        NoteOn(
                            VariableLengthQuantity(96u), 0, MidiKey.C4,
                            MidiVelocity.Niente
                        ),
                        EndOfTrack(VariableLengthQuantity(0u))
                    )
                ),
                TrackChunk(
                    listOf(
                        NoteOn(
                            VariableLengthQuantity(0u), 0, MidiKey.D7,
                            MidiVelocity.Piano
                        ),
                        NoteOn(
                            VariableLengthQuantity(96u), 0, MidiKey.D7,
                            MidiVelocity.Niente
                        ),
                        EndOfTrack(VariableLengthQuantity(0u))
                    )
                )
            )
        )
    ),
    "Format 2 standard midi file"
)

val validTestFiles = listOf(
    noteOnAndNoteOff,
    smpteDivision,
    basicRunningStatus,
    runningStatusInterruptedByMetaAndSySexEvent,
    allChannelMessage,
    allMetaEvents,
    correctSysexMessages,
    format1MidiFile,
    format2MidiFile
)

data class InvalidMidiTestFile(
    val bytes: ByteArray,
    val expectedException: Exception,
    val testMessage: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InvalidMidiTestFile) return false

        if (!bytes.contentEquals(other.bytes)) return false
        if (expectedException != other.expectedException) return false
        if (testMessage != other.testMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + expectedException.hashCode()
        result = 31 * result + testMessage.hashCode()
        return result
    }
}

private val emptyFile = InvalidMidiTestFile(
    ByteArray(0),
    EmptyFile(),
    "Empty file"
)

private val missingChunkBytes = InvalidMidiTestFile(
    "lol".toByteArray(Charsets.US_ASCII),
    MissingBytes("midi chunk", 8, 3),
    "Missing chunk bytes"
)

private val invalidHeaderChunkType = InvalidMidiTestFile(
    byteArrayOf(0, 1, 2, 3, 0, 0, 0, 0),
    InvalidChunkType("header", MThd_MAGIC, 0x00010203u),
    "Invalid header chunk type"
)

private val invalidHeaderChunkLength = InvalidMidiTestFile(
    MThd_MAGIC.toByteArray().plus(0xF5678u.toByteArray()),
    InvalidHeaderChunkLength(0xF5678u),
    "Invalid header chunk length"
)

private val invalidFileFormat = InvalidMidiTestFile(
    MThd_MAGIC.toByteArray().plus(byteArrayOf(0, 0, 0, 6)).plus(byteArrayOf(0, 3)),
    InvalidMidiFileFormat(3u),
    "Invalid file format"
)

private val invalidNumberOfTracksForFormat0File = InvalidMidiTestFile(
    MThd_MAGIC.toByteArray().plus(byteArrayOf(0, 0, 0, 6)).plus(byteArrayOf(0, 0, 0, 3)),
    InvalidNumberOfTracksForFormat0File(3u),
    "Invalid number of tracks for format 0 file"
)

private val invalidSMPTEFramesPerSecond = InvalidMidiTestFile(
    MThd_MAGIC.toByteArray().plus(byteArrayOf(0, 0, 0, 6)).plus(byteArrayOf(0, 0, 0, 1, -20, 40)),
    InvalidSMPTEFramesPerSecond(-20),
    "Invalid SMPTE frames per second"
)

private val invalidVariableLengthQuantity = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 5))
        .plus(
            byteArrayOf(
                0x80.toByte(), // delta-time with more than 4 bytes
                0x80.toByte(),
                0x80.toByte(),
                0x80.toByte(),
                0x80.toByte()
            )
        ),
    InvalidVariableLengthQuantityLength(5),
    "Invalid variable length quantity"
)

private val missingMidiChannelMessageStatus = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 6))
        .plus(
            byteArrayOf(
                0x00, // delta-time
                SystemExclusiveFormat.F7_FORMAT.byte,
                0x00, // sysex message length of 0

                0x00, // channel message delta-time
                MidiKey.C4.key, // no status byte already midi key
                MidiVelocity.Piano.velocity
            )
        ),
    MissingMidiChannelMessageStatus(),
    "Missing midi channel message status"
)

private val trackChunkLengthTooSmall = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 2))
        .plus(
            byteArrayOf(
                // End Of Track
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0x00
            )
        ),
    TrackChunkLengthTooSmall(2u, 4u),
    "Track chunk length too small"
)

private val emptyMidiTrackEvents = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 0)),
    EmptyMidiTrackEvents(),
    "Empty midi track events"
)

private val invalidFinalEventInTrack = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 3))
        .plus(
            byteArrayOf(
                0x00, // delta-time
                SystemExclusiveFormat.F7_FORMAT.byte,
                0x00, // sysex message length of 0
            )
        ),
    InvalidFinalEventInTrack(SystemExclusiveEvent::class.simpleName),
    "Invalid final event in track"
)

private val invalidMetaEventLength = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 7))
        .plus(
            byteArrayOf(
                // Time Signature
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.TIME_SIGNATURE.type.toByte(),
                0x03, // The length is supposed to be 4
                0x04,
                0x02,
                0x18,
            )
        ),
    InvalidMetaEventLength("time signature", 4, 3),
    "Invalid meta event length"
)

private val invalidMidiDataByte = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 4))
        .plus(
            byteArrayOf(
                0x00, // channel message delta-time
                (MidiChannelMessageStatus.NOTE_ON.status shl 4).toByte(),
                MidiKey.C4.key,
                0x96.toByte() // Invalid velocity since msb is set
            )
        ),
    InvalidMidiDataByte("midi velocity", 0x96.toByte()),
    "Invalid midi data byte"
)

private val illegalMessageStatusByte = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 2))
        .plus(
            byteArrayOf(
                0x00, // channel message delta-time
                0xF1.toByte()
            )
        ),
    IllegalMessageStatusByte(0xF1.toByte()),
    "Illegal message status byte"
)

// TODO: Write invalid test files for sysex multi-packet event
private val invalidSingleSysexF0Event = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 8))
        .plus(
            byteArrayOf(
                0x00, // delta-time
                SystemExclusiveFormat.F0_FORMAT.byte,
                0x01, // sysex message length of 0
                0x40,

                // End Of Track
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0x00
            )
        ),
    SystemExclusiveFormatF0IncorrectEnding(),
    "Single F0 sysex event incorrect ending"
)

private val twoF0SysexInMultiPacketSysex = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 6))
        .plus(
            byteArrayOf(
                0x00, // delta-time
                SystemExclusiveFormat.F0_FORMAT.byte,
                0x01, // sysex message length of 0
                0x40,

                0x00, // delta-time
                SystemExclusiveFormat.F0_FORMAT.byte,
            )
        ),
    MultiPacketedSystemExclusiveMessageError(),
    "Two F0 sysex event in multi-packet sysex event"
)

private val invalidEndingInMultiPacketSysexEvent = InvalidMidiTestFile(
    basicFormat0HeaderChunkBytes
        .plus(MTrk_MAGIC.toByteArray())
        .plus(byteArrayOf(0, 0, 0, 27))
        .plus(
            byteArrayOf(
                // Starting the example of multi-packet sysex message from the midi spec documents with error at the end
                0x00,
                SystemExclusiveFormat.F0_FORMAT.byte,
                0x03,
                0x43,
                0x12,
                0x00,

                0x81.toByte(),
                0x48,
                SystemExclusiveFormat.F7_FORMAT.byte,
                0x06,
                0x43,
                0x12,
                0x00,
                0x43,
                0x12,
                0x00,

                0x64,
                SystemExclusiveFormat.F7_FORMAT.byte,
                0x04,
                0x43,
                0x12,
                0x00,
                0x69, // This is supposed to be the F7 byte

                // End Of Track
                0x00,
                META_EVENT_STATUS_BYTE.toByte(),
                MetaEventsType.END_OF_TRACK.type.toByte(),
                0x00
            )
        ),
    SystemExclusiveFormatF0IncorrectEnding(),
    "Invalid ending in multi-packet sysex event"
)

val invalidTestFiles = listOf(
    emptyFile,
    missingChunkBytes,
    invalidHeaderChunkType,
    invalidHeaderChunkLength,
    invalidFileFormat,
    invalidNumberOfTracksForFormat0File,
    invalidSMPTEFramesPerSecond,
    invalidVariableLengthQuantity,
    missingMidiChannelMessageStatus,
    trackChunkLengthTooSmall,
    emptyMidiTrackEvents,
    invalidFinalEventInTrack,
    invalidMetaEventLength,
    invalidMidiDataByte,
    illegalMessageStatusByte,
    invalidSingleSysexF0Event,
    twoF0SysexInMultiPacketSysex,
    invalidEndingInMultiPacketSysexEvent
)
