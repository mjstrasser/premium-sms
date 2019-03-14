package mjs.kotlin.tracing

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.features.CallLogging
import io.ktor.http.Headers
import io.ktor.response.ApplicationResponse
import io.ktor.response.header
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase
import kotlin.random.Random

const val B3_HEADER = "b3"
const val TRACE_ID_HEADER = "X-B3-TraceId"
const val SPAN_ID_HEADER = "X-B3-SpanId"

private val prng = Random(System.nanoTime())

enum class IdLength { ID_64_BITS, ID_128_BITS }

fun nextId(idLength: IdLength = IdLength.ID_64_BITS) = when (idLength) {
    IdLength.ID_64_BITS -> String.format("%016x", prng.nextLong())
    IdLength.ID_128_BITS -> String.format("%016x%016x", prng.nextLong(), prng.nextLong())
}

data class TraceAndSpan(val traceId: String, val spanId: String)

/**
 * Ktor feature that handles Zipkin headers for trace ID and span ID. It currently mimics the
 * behaviour of [Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth).
 *
 * When installed, the headers are handled as follows:
 *
 * - If the request contains an X-B3-TraceId header, set the same header into the response
 *   and set a new 64-bit ID into the X-B3-SpanId header into the response.
 *
 * - If the request does not contain an X-B3-TraceId header, set a new 64-bit ID
 *   into both the X-B3-TraceId and X-B3-SpanId headers.
 */
class ZipkinIds {

    class Configuration {
        var b3Header = false
    }

    /**
     * Installable feature for [ZipkinIds].
     */
    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, ZipkinIds> {

        override val key = AttributeKey<ZipkinIds>("ZipkinIds")

        /**
         * Phase of [ApplicationCallPipeline] into which the feature is installed.
         */
        private val phase: PipelinePhase = PipelinePhase("ZipkinIds")

        internal val traceAndSpanKey = AttributeKey<TraceAndSpan>("traceAndSpan")

        private fun setHeaders(response: ApplicationResponse, traceAndSpan: TraceAndSpan, b3Header: Boolean) =
            if (b3Header) {
                response.header(B3_HEADER, "${traceAndSpan.traceId}-${traceAndSpan.spanId}")
            } else {
                response.header(TRACE_ID_HEADER, traceAndSpan.traceId)
                response.header(SPAN_ID_HEADER, traceAndSpan.spanId)
            }

        private fun readHeaders(headers: Headers): TraceAndSpan {
            val b3 = headers[B3_HEADER]
            val traceId = headers[TRACE_ID_HEADER]
            return b3?.let {
                TraceAndSpan(b3.split("-")[0], nextId())
            } ?: traceId?.let {
                TraceAndSpan(traceId, nextId())
            } ?: nextId().let { newId ->
                TraceAndSpan(newId, newId)
            }
        }

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): ZipkinIds {
            val configuration = Configuration().apply(configure)
            val instance = ZipkinIds()
            pipeline.insertPhaseBefore(ApplicationCallPipeline.Setup, phase)

            pipeline.intercept(phase) {
                val call = call

                val traceAndSpan = readHeaders(call.request.headers)

                call.attributes.put(traceAndSpanKey, traceAndSpan)

                setHeaders(call.response, traceAndSpan, configuration.b3Header)
            }

            return instance
        }
    }
}

/**
 * A [TraceAndSpan] that is retrieved or or set by [ZipkinIds] feature or `null`.
 */
val ApplicationCall.traceAndSpan: TraceAndSpan? get() = attributes.getOrNull(ZipkinIds.traceAndSpanKey)

/**
 * Keys for Slf4j MDC.
 */
const val TRACE_ID_KEY = "traceId"
const val SPAN_ID_KEY = "spanId"

/**
 * Put the Zipkin IDs into the logging MDC.
 */
fun CallLogging.Configuration.zipkinMdc() {
    mdc(TRACE_ID_KEY) { it.traceAndSpan?.traceId }
    mdc(SPAN_ID_KEY) { it.traceAndSpan?.spanId }
}
