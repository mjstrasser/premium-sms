package mjs.kotlin

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
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
import mjs.kotlin.tracing.IdLength
import mjs.kotlin.tracing.ZipkinIds
import mjs.kotlin.tracing.zipkinMdc
import org.slf4j.event.Level
import java.text.DateFormat

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("UNUSED_PARAMETER")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(ZipkinIds) {
        b3Header = true
        idLength = IdLength.ID_128_BITS
        initiateTracePathPrefixes = arrayOf("/api")
    }

    install(CallLogging) {
        level = Level.INFO
        zipkinMdc()
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            dateFormat = DateFormat.getInstance()
        }
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

