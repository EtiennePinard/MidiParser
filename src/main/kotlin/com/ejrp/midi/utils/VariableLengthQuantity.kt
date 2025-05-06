package com.ejrp.midi.utils

import com.ejrp.midi.InvalidVariableLengthQuantityLength
import com.ejrp.midi.MissingBytes
import java.io.InputStream
import java.io.OutputStream

/**
 * A class which represent a variable length quantity of no more than 4 bytes.
 * @property quantity A positive u32 number no more than 0x0FFFFFFF
 * @constructor Creates a Variable length quantity with a quantity and a length
 * @see  <a href="https://en.wikipedia.org/wiki/Variable-length_quantity">https://en.wikipedia.org/wiki/Variable-length_quantity</a>
 * @see  <a href="https://web.archive.org/web/20051129113105/http://www.borg.com/~jglatt/tech/midifile/vari.htm#expand">https://web.archive.org/web/20051129113105/http://www.borg.com/~jglatt/tech/midifile/vari.htm#expand</a>
 */
class VariableLengthQuantity(val quantity: UInt) : Serialize, Comparable<VariableLengthQuantity> {

    init {
        require(quantity <= 0x0FFFFFFFu) {
            "The quantity of a variable length quantity is from 0 to 0x0FFFFFFF.\n" +
                    "Expected: quantity <= 0x0FFFFFFF\n" +
                    "Actual: $quantity"
        }
    }

    override fun compareTo(other: VariableLengthQuantity) = quantity compareTo other.quantity

    override fun toOutputStream(stream: OutputStream): OutputStream {
        var length = length() - 1
        while (length >= 1) {
            stream.write(
                quantity.shr(7 * length).and(0x7Fu).or(0x80u).toInt()
            )
            length--
        }
        stream.write(quantity.and(0x7Fu).toInt())
        return stream
    }

    fun length(): Int {
        return 4 - (quantity.countLeadingZeroBits() - 4) / 7 + (quantity.isZero().toInt())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VariableLengthQuantity) return false

        if (quantity != other.quantity) return false

        return true
    }

    override fun hashCode(): Int {
        return quantity.hashCode()
    }

    override fun toString(): String {
        return "VariableLengthQuantity(quantity=$quantity)"
    }

    companion object : DeserializeStream<VariableLengthQuantity> {
        override fun fromInputStream(serialized: InputStream): VariableLengthQuantity {
            var quantity = 0u
            var length = 0
            do {
                val currentByte = serialized.read()
                if (currentByte == -1) {
                    throw MissingBytes("variable length quantity", 1, 0)
                }
                length++
                if (length > 4) {
                    throw InvalidVariableLengthQuantityLength(length)
                }
                quantity = quantity.shl(7).or(currentByte.toUInt().and(0x7Fu))
            } while (currentByte.getBit(7)) // while bit 7 is set we read another value

            return VariableLengthQuantity(quantity)
        }
    }
}