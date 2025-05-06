package com.ejrp.midi.utils

import java.io.OutputStream

/**
 * Shifts right a byte by the specified bit count.
 *
 * @param bitCount The amount of bits to shift by
 * @return The shifted byte
 */
infix fun Byte.shr(bitCount: Int) = toUByte().shr(bitCount).toByte()

/**
 * Shifts right a u8 value by the specified bit count.
 *
 * @param bitCount The amount of bits to shift by
 * @return The shifted 8-bit value
 */
infix fun UByte.shr(bitCount: Int) = toUInt().shr(bitCount).toUByte()

/**
 * Shifts right a u16 value by the specified bit count.
 *
 * @param bitCount The amount of bits to shift by
 * @return The shifted 16-bit value
 */
infix fun UShort.shr(bitCount: Int) = toUInt().shr(bitCount).toUShort()

/**
 * Shifts lefts a u16 value by the specified bit count.
 *
 * @param bitCount The amount of bits to shift by
 * @return The shifted 16-bit value
 */
infix fun UShort.shl(bitCount: Int) = toUInt().shl(bitCount).toUShort()


/**
 * Converts a 16-bit value into a byte array written with the most significant byte first
 *
 * @return The byte array of size 2 with the u16 written in it
 */
fun UShort.toByteArray(): ByteArray = byteArrayOf(shr(8).toByte(), and(0xFFu).toByte())

/**
 * Checks if an unsigned 32-bit number is non-zero
 *
 * @return 1 is the uint is zero else 1
 * @see <a href=https://stackoverflow.com/a/3912218>https://stackoverflow.com/a/3912218</a>
 */
fun UInt.isZero() = ((this or (inv() + 1u)) shr 31).inv() and 1u

/**
 * Converts a 32-bit value into a byte array written with the most significant byte first
 *
 * @return The byte array of size 4 with the u32 written in it
 */
fun UInt.toByteArray(): ByteArray = byteArrayOf(
    shr(24).toByte(),
    shr(16).and(0xFFu).toByte(),
    shr(8).and(0xFFu).toByte(),
    and(0xFFu).toByte()
)

/**
 * Writes a 16-bit value to a byte array at a specific index in big endian.
 * Asserts that all the 16-bit value can fit into the byte array.
 *
 * @param index The index to write the 16-bit value at in big endian.
 * @param data The 16-bit value to write to the byte array
 * @return The number of data written to the byte array, so 2 in this case
 */
fun ByteArray.writeU16(index: Int, data: UShort): Int {
    require(index + 1 < size) { "index + 1 is ${index + 1} which is smaller than size ($size)" }

    this[index] = data.shr(8).toByte()
    this[index + 1] = data.and(0xFFu).toByte()
    return 2
}

/**
 * Writes a 32-bit value to a byte array at a specific index in big endian.
 * Asserts that all the 32-bit value can fit into the byte array.
 *
 * @param index The index to write the 32-bit value at in big endian.
 * @param data The 32-bit value to write to the byte array
 * @return The number of data written to the byte array, so 4 in this case
 */
fun ByteArray.writeU32(index: Int, data: UInt): Int {
    require(index + 3 < size) { "index + 3 is ${index + 3} which is smaller than size ($size)" }
    this[index] = data.shl(24).and(0xFFu).toByte()
    this[index + 1] = data.shl(16).and(0xFFu).toByte()
    this[index + 2] = data.shl(8).and(0xFFu).toByte()
    this[index + 3] = data.and(0xFFu).toByte()
    return 4
}

/**
 * Reads an unsigned 16-bit number in big endian from a byte at a specified index.
 * Checks that there is at least 2 bytes left in the array from the index.
 *
 * @param index The index to read the 16-bit value at
 * @return The unsigned 16-bit number read
 */
fun ByteArray.readU16FromBytes(index: Int = 0): UShort {
    require(index + 1 < size) { "index + 1 is ${index + 1} which is smaller than size ($size)" }
    var result = this[index].toUByte().toUInt().shl(8)
    result += this[index + 1].toUByte().toUInt()
    return result.toUShort()
}

/**
 * Reads an unsigned 32-bit number in big endian from a byte at a specified index.
 * Checks that there is at least 4 bytes left in the array from the index.
 *
 * @param index The index to read the 32-bit value at
 * @return The unsigned 32-bit number read
 */
fun ByteArray.readU32FromBytes(index: Int = 0): UInt {
    require(index + 3 < size) { "index + 3 is ${index + 3} which is smaller than size ($size)" }
    var result = this[index].toUByte().toUInt().shl(24)
    result += this[index + 1].toUByte().toUInt().shl(16)
    result += this[index + 2].toUByte().toUInt().shl(8)
    result += this[index + 3].toUByte().toUInt()
    return result
}

/**
 * Convert a byte to a hex string with format 0x(first four bits)(Last four bits)
 * @return The hex representation of this byte as a string
 */
fun Byte.toHexString() = toUByte().toHexString()

/**
 * Convert a ubyte to a hex string with format 0x(first four bits)(Last four bits)
 *
 * @return The hex representation of this ubyte as a string
 */
fun UByte.toHexString() = "0x" + Integer.toHexString(toInt())

/**
 * Convert a ushort to a hex string with format 0x(first byte)(last byte)
 *
 * @return The hex representation of this ushort as a string
 */
fun UShort.toHexString() = "0x" + Integer.toHexString(toInt())

/**
 * Convert a uint to a hex string with format 0x(first byte)(second byte)(third byte)(fourth byte)
 *
 * @return The hex representation of this uint as a string
 */
fun UInt.toHexString() = "0x" + Integer.toHexString(toInt())

/**
 * Converts a byte array to a hex string list for easy logging
 * @return A list of strings which represent the bytes of the original array
 */
fun ByteArray.toHexStringList() = this.map { it.toHexString() }

/**
 * Gets the bit at the index in big endian with 0 begin the least significant bit.
 *
 * @param bitIndex The index of the bit
 * @return A boolean which represents a bit
 */
fun Byte.getBit(bitIndex: Int) = toUByte().getBit(bitIndex)

/**
 * Gets the bit at the index in big endian with 0 begin the least significant bit.
 *
 * @param bitIndex The index of the bit
 * @return A boolean which represents a bit
 */
fun Int.getBit(bitIndex: Int) = toUInt().getBit(bitIndex)

/**
 * Gets the bit at the index in big endian with 0 being the least significant bit.
 *
 * @param bitIndex The index of the bit
 * @return A boolean which represents a bit
 */
fun UByte.getBit(bitIndex: Int): Boolean {
    require(0 <= bitIndex && bitIndex <= 7) {
        "An 8-bit value only has value bit index from 0 to 7, which the bit index $bitIndex is not in this range"
    }
    return toInt() and (1 shl bitIndex) != 0
}

/**
 * Gets the bit at the index in big endian with 0 being the least significant bit.
 *
 * @param bitIndex The index of the bit
 * @return A boolean which represents a bit
 */
fun UShort.getBit(bitIndex: Int): Boolean {
    require(0 <= bitIndex && bitIndex <= 15) {
        "A 16-bit value only has value bit index from 0 to 15, which the bit index $bitIndex is not in this range"
    }
    return toInt() and (1 shl bitIndex) != 0
}

/**
 * Gets the bit at the index in big endian with 0 being the least significant bit.
 *
 * @param bitIndex The index of the bit
 * @return A boolean which represents a bit
 */
fun UInt.getBit(bitIndex: Int): Boolean {
    require(0 <= bitIndex && bitIndex <= 31) {
        "A 32-bit value only has value bit index from 0 to 31, which the bit index $bitIndex is not in this range"
    }
    return this and (1u shl bitIndex) != 0u
}

/**
 * A convenience function to write a byte to an output stream and return the stream
 *
 * @param byte The byte to write to the stream
 * @return The stream that was written to
 */
fun OutputStream.write(byte: Byte): OutputStream {
    write(byte.toInt())
    return this
}