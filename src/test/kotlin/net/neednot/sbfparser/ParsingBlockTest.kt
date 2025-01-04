package net.neednot.sbfparser

import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ParsingBlockTest {

    @Test
    fun parseBlockTest() {
        val data = "24 40 34 b1 f2 0f 18 00".replace(" ", "").hexToByteArray()
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val header = parseHeader(buffer)
        println(header)
    }
}