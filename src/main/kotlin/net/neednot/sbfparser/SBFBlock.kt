package net.neednot.sbfparser

//mosaic-X5 Firmware v4.14.10.1 Reference Guide
//https://www.septentrio.com/en/products/gnss-receivers/gnss-receiver-modules/mosaic-x5#resources

data class SBFBlock(
    val header: BlockHeader,
    val timestamp: BlockTimestamp,
    val body: BlockBody,
)

data class BlockHeader(
    val sync: String,
    val crc: UShort,
    val id: BlockId,
    val length: UShort,
)

data class BlockId(
    val number: UShort,
    val revision: UShort
)

data class BlockTimestamp(
    val tow: UInt,
    val wnc: UShort
)