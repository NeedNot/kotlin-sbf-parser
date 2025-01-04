package net.neednot.sbfparser

sealed interface BlockBody {
    val name: String
}

/** Block id 4082 */
@OptIn(ExperimentalUnsignedTypes::class)
data class QualityInd(
    override val name: String = "qualityInd",

    val n: UByte,
    val reserved: UByte,

    @StringLength("n")
    val indicators: UShortArray
): BlockBody