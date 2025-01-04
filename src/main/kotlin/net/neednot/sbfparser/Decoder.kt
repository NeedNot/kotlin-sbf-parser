package net.neednot.sbfparser

import java.nio.ByteBuffer
import kotlin.reflect.KParameter

fun <T : BlockBody> decode(bytes: ByteBuffer, clazz: Class<T>): T {
    val constructor = clazz.kotlin.constructors.first()
    val params = mutableMapOf<KParameter, Any?>()

    constructor.parameters.forEach { param ->
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
                val annotation = clazz.kotlin.members
                    .find { it.name == param.name }
                    ?.annotations
                    ?.filterIsInstance<StringLength>()
                    ?.firstOrNull()

                if (annotation != null) {
                    val lengthFieldName = annotation.lengthFieldName
                    val length = if (lengthFieldName == "") {
                        params.entries.first { it.key.name == lengthFieldName }.value as Int
                    } else {
                        if (annotation.length == -1) {
                            throw IllegalArgumentException("@StringLength annotation requires a lengthFieldName or length parameter")
                        }
                        annotation.length
                    }
                    val stringBytes = ByteArray(length)
                    bytes.get(stringBytes, 0, length)
                    stringBytes.toString(Charsets.US_ASCII)
                } else {
                    throw IllegalArgumentException("String property ${param.name} requires @StringLength annotation")
                }
            }
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }
        params[param] = value
    }

    return constructor.callBy(params)
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
annotation class StringLength(val lengthFieldName: String = "", val length: Int = -1)