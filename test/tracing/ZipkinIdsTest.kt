package tracing

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import mjs.kotlin.module
import mjs.kotlin.tracing.SPAN_ID_HEADER
import mjs.kotlin.tracing.TRACE_ID_HEADER
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ZipkinIdsTest {

    @Test
    fun `should set trace ID and span ID to the same value if no trace ID in request`(): Unit =
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                with(response.headers) {
                    assertThat(contains(TRACE_ID_HEADER)).isTrue()
                    assertThat(contains(SPAN_ID_HEADER)).isTrue()
                    assertThat(get(TRACE_ID_HEADER)).isEqualTo(get(SPAN_ID_HEADER))
                }
            }
        }

    @Test
    fun `should set a new span ID if there is a trace ID in request`(): Unit =
        withTestApplication({ module(testing = true) }) {
            val traceId = "a56524c38b3f0bfb"
            handleRequest(HttpMethod.Get, "/") {
                addHeader(TRACE_ID_HEADER, traceId)
                addHeader(SPAN_ID_HEADER, traceId)
            }.apply {
                with(response.headers) {
                    assertThat(contains(TRACE_ID_HEADER)).isTrue()
                    assertThat(contains(SPAN_ID_HEADER)).isTrue()
                    assertThat(get(TRACE_ID_HEADER)).isNotEqualTo(get(SPAN_ID_HEADER))
                    assertThat(get(TRACE_ID_HEADER)).isEqualTo(traceId)
                }
            }
        }

}
