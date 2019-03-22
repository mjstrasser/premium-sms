package mjs.kotlin.tracing

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.http.HeadersBuilder
import io.ktor.util.AttributeKey
import mjs.kotlin.tracing.ZipkinIds.Feature.traceAndSpanKey

class ZipkinClientIds {

    class Configuration

    companion object Feature : HttpClientFeature<Configuration, ZipkinClientIds> {

        override val key = AttributeKey<ZipkinClientIds>("ZipkinClientIds")

        override fun prepare(block: Configuration.() -> Unit): ZipkinClientIds = ZipkinClientIds()

        override fun install(feature: ZipkinClientIds, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                scope.attributes.getOrNull(traceAndSpanKey)?.let { traceAndSpan ->
                    setClientHeaders(context.headers, traceAndSpan)
                }
            }
        }

        private fun setClientHeaders(headers: HeadersBuilder, traceAndSpan: TraceAndSpan) {
            headers.apply {
                append(TRACE_ID_HEADER, traceAndSpan.traceId)
                append(PARENT_SPAN_ID_HEADER, traceAndSpan.spanId)
                append(SPAN_ID_HEADER, nextId())
            }
        }
    }
}
