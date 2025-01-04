package net.neednot.sbfparser

import org.junit.jupiter.api.Test

//invalid data
@OptIn(ExperimentalStdlibApi::class)
class SBFParserTest {

    @Test
    fun getLength() {

    }

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
        try {
            val sbfParser = SBFParser()
            val data = "24 40 34 b1 f2 0f 18 00 ff ff ff ff ff ff 04 00 0b 00 01 00 15 0a 00 00"
                .replace(" ", "").hexToByteArray()
            sbfParser.addData(data)
            throw Exception("Invalid block not detected")
        } catch (e: InvalidBlockException) {

        }
    }
}
