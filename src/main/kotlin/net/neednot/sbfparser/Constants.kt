package net.neednot.sbfparser

val BLOCK_SYNC = byteArrayOf(36, 64)

val blockClassById: Map<Int, Class<out BlockBody>> = mapOf(
    4028 to BaseVectorGeod::class.java,
    4094 to PosProjected::class.java,
    4082 to QualityInd::class.java
)