package mjs.kotlin

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotZero
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
                assertThat(TraceUtil.nextId()).isNotZero()
            }
        }
    }

    @Nested
    inner class HexId {
        @Test
        fun `should convert one-byte value`() {
            assertThat(TraceUtil.hexId(139L)).isEqualTo("0000008b")
        }
    }
}

