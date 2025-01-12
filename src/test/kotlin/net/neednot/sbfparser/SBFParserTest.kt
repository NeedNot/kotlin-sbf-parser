package net.neednot.sbfparser

import org.junit.jupiter.api.Test
import kotlin.math.abs

//invalid data
@OptIn(ExperimentalStdlibApi::class)
class SBFParserTest {

    @Test
    fun fullAndIncomplete() {
        val sbfParser = SBFParser()
        val data = "24 40 34 b1 f2 0f 18 00 ff ff ff ff ff ff 04 00 0b 00 01 00 15 0a 00 00 24 76 2e 24 40 34 b1 f2 0f 18 00 ff ff"
            .replace(" ", "").hexToByteArray()
        sbfParser.addData(data)
        val blocks = sbfParser.getBlocks()
        println(blocks)
        require(blocks.size == 1)
    }

    @Test
    fun incomplete() {
        val sbfParser = SBFParser()
        val data = "24 40 34 b1 f2 0f 18 00 ff ff ff ff ff ff 04 00 0b 00 01 00 15"
            .replace(" ", "").hexToByteArray()
        sbfParser.addData(data)
        require(sbfParser.getBlocks().isEmpty())
    }

    @Test
    fun invalid() {
        val sbfParser = SBFParser()
        val data = "24 40 34 b1 f2 0f 18 00 ff ff ff ff ff ff 04 00 0b 00 01 00 15 0a 00 00"
            .replace(" ", "").hexToByteArray()
        sbfParser.addData(data)
        require(sbfParser.getBlocks().size == 1)
    }

    @Test
    fun fullAndInvalid() {
        val sbfParser = SBFParser()
        val data = "24 40 34 b1 f2 0f 18 00 ff ff ff ff ff ff 04 00 0b 00 01 00 15 aa 00 00 24 76 2e 24 40 34 b1 f2 0f 18 00 ff ff ff ff ff ff 04 00 0b 00 01 00 15 0a 00 00 24 76 2e"
            .replace(" ", "").hexToByteArray()
        sbfParser.addData(data)
        val blocks = sbfParser.getBlocks()
        println(blocks)
        require(blocks.size == 1)
    }

    @Test
    fun badBaseVectorGeod() {
        val sbfParser = SBFParser()
        val data = byteArrayOf(36, 64, 43, 91, -68, 15, 68, 0, 14, -8, -107, 4, 45, 9, 1, 52, 18, 0, 5, 16, 35, 14, 89, 91, -80, -5, 53, -64, 101, 117, -25, 6, -62, 126, 37, 64, 57, -66, -42, 58, -116, -47, -27, -65, 102, -62, 34, -69, 124, -99, -18, -70, -64, -17, 33, 59, -91, 115, 97, -1, 0, 0, 123, 0, 13, 9, 34, 80)
        sbfParser.addData(data)
        val block = sbfParser.getBlocks().first()
        val blockBody = block.body as BaseVectorGeod
        println(block)
        require(abs(blockBody.vectorInfoGeod.first().deltaNorth) > 1 )
    }
}
