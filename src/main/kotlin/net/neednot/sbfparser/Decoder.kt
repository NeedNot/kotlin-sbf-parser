package net.neednot.sbfparser

import java.nio.ByteBuffer
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

@OptIn(ExperimentalUnsignedTypes::class)
fun <T : BlockBody> decode(bytes: ByteBuffer, clazz: Class<T>): T {
    val constructor = clazz.kotlin.constructors.firstOrNull()
        ?: throw IllegalArgumentException("No suitable constructor found.")

    val params = mutableMapOf<KParameter, Any?>()

    constructor.parameters.filter { it.name != "name" }.forEach { param ->
        val type = param.type
        val value: Any? = when (type.classifier) {
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
                val size = getArraySize(clazz, param, params)
                val stringBytes = ByteArray(size)
                bytes.get(stringBytes)
                stringBytes.toString(Charsets.US_ASCII)
            }
            UShortArray::class -> {
                // number of uShorts (2 bytes each)
                val size = getArraySize(clazz, param, params)
                if (bytes.remaining() < size * 2) {
                    throw IncompleteBlockException("Not enough bytes to decode UShortArray", bytes.array())
                }
                val byteArray = ByteArray(size * 2)
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

private fun getArraySize(clazz: Class<*>, param: KParameter, params: Map<KParameter, Any?>): Int {
    val annotation = clazz.kotlin.findAnnotation<ArraySize>() ?: throw IllegalArgumentException("String or Array property ${param.name} requires @ArraySize annotation")

    val sizeField = annotation.sizeField
    return if (sizeField != "") {
        val mapEntry = (params.entries.first { it.key.name == sizeField })
        val value = mapEntry.value
        try {
            decodeNumber(value)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("${param.name} is not a valid number type: $value")
        }
    } else {
        if (annotation.size == -1) {
            throw IllegalArgumentException("@ArraySize annotation requires a sizeField or size parameter")
        }
        annotation.size
    }
}

private fun <T>decodeNumber(value: T): Int {
    return when (value) {
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
        else -> throw IllegalArgumentException()
    }
}

/**
 * Put this annotation over a String or array property to specify the size
 *
 * @param sizeField name of a decoded property to use as the size
 * @param size hardcoded int value to use as the size
 *
 * Example
 *
 * Block 4082 has property N and later Indicators
 * N is an unsigned 8-bit integer while Indicators is an Ascii string of N size.
 * So you would annotate indicators with the [sizeField] = "n"
 * */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ArraySize(val sizeField: String = "", val size: Int = -1)