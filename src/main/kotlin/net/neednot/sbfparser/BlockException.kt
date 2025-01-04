package net.neednot.sbfparser

open class BlockException(message: String, val data: ByteArray): Exception(message)

class InvalidBlockException(message: String, data: ByteArray): BlockException(message, data)
class IncompleteBlockException(message: String, data: ByteArray): BlockException(message, data)