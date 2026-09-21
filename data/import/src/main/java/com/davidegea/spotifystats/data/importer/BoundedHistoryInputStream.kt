package com.davidegea.spotifystats.data.importer

import java.io.FilterInputStream
import java.io.InputStream

/** A shared one-GiB decompressed budget covers an entire archive without materializing it. */
internal class BoundedHistoryInputStream(input: InputStream, private val maximum: Long = 1_073_741_824L) : FilterInputStream(input) {
    private var readBytes = 0L
    override fun read(): Int = `in`.read().also { if (it >= 0) count(1) }
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int = `in`.read(buffer, offset, length).also { if (it > 0) count(it) }
    private fun count(size: Int) { readBytes += size; require(readBytes <= maximum) { "History input exceeds the supported size" } }
}
