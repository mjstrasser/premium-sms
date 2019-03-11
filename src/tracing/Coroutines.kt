package mjs.kotlin.tracing

import mjs.kotlin.tracing.TraceUtil.nextId

const val TRACE_SPAN_ID_KEY = "TraceAndSpan"

data class TraceAndSpan(val traceId: String, val spanId: String)

suspend fun setTraceAndSpan(traceId: String, spanId: String) {
    // TODO: Put trace and span into coroutine context
    println(TraceAndSpan(traceId, spanId))
}

suspend fun getTraceAndSpan(): TraceAndSpan {
    // TODO: Get from coroutine context, if present
    return nextId().let { TraceAndSpan(it, it) }
}
