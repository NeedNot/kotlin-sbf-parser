package net.neednot.sbfparser

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

private val BLOCK_SYNC = byteArrayOf(36, 64)

class SBFParser {
    private var buffer: MutableList<Byte> = mutableListOf()
    val length: Int
        get() = buffer.size
    private val blocks: MutableList<SBFBlock> = mutableListOf()

    fun addData(data: ByteArray) {
        buffer.addAll(data.toList())
        if (buffer.isEmpty()) return

        val byteArray = buffer.toByteArray()
        var position: Int
        while (true) {
            position = byteArray.findFirst(BLOCK_SYNC)
            if (position < 0) break
            val byteBuffer = ByteBuffer.wrap(byteArray)
            byteBuffer.position(position)
            val header = try {
                parseHeader(byteBuffer)
            } catch (e: IncompleteBlockException) {
                e.printStackTrace()
                continue
            }
            val timestamp = parseTimestamp(byteBuffer)
            val body = parseBody(byteBuffer, header.id.number)
            blocks += SBFBlock(
                header = header,
                timestamp = timestamp,
                body = body
            )
        }
    }

    fun getBlocks(): List<SBFBlock> {
        return blocks.toList().also { blocks.clear() }
    }
}

fun parseHeader(data: ByteBuffer): BlockHeader {
    if (data.remaining() < 8) throw IncompleteBlockException("Header requires 8 bytes", data.array())

    val sync = String(charArrayOf(data.get().toInt().toChar(),data.get().toInt().toChar()))
    println("${data.position()}  ${data.remaining()}")
    val crc = data.getShort().toUShort()

    val id = data.getShort()

    println("${data.position()}  ${data.remaining()}")

    val length = data.getShort().toUShort()
    if (length.mod(4u)>0u) throw BlockException("Length must be a multiple of 4", data.array())

//    create SBFBlockId from id
    val number = id.toInt() and 0x1FFF
    val revision = (id.toInt() shr 13) and 0x07

    return BlockHeader(
        sync = sync,
        crc = crc,
        id = BlockId(number.toUShort(), revision.toUShort()),
        length = length
    )
}

private fun isStartOrBlock() {
    TODO()
//        val bytesForCRC = ByteArray(length.toInt()-8)
//    data.get(bytesForCRC)
//    val crcCom = computeCRC(bytesForCRC)
//    if (crcCom != crc) throw BlockException("CRCs did not match", data.array())
}

private fun parseTimestamp(data: ByteBuffer): BlockTimestamp {
    val tow = data.getInt().toUInt()
    val wnc = data.getShort().toUShort()

    return BlockTimestamp(
        tow = if (tow == TOW_DO_NOT_USE_VALUE) null else tow,
        wnc = if (wnc == WNC_DO_NOT_USE_VALUE) null else wnc
    )
}

private fun parseBody(data: ByteBuffer, id: UShort): BlockBody {
//    todo find the block id
    return decode(data, QualityInd::class.java)
}