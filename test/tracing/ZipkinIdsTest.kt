package mjs.kotlin.tracing

import assertk.assertThat
import assertk.assertions.*
import io.ktor.application.install
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import mjs.kotlin.tracing.ZipkinIds.Feature.foundPrefixMatch
import mjs.kotlin.tracing.ZipkinIds.Feature.traceAndSpanKey
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ZipkinIdsTest {

    @Nested
    inner class IdGeneration {
        @Test
        fun `should generate a 64-bit ID by default`() {
            assertThat(nextId()).hasLength(16)
        }

        @Test
        fun `should generate a 128-bit ID when specified`() {
            assertThat(nextId(IdLength.ID_128_BITS)).hasLength(32)
        }
    }


    @Nested
    inner class PathPrefixes {
        @Test
        fun `should return false if a path does not match any of the prefixes`() {
            assertThat(foundPrefixMatch("/health", arrayOf("/api", "/login", "/logout"))).isFalse()
        }

        @Test
        fun `should return true if a path matches one of the prefixes`() {
            assertThat(foundPrefixMatch("/api/v1/premium-sms", arrayOf("/api/", "/login", "/logout"))).isTrue()
        }

        @Test
        fun `should initiate a trace if the path starts with a specified prefix`(): Unit = withTestApplication {
            application.install(ZipkinIds) {
                initiateTracePathPrefixes = arrayOf("/api")
            }
            handleRequest(HttpMethod.Post, "/api/v1/premium-sms").apply {
                assertThat(request.call.attributes.contains(traceAndSpanKey)).isTrue()
            }
        }

        @Test
        fun `should not initiate a trace if the path does not start a specified prefix`(): Unit = withTestApplication {
            application.install(ZipkinIds) {
                initiateTracePathPrefixes = arrayOf("/api")
            }
            handleRequest(HttpMethod.Get, "/health").apply {
                assertThat(request.call.attributes.contains(traceAndSpanKey)).isFalse()
            }
        }
    }

    @Nested
    inner class HttpHeaders {
        @Test
        fun `headers should not be set if the feature is not installed`(): Unit = withTestApplication {
            handleRequest(HttpMethod.Get, "/").apply {
                with(response.headers) {
                    assertThat(contains(TRACE_ID_HEADER)).isFalse()
                    assertThat(contains(SPAN_ID_HEADER)).isFalse()
                }
            }
        }

        @Test
        fun `b3 header should be read if present`(): Unit = withTestApplication {
            application.install(ZipkinIds)
            val traceId = nextId()
            val spanId = nextId()
            handleRequest(HttpMethod.Get, "/") {
                addHeader(B3_HEADER, "$traceId-$spanId")
            }.apply {
                with(response.headers) {
                    assertThat(contains(TRACE_ID_HEADER)).isTrue()
                    assertThat(contains(SPAN_ID_HEADER)).isTrue()
                }
            }
        }

        @Test
        fun `X-B3-TraceId and X-B3-SpanId headers should be read if present`(): Unit = withTestApplication {
            application.install(ZipkinIds)
            val traceId = nextId()
            val spanId = nextId()
            handleRequest(HttpMethod.Get, "/") {
                addHeader(TRACE_ID_HEADER, traceId)
                addHeader(SPAN_ID_HEADER, spanId)
            }.apply {
                with(response.headers) {
                    assertThat(contains(TRACE_ID_HEADER)).isTrue()
                    assertThat(contains(SPAN_ID_HEADER)).isTrue()
                }
            }
        }

        @Test
        fun `should set trace ID and span ID to the same value if no X-B3-TraceId header in request`(): Unit =
            withTestApplication {
                application.install(ZipkinIds)
                handleRequest(HttpMethod.Get, "/").apply {
                    with(response.headers) {
                        assertThat(contains(TRACE_ID_HEADER)).isTrue()
                        assertThat(contains(SPAN_ID_HEADER)).isTrue()
                        assertThat(get(TRACE_ID_HEADER)).isEqualTo(get(SPAN_ID_HEADER))
                    }
                }
            }

        @Test
        fun `should set a new span ID if there is a trace ID in request`(): Unit = withTestApplication {
            application.install(ZipkinIds)
            val traceId = nextId()
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

    @Nested
    inner class CallAttributes {
        @Test
        fun `should not be set if the feature is not installed`(): Unit = withTestApplication {
            handleRequest(HttpMethod.Get, "/").apply {
                assertThat(request.call.attributes.contains(traceAndSpanKey)).isFalse()
            }
        }

        @Test
        fun `should be set if the feature is installed`(): Unit = withTestApplication {
            application.install(ZipkinIds)
            val traceId = nextId(IdLength.ID_128_BITS)
            val spanId = nextId()
            handleRequest(HttpMethod.Get, "/") {
                addHeader(TRACE_ID_HEADER, traceId)
                addHeader(SPAN_ID_HEADER, spanId)
            }.apply {
                with(request.call.attributes) {
                    assertThat(contains(traceAndSpanKey)).isTrue()
                    // Only compare trace ID because span ID is set to a new value.
                    get(traceAndSpanKey).also { id ->
                        assertThat(id.traceId).isEqualTo(traceId)
                    }
                }
            }
        }
    }
}
