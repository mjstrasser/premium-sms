package mjs.kotlin.tracing

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.features.CallLogging
import io.ktor.http.Headers
import io.ktor.response.header
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase
import mjs.kotlin.tracing.TraceUtil.nextId

data class TraceAndSpan(val traceId: String, val spanId: String)

class ZipkinIds {

    class Configuration

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, ZipkinIds> {

        override val key = AttributeKey<ZipkinIds>("ZipkinIds")

        private val phase: PipelinePhase = PipelinePhase("ZipkinIds")

        internal val traceAndSpanKey = AttributeKey<TraceAndSpan>("traceAndSpan")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): ZipkinIds {
            val instance = ZipkinIds()
            pipeline.insertPhaseBefore(ApplicationCallPipeline.Setup, phase)

            pipeline.intercept(phase) {
                val call = call

                val traceAndSpan = calculateTraceAndSpan(call.request.headers)

                call.attributes.put(traceAndSpanKey, traceAndSpan)

                call.response.header("X-B3-TraceId", traceAndSpan.traceId)
                call.response.header("X-B3-SpanId", traceAndSpan.spanId)
            }

            return instance
        }

        private fun calculateTraceAndSpan(headers: Headers): TraceAndSpan {
            return if (headers["X-B3-TraceId"] == null) {
                val traceId = nextId()
                TraceAndSpan(traceId, traceId)
            } else {
                val traceId = headers["X-B3-TraceId"]!!
                TraceAndSpan(traceId, nextId())
            }
        }
    }
}

val ApplicationCall.traceAndSpan: TraceAndSpan? get() = attributes.getOrNull(ZipkinIds.traceAndSpanKey)

fun CallLogging.Configuration.zipkinMdc() {
    mdc("traceId") { it.traceAndSpan?.traceId }
    mdc("spanId") { it.traceAndSpan?.spanId }
}
