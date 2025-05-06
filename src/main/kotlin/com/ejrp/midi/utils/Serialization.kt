package com.ejrp.midi.utils

import java.io.InputStream
import java.io.OutputStream

/**
 * Interface with a method to serialize a type into an output stream
 */
interface Serialize {

    /**
     * Serializes a type and writes it to an output stream
     *
     * @param stream The stream to write the serialized data to
     *
     * @return The stream that was written to
     */
    fun toOutputStream(stream: OutputStream): OutputStream
}

/**
 * Interface with a method to deserialize an input stream into ToType.
 */
interface DeserializeStream<ToType> {

    /**
     * Deserializes the data from an input stream into ToType.
     *
     * @param serialized The input stream containing the serialized data
     * @return The ToType representation of the serialized data
     */
    fun fromInputStream(serialized: InputStream): ToType
}
