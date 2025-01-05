package net.neednot.sbfparser

import java.nio.ByteBuffer
import kotlin.reflect.KParameter

@OptIn(ExperimentalUnsignedTypes::class)
fun <T : BlockBody> decode(bytes: ByteBuffer, clazz: Class<T>): T {
    val constructor = clazz.kotlin.constructors.first()
    val params = mutableMapOf<KParameter, Any?>()

    constructor.parameters.filter { it.name != "name" }.forEach { param ->
        val type = param.type
        val value = when (type.classifier) {
            UByte::class -> bytes.get().toUByte()
            UShort::class -> bytes.getShort().toUShort()
            UInt::class -> bytes.getInt().toUInt()
            ULong::class -> bytes.getLong().toULong()
            Byte::class -> bytes.get()
            Short::class -> bytes.getShort()
            Int::class -> bytes.getInt()
            Long::class -> bytes.getLong()
            Float::class -> bytes.getFloat()
            Double::class -> bytes.getDouble()
            String::class -> {
                val length = getArrayLength(clazz, param, params)
                val stringBytes = ByteArray(length)
                bytes.get(stringBytes)
                stringBytes.toString(Charsets.US_ASCII)
            }
            UShortArray::class -> {
//                number of uShorts (2 bytes each)
                val length = getArrayLength(clazz, param, params)
                if (bytes.remaining() < length*2) {
                    throw IncompleteBlockException("Not enough bytes to decode UShortArray", bytes.array())
                }
                val byteArray = ByteArray(length*2)
                bytes.get(byteArray)
                ShortArray(byteArray.size / 2) {
                    (byteArray[it * 2].toUByte().toInt() + (byteArray[(it * 2) + 1].toInt() shl 8)).toShort()
                }.toUShortArray()
            }
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }
        params[param] = value
    }

    return constructor.callBy(params)
}

private fun getArrayLength(clazz: Class<*>, param: KParameter, params: Map<KParameter, Any?>): Int {
    val annotation = clazz.kotlin.members
        .find { it.name == param.name }
        ?.annotations
        ?.filterIsInstance<ArrayLength>()
        ?.firstOrNull()

    if (annotation == null) {
        throw IllegalArgumentException("String or Array property ${param.name} requires @ArrayLength annotation")
    }

    val lengthFieldName = annotation.lengthFieldName
    return if (lengthFieldName != "") {
        val mapEntry = (params.entries.first { it.key.name == lengthFieldName })
        val value = mapEntry.value
        when (value) {
            is Byte -> value.toInt()
            is UByte -> value.toInt()
            is Short -> value.toInt()
            is UShort -> value.toInt()
            is Int -> value
            is UInt -> value.toInt()
            is Long -> value.toInt()
            is ULong -> value.toInt()
            is Float -> value.toInt()
            is Double -> value.toInt()
            else -> throw IllegalArgumentException("${param.name} is not a valid number type: $value")
        }
    } else {
        if (annotation.length == -1) {
            throw IllegalArgumentException("@ArrayLength annotation requires a lengthFieldName or length parameter")
        }
        annotation.length
    }
}



/**
 * Put this annotation over a String property to specify the length
 *
 * @param lengthFieldName name of a decoded property to use as the length
 * @param length hardcoded int value to use as the length
 *
 * Example
 * Block 4082 has property N and later Indicators
 * N is an unsigned 8-bit integer while Indicators is an Ascii string of N length.
 * So you would annotate indicators with the lengthFieldName = "n"
 * */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ArrayLength(val lengthFieldName: String = "", val length: Int = -1)