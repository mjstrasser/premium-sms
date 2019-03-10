package mjs.kotlin

import assertk.assertThat
import assertk.assertions.hasLength
import assertk.assertions.isNotZero
import mjs.kotlin.TraceUtil.hexId
import mjs.kotlin.TraceUtil.nextId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TraceUtilTest {

    @Nested
    inner class NextId {
        @Test
        fun `should never be zero`() {
            repeat(1_000_000) {
                assertThat(nextId()).isNotZero()
            }
        }
    }

    @Nested
    inner class HexId {
        @Test
        fun `should all be 16 characters long`() {
            repeat(1_000) {
                assertThat(hexId(nextId())).hasLength(16)
            }
        }
    }
}

