package net.neednot.sbfparser

import org.junit.jupiter.api.Test
import java.nio.BufferUnderflowException

class BufferUnderFlowTest {
    @Test
    fun testBufferUnderFlow() {
        val sbfParser = SBFParser()
        val baseVectorGeod = byteArrayOf(36, 64, -79, 55, -68, 15, 16, 0, -1, -1, -1, -1, -1, -1, 0, 52)
        val qualityInd = byteArrayOf(36, 64, 52, -79, -14, 15, 24, 0, -1, -1, -1, -1, -1, -1, 4, 0, 11, 0, 1, 0, 21, 10)
        val emptyBytes = byteArrayOf(0,0)

//        simulate incoming data
        var i = 0
        while (i++ < 1000) {
            val bufferList = mutableListOf<Byte>()
            if (i % 100 == 0) {
//                bufferList.addAll(qualityInd.toList())
//                bufferList.addAll(emptyBytes.toList())
            }
            bufferList.addAll(baseVectorGeod.toList())

            sbfParser.addData(bufferList.toByteArray())
            Thread.sleep(10)
        }

        println(sbfParser.length)
    }
}