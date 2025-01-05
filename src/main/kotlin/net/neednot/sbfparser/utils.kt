package net.neednot.sbfparser


/** [Source](https://stackoverflow.com/a/21341168) */
fun ByteArray.indexOf(smallerArray: ByteArray): Int {
    for (i in 0 until size - smallerArray.size + 1) {
        var found = true
        for (j in smallerArray.indices) {
            if (this[i + j] != smallerArray[j]) {
                found = false
                break
            }
        }
        if (found) return i
    }
    return -1
}

fun computeCrc(data: ByteArray): Int {
    val polynomial = 0x1021
    var crc = 0x0000

    for (byte in data) {
        val currentByte = byte.toInt() and 0xFF
        crc = crc xor (currentByte shl 8)

        for (i in 0 until 8) {
            crc = if ((crc and 0x8000) != 0) {
                (crc shl 1) xor polynomial
            } else {
                crc shl 1
            }
        }
    }

    return crc and 0xFFFF
}