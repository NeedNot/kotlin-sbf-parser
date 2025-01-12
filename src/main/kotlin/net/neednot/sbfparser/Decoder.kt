package net.neednot.sbfparser

import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

@OptIn(ExperimentalUnsignedTypes::class)
fun <T : BlockBody> decode(bytes: ByteBuffer, clazz: Class<T>): T {
    val constructor = clazz.kotlin.constructors.firstOrNull()
        ?: throw IllegalArgumentException("No suitable constructor found.")

    val params = mutableMapOf<KParameter, Any?>()

    constructor.parameters.filter { it.name != "name" }.forEach { param ->

        val subBlockListAnnotation = clazz.kotlin.members
            .find { it.name == param.name }
            ?.annotations
            ?.filterIsInstance<SubBlockList>()
            ?.firstOrNull()

        if (subBlockListAnnotation != null) {

            val sizeValue = params.entries.firstOrNull { it.key.name == subBlockListAnnotation.sizeFieldName }?.value
                ?: throw IllegalArgumentException("Could not find property '${subBlockListAnnotation.sizeFieldName}' for sub-block list size.")
            val lengthValue =
                params.entries.firstOrNull { it.key.name == subBlockListAnnotation.lengthFieldName }?.value
                    ?: throw IllegalArgumentException("Could not find property '${subBlockListAnnotation.lengthFieldName}' for sub-block list length.")

            val subBlockCount = decodeNumber(sizeValue)
            val subBlockLength = decodeNumber(lengthValue)

            val listType = param.type.arguments.first().type
                ?: throw IllegalArgumentException("Could not detect sub-block type for parameter '${param.name}'.")
            val subBlockKClass = listType.classifier as? KClass<*>
                ?: throw IllegalArgumentException("Sub-block type for '${param.name}' is not a class type.")

            val resultList = mutableListOf<Any>()
            repeat(subBlockCount) {
                if (bytes.remaining() < subBlockLength) {
                    throw IncompleteBlockException("Not enough bytes to decode sub-block", bytes.array())
                }

                @Suppress("UNCHECKED_CAST")
                val subBlockDecoded = decodeSubBlock(
                    bytes,
                    subBlockKClass.java as Class<SubBlock>
                )
                resultList.add(subBlockDecoded)
            }
            params[param] = resultList
        } else {
            val type = param.type
            val value: Any = when (type.classifier) {
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
                String::class -> decodeString(bytes, clazz, param, params)
                UShortArray::class -> decodeUShortArray(bytes, clazz, param, params)
                else -> throw IllegalArgumentException("Unsupported type: $type")
            }
            params[param] = value
        }
    }

    return constructor.callBy(params)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun <S : SubBlock> decodeSubBlock(bytes: ByteBuffer, kClass: Class<S>): S {
    // Your existing decode logic but for SubBlock
    val constructor = kClass.kotlin.constructors.firstOrNull()
        ?: throw IllegalArgumentException("No suitable constructor found for SubBlock.")

    val params = mutableMapOf<KParameter, Any?>()

    constructor.parameters.filter { it.name != "name" }.forEach { param ->
        // Decode each property in a similar manner
        val type = param.type
        val value: Any = when (type.classifier) {
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
            String::class -> decodeString(bytes, kClass, param, params)
            UShortArray::class -> decodeUShortArray(bytes, kClass, param, params)
            else -> throw IllegalArgumentException("Unsupported type in SubBlock: ${type.classifier}")
        }
        params[param] = value
    }

    return constructor.callBy(params)
}

private fun getArraySize(clazz: Class<*>, param: KParameter, params: Map<KParameter, Any?>): Int {
    val annotation = clazz.kotlin.members
        .find { it.name == param.name }
        ?.annotations
        ?.filterIsInstance<ArraySize>()
        ?.firstOrNull() ?: throw IllegalArgumentException("String or Array property ${param.name} requires @ArraySize annotation")

    val sizeFieldName = annotation.sizeFieldName
    return if (sizeFieldName != "") {
        val mapEntry = (params.entries.first { it.key.name == sizeFieldName })
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

private fun <T> decodeString(
    bytes: ByteBuffer,
    clazz: Class<T>,
    param: KParameter,
    params: MutableMap<KParameter, Any?>
): String {
    val size = getArraySize(clazz, param, params)
    val stringBytes = ByteArray(size)
    bytes.get(stringBytes)
    return stringBytes.toString(Charsets.US_ASCII)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun <T> decodeUShortArray(
    bytes: ByteBuffer,
    clazz: Class<T>,
    param: KParameter,
    params: MutableMap<KParameter, Any?>
): UShortArray {
    val size = getArraySize(clazz, param, params)
    if (bytes.remaining() < size * 2) {
        throw IncompleteBlockException("Not enough bytes to decode UShortArray", bytes.array())
    }
    val byteArray = ByteArray(size * 2)
    bytes.get(byteArray)
    return ShortArray(byteArray.size / 2) {
        (byteArray[it * 2].toUByte().toInt() + (byteArray[(it * 2) + 1].toInt() shl 8)).toShort()
    }.toUShortArray()
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
        else -> throw IllegalArgumentException("Unsupported numeric type: $value")
    }
}

/**
 * Annotation to specify the size of an array property.
 *
 * This annotation can be used to define the size of an array either by referencing
 * another property that holds the size value or by directly specifying a fixed size.
 *
 * @property sizeFieldName The name of the property that contains the size of the array.
 *                         If not specified, the `size` parameter must be used.
 * @property size The fixed size of the array. Defaults to -1, indicating that the size
 *                should be determined by `sizeFieldName`.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ArraySize(val sizeFieldName: String = "", val size: Int = -1)

/**
 * Indicates that the annotated property should be decoded as a list of sub-blocks,
 * where the number of elements is determined by one field and the length of each
 * sub-block is determined by another.
 *
 * @param sizeFieldName Name of the property holding the sub-block count
 * @param lengthFieldName Name of the property holding each sub-block’s length
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubBlockList(val sizeFieldName: String, val lengthFieldName: String)