package com.ejrp.midi

import com.ejrp.midi.data.HEADER_CHUNK_LENGTH
import com.ejrp.midi.utils.toByteArray
import com.ejrp.midi.utils.toHexString

private fun <T> message(message: String, expected: T, actual: T) = "$message\n\tExpected: $expected\n\tActual: $actual"

class EmptyFile() : Exception() {
    override val message: String = "The input stream is empty, so we cannot parse a midi file."
    override fun equals(other: Any?) = this === other || (other is EmptyFile && message == other.message)
    override fun hashCode() = message.hashCode()
}

class MissingBytes(thingToDeserialize: String, expectedSize: Int, actualSize: Int) : Exception() {
    override val message: String = message("There is not enough bytes in the input stream to deserialize a $thingToDeserialize", expectedSize, actualSize)
    override fun equals(other: Any?) = this === other || (other is MissingBytes && message == other.message)
    override fun hashCode() = message.hashCode()
}

class InvalidChunkType(chunkName: String, expectedType: UInt, actualType: UInt) : Exception() {
    override val message: String = message("The first 4 bytes of the $chunkName chunk are not the magic type bytes", "\"${expectedType.toByteArray().toString(Charsets.US_ASCII)}\"", "\"${actualType.toByteArray().toString(Charsets.US_ASCII)}\"")
    override fun equals(other: Any?) = this === other || (other is InvalidChunkType && message == other.message)
    override fun hashCode() = message.hashCode()
}

class InvalidHeaderChunkLength(actualLength: UInt) : Exception() {
    override val message: String = message("The length of the header chunk is incorrect", HEADER_CHUNK_LENGTH, actualLength)
    override fun equals(other: Any?) = this === other || (other is InvalidHeaderChunkLength && message == other.message)
    override fun hashCode() = message.hashCode()
}

class InvalidMidiFileFormat(actualFormat: UShort) : Exception() {
    override val message: String = message("Unknown midi file type, cannot parse the file further", "0, 1 or 2", actualFormat)
    override fun equals(other: Any?) = this === other || (other is InvalidMidiFileFormat && message == other.message)
    override fun hashCode() = message.hashCode()
}

class InvalidNumberOfTracksForFormat0File(actualNbTracks: UShort) : Exception() {
    override val message: String = message("A format 0 midi file only has one track chunk", "1", actualNbTracks)
    override fun equals(other: Any?) = this === other || (other is InvalidNumberOfTracksForFormat0File && message == other.message)
    override fun hashCode() = message.hashCode()
}

class InvalidSMPTEFramesPerSecond(actualFramesPerSecond: Byte) : Exception() {
    override val message: String = message("There are four valid frames per second number for the SMPTE division", "-24, -25, -29 or -30", actualFramesPerSecond)
    override fun equals(other: Any?) = this === other || (other is InvalidSMPTEFramesPerSecond && message == other.message)
    override fun hashCode() = message.hashCode()
}

class InvalidVariableLengthQuantityLength(actualLength: Int) : Exception() {
    override val message: String = message("The length of the variable length quantity parsed is bigger than 4", "1, 2, 3 or 4", actualLength)
    override fun equals(other: Any?) = this === other || (other is InvalidVariableLengthQuantityLength && message == other.message)
    override fun hashCode() = message.hashCode()
}

class MissingMidiChannelMessageStatus() : Exception() {
    override val message: String = "There is no channel message status byte. You need at least to include one midi channel message status byte for running status to work."
    override fun equals(other: Any?) = this === other || (other is MissingMidiChannelMessageStatus && message == other.message)
    override fun hashCode() = message.hashCode()
}

class TrackChunkLengthTooSmall(trackChunkLength: UInt, bytesRead: UInt) : Exception() {
    override val message: String = message("The length property in the track chunk meta data indicates a smaller number than the total size of all events parsed. Please fix this defect in your midi file.", trackChunkLength, bytesRead)
    override fun equals(other: Any?) = this === other || (other is TrackChunkLengthTooSmall && message == other.message)
    override fun hashCode() = message.hashCode()
}

class EmptyMidiTrackEvents() : Exception() {
    override val message: String = "The track chunk contains no midi track events. The minimum number of event is one end of track event."
    override fun equals(other: Any?) = this === other || (other is EmptyMidiTrackEvents && message == other.message)
    override fun hashCode() = message.hashCode()
}

class InvalidFinalEventInTrack(className: String?) : Exception() {
    override val message: String = message("The last event in this track is not an end of track event", "EndOfTrack", className)
    override fun equals(other: Any?) = this === other || (other is InvalidFinalEventInTrack && message == other.message)
    override fun hashCode() = message.hashCode()
}

class InvalidMetaEventLength(metaEventName: String, expectedLength: Int, actualLength: Int) : Exception() {
    override val message: String = message("The length of the $metaEventName event is incorrect", expectedLength, actualLength)
    override fun equals(other: Any?) = this === other || (other is InvalidMetaEventLength && message == other.message)
    override fun hashCode() = message.hashCode()
}

class InvalidMidiDataByte(typeOfData: String, data: Byte) : Exception() {
    override val message: String = "The most significant bit this $typeOfData, ${data.toHexString()}, is set, which is invalid according to the standard."
    override fun equals(other: Any?) = this === other || (other is InvalidMidiDataByte && message == other.message)
    override fun hashCode() = message.hashCode()
}

class IllegalMessageStatusByte(statusByte: Byte) : Exception() {
    override val message: String = "We do not support the midi message with the status byte ${statusByte.toHexString()}"
    override fun equals(other: Any?) = this === other || (other is IllegalMessageStatusByte && message == other.message)
    override fun hashCode() = message.hashCode()
}

class SystemExclusiveFormatF0IncorrectEnding() : Exception() {
    override val message: String = "All system exclusive message using the F0 format must end with the F7 byte.\n" +
            "A tricky edge case of that rule is when you have a multi-packeted system exclusive message.\nIn that case " +
            "the first F0 message does not end with F7 but the next messages in the track\nshould all be system exclusive " +
            "messages in the F7 format and the last of these messages\nshould end with the F7 byte. Check out the standard " +
            "midi file specifications for more information on this subject."
    override fun equals(other: Any?) = this === other || (other is SystemExclusiveFormatF0IncorrectEnding && message == other.message)
    override fun hashCode() = message.hashCode()
}

class MultiPacketedSystemExclusiveMessageError() : Exception() {
    override val message: String = "A multi-packeted system exclusive message starts with a F0 system exclusive message " +
            "and all subsequent system exclusive messages are in the F7 format."
    override fun equals(other: Any?) = this === other || (other is MultiPacketedSystemExclusiveMessageError && message == other.message)
    override fun hashCode() = message.hashCode()
}
