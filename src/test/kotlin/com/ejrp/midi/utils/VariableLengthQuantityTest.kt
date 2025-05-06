package com.ejrp.midi.utils

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

/*
The test mainly uses this table:

     NUMBER        VARIABLE QUANTITY
    00000000              00
    00000040              40
    0000007F              7F
    00000080             81 00
    00002000             C0 00
    00003FFF             FF 7F
    00004000           81 80 00
    00100000           C0 80 00
    001FFFFF           FF FF 7F
    00200000          81 80 80 00
    08000000          C0 80 80 00
    0FFFFFFF          FF FF FF 7F

 */
class VariableLengthQuantityTest {
    val valueToVlq = mapOf(
        0x0u to byteArrayOf(0x00),
        0x40u to byteArrayOf(0x40),
        0x7Fu to byteArrayOf(0x7F),
        0x80u to byteArrayOf(0x81.toByte(), 0x00),
        0x2000u to byteArrayOf(0xC0.toByte(), 0x00),
        0x3FFFu to byteArrayOf(0xFF.toByte(), 0x7F),
        0x4000u to byteArrayOf(0x81.toByte(), 0x80.toByte(), 0x00),
        0x100000u to byteArrayOf(0xC0.toByte(), 0x80.toByte(), 0x00),
        0x1FFFFFu to byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0x7F),
        0x200000u to byteArrayOf(0x81.toByte(), 0x80.toByte(), 0x80.toByte(), 0x00),
        0x8000000u to byteArrayOf(0xC0.toByte(), 0x80.toByte(), 0x80.toByte(), 0x00),
        0xFFFFFFFu to byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x7F)
    )

    @Test
    fun toOutputStream() {
        for ((value, array) in valueToVlq) {
            val actual = VariableLengthQuantity(value).toOutputStream(ByteArrayOutputStream()) as ByteArrayOutputStream
            assertArrayEquals(
                array,
                actual.toByteArray()
            ) {
                "ERROR toOutputStream on value ${value.toHexString()}\n" +
                        "\tExpected: ${array.toHexStringList()}\n" +
                        "\tActual: ${actual.toByteArray().toHexStringList()}"
            }
        }
    }

    @Test
    fun length() {
        for ((value, array) in valueToVlq) {
            val actual = VariableLengthQuantity(value).length()
            assertEquals(
                array.size, actual) {
                "ERROR length on value ${value.toHexString()}\n" +
                        "\tExpected: ${array.size}\n" +
                        "\tActual: $actual"
            }
        }
    }

    @Test
    fun fromInputStream() {
        for ((value, array) in valueToVlq) {
            // Note that we are also testing the overridden equals operator in here
            val actual = VariableLengthQuantity.fromInputStream(array.inputStream())
            assertEquals(
                VariableLengthQuantity(value),
                actual
            )  {
                "ERROR toOutputStream on value ${value}\n" +
                        "\tExpected: ${VariableLengthQuantity(value)}\n" +
                        "\tActual: $actual"
            }
        }
    }
}

