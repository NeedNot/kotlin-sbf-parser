package net.neednot.sbfparser;

import org.junit.jupiter.api.Test;

//test for complete data
//half data
//complete + half data
//two complete data
//invalid data
class SBFParserTest {

    val data = "24 40 34 b1 f2 0f 18 00 ff ff ff ff ff ff 04 00 0b 00 01 00 15 0a 00 00 24 76 2e 24 40 34 b1 f2 0f 18 00 ff ff ff ff ff ff 04 00 0b 00 01 00 15 0a 00 00"
        .replace(" ", "")
        .hexToByteArray()
    val sbfParser = SBFParser()

    @Test
    fun getLength() {

    }

    @Test
    fun addDataAndGetBlocks() {
        sbfParser.addData(data)
        sbfParser.getBlocks().forEach {
            println(it)
        }
    }
}
