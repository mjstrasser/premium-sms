package mjs.kotlin

import kotlin.random.Random

val prng = Random(System.nanoTime())

object TraceUtil {

    /**
     * Returns the next ID, ensuring it is non-zero.
     */
    fun nextLong() = prng.nextLong().let {
        if (it == 0L) prng.nextLong() else it
    }

    fun hexId(id: Long): String {
        return String.format("%016x", id)
    }

    fun nextId() = hexId(nextLong())
}
