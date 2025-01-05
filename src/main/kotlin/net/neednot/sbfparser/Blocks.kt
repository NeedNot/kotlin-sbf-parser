package net.neednot.sbfparser

sealed interface BlockBody {
    val name: String
}

/** Block id 4082 */
@OptIn(ExperimentalUnsignedTypes::class)
data class QualityInd(
    override val name: String = "QualityInd",

    val n: UByte,
    val reserved: UByte,

    /**
     * Bits need to be interpreted as follows:
     *
     * 0-7: Quality indicator type
     *
     * 8-11: Quality indicator value
     *
     * 12-15: Reserved
     * */
    @ArrayLength("n")
    val indicators: UShortArray
): BlockBody

/** Block id 4094 */
data class PosProjected(
    override val name: String = "PosProjected",

    val mode: UByte,
    val error: UByte,
    val northing: Double,
    val easting: Double,
    val alt: Double,
    val datum: UByte

): BlockBody

/** Block id 4028 */
data class BaseVectorGeod(
    override val name: String = "BaseVectorGeod",

    val n: UByte,
    val sbLength: UByte,

//    sub block
    val nrSV: UByte,
    val error: UByte,
    val mode: UByte,
    val misc: UByte,
    val deltaEast: Double,
    val deltaNorth: Double,
    val deltaUp: Double,
    val deltaVe: Float,
    val deltaVn: Float,
    val deltaVu: Float,
    val azimuth: UShort,
    val elevation: Short,
    val referenceId: UShort,
    val corrAge: UShort,
    val signalInfo: UInt
): BlockBody