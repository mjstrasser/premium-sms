package mjs.kotlin

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.callIdMdc
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import mjs.kotlin.sms.MOMessage
import mjs.kotlin.sms.processMoMessage
import mjs.kotlin.tracing.TraceUtil.nextId
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("UNUSED_PARAMETER")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(CallId) {
        generate { nextId() }
        header("X-B3-TraceId")
    }

    install(CallLogging) {
        level = Level.INFO
        callIdMdc()
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        get("/") {
            call.respond(mapOf("status" to "UP"))
        }

        post("/api/v1/premium-sms") {
            try {
                val moMessage = call.receive<MOMessage>()
                val result = processMoMessage(moMessage)
                call.respond(HttpStatusCode.Accepted, result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "There was an error processing your request")
            }
        }
    }
}

