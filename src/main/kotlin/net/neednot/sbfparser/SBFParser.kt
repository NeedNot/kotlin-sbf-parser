package net.neednot.sbfparser

import java.nio.ByteBuffer
import java.nio.ByteOrder

class SBFParser {
    private var buffer: MutableList<Byte> = mutableListOf()
    val length: Int
        get() = buffer.size
    private val blocks: MutableList<SBFBlock> = mutableListOf()

    fun addData(data: ByteArray) {
        buffer.addAll(data.toList())
        if (buffer.isEmpty()) return

        var position: Int
        while (true) {
            val byteArray = buffer.toByteArray()
            position = byteArray.indexOf(BLOCK_SYNC)
            if (position < 0) break
            val byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
            byteBuffer.position(position)
            try {
                val header = parseHeader(byteBuffer)
                val originalPosition = byteBuffer.position()
                val crcBufferLength = 4+(header.length.toInt()-8)
                val crcArray = ByteArray(crcBufferLength)
                byteBuffer.position(originalPosition-4)
                if (crcBufferLength > byteBuffer.remaining()) {
                    throw IncompleteBlockException("Not enough bytes to make a block", byteBuffer.array())
                }
                byteBuffer.get(crcArray)
                if (!validateCRC(crcArray, header.crc)) {
                    throw InvalidBlockException("CRCs did not match", byteBuffer.array())
                }
//                reset to after header
                byteBuffer.position(originalPosition)
                val timestamp = parseTimestamp(byteBuffer)
                val body = parseBody(byteBuffer, header.id.number)

                // Successfully parsed block
                blocks += SBFBlock(
                    header = header,
                    timestamp = timestamp,
                    body = body
                )

                // Remove the processed bytes from the buffer
                val processedBytes = byteBuffer.position()
                buffer = buffer.drop(processedBytes).toMutableList()

            } catch (e: IncompleteBlockException) {
                break
            } catch (e: InvalidBlockException) {
//                Remove the invalid bytes from the buffer
                val processedBytes = byteBuffer.position()
                buffer = buffer.drop(processedBytes).toMutableList()
            }
        }
    }

    fun getBlocks(): List<SBFBlock> {
        return blocks.toList().also { blocks.clear() }
    }

    fun clearData() {
        buffer.clear()
    }
}

private fun parseHeader(data: ByteBuffer): BlockHeader {
    if (data.remaining() < 8) throw IncompleteBlockException("Header requires 8 bytes", data.array())

    val sync = String(charArrayOf(data.get().toInt().toChar(),data.get().toInt().toChar()))
    val crc = data.getShort().toUShort()

    val id = data.getShort()

    val length = data.getShort().toUShort()
    if (length.mod(4u)>0u) throw InvalidBlockException("Length must be a multiple of 4", data.array())

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

private fun parseTimestamp(data: ByteBuffer): BlockTimestamp {
    if (data.remaining() < 6) throw IncompleteBlockException("Timestamp requires 6 bytes", data.array())

    val tow = data.getInt().toUInt()
    val wnc = data.getShort().toUShort()

    return BlockTimestamp(
        tow = tow,
        wnc = wnc
    )
}

private fun parseBody(data: ByteBuffer, id: UShort): BlockBody {
    val clazz = blockClassById[id.toInt()] ?: throw InvalidBlockException("Unknown block type $id", data.array())
    return decode(data, clazz as Class<BlockBody>)
}

private fun validateCRC(data: ByteArray, target: UShort): Boolean {
    return computeCrc(data).toUShort() == target
}