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
import io.ktor.server.cio.EngineMain
import mjs.kotlin.features.ZipkinIds
import mjs.kotlin.features.zipkinMdc
import mjs.kotlin.sms.MOMessage
import mjs.kotlin.sms.processMoMessage
import org.slf4j.event.Level
import java.text.DateFormat

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("UNUSED_PARAMETER")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(ZipkinIds) {
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
        get("/health") {
            call.respond(mapOf("status" to "UP"))
        }

        post("/api/v1/premium-sms") {
            try {
                val moMessage = call.receive<MOMessage>()
                val result = call.processMoMessage(moMessage)
                call.respond(HttpStatusCode.Accepted, result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "There was an error processing your request")
            }
        }
    }
}

