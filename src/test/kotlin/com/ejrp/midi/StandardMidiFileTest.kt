package com.ejrp.midi

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class StandardMidiFileTest {

    @Test
    fun maximumFileSize() {
        println("TEST: maximumFileSize")
        for (testFile in validTestFiles) {
            println("\t${testFile.testMessage}")
            testFile.bytes.inputStream().use {
                val midiFile = StandardMidiFile.fromInputStream(it)
                assert(testFile.bytes.size <= midiFile.maximumFileSize().toLong())
            }
        }
    }

    @Test
    fun fromInputStream() {
        println("TEST: fromInputStream")
        println("\tVALID MIDI FILES")
        for (testFile in validTestFiles) {
            println("\t\t${testFile.testMessage}")
            testFile.bytes.inputStream().use {
                val midiFile = StandardMidiFile.fromInputStream(it)
                assertEquals(testFile.parsedMidiFile, midiFile)
            }
        }
        println("\tINVALID MIDI FILES")
        for (testFile in invalidTestFiles) {
            println("\t\t${testFile.testMessage}")
            testFile.bytes.inputStream().use { inputStream ->
                try {
                    StandardMidiFile.fromInputStream(inputStream)
                } catch (exception: Exception) {
                    println(exception.message!!.split("\n").joinToString(separator = "\n") { "\t\t\t" + it })
                    assertEquals(
                        testFile.expectedException,
                        exception
                    )
                }
            }
        }
    }

    @Test
    fun toOutputStream() {
        println("TEST: toOutputStream")
        for (testFile in validTestFiles) {
            println("\t${testFile.testMessage}")
            val stream = ByteArrayOutputStream()
            testFile.parsedMidiFile.toOutputStream(stream)
            assertArrayEquals(
                testFile.bytes,
                stream.toByteArray()
            )
        }
    }

}
