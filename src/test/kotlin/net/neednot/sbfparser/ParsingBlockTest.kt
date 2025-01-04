package net.neednot.sbfparser

import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.system.measureTimeMillis
@OptIn(ExperimentalStdlibApi::class)
class ParsingBlockTest {

    @Test
    fun parseBlockTest() {
        val data = "24 40 34 b1 f2 0f 18 00".hexToByteArray()
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val header = parseHeader(buffer)
        println(header)
    }

    @Test
    fun parseTimestampTest() {
        val data = "ff ff ff ff ff ff".hexToByteArray()
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val timestamp = parseTimestamp(buffer)
        println(timestamp)
    }

    @Test
    fun parseBodyTest() {
        val data = "04 00 0b 00 01 00 15 0a 00 00 24 40 34 b1 f2 0f 18 00 ff ff ff ff ff ff 04 00 0b 00 01 00 15 0a 00 00".hexToByteArray()
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val body = parseBody(buffer, 4082u)
        println(body)
    }
}