package net.neednot.sbfparser

/**
 * Base exception class for handling block-related errors.
 *
 * @param message A descriptive message about the exception.
 * @param data The raw data associated with the block and after that caused the exception.
 */
open class BlockException(message: String, val data: ByteArray): Exception(message)

/**
 * Exception thrown when a block is invalid and cannot be processed.
 *
 * This is to indicate that the block data must be discarded.
 *
 * @param message A description of why the block is invalid.
 * @param data The raw data of the invalid block.
 */
class InvalidBlockException(message: String, data: ByteArray): BlockException(message, data)

/**
 * Exception thrown when a block is incomplete but may be completed later.
 *
 * When a block does not contain all necessary data for processing,
 * but the incomplete data is kept in a buffer for when new data comes in.
 *
 * @param message A description of why the block is incomplete.
 * @param data The raw data of the incomplete block.
 */
class IncompleteBlockException(message: String, data: ByteArray): BlockException(message, data)