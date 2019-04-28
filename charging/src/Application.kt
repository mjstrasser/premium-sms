package premiumSms.charging

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
import mjs.ktor.features.zipkin.ZipkinIds
import mjs.ktor.features.zipkin.zipkinMdc
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import premiumSms.charging.charging.processCharge
import java.text.DateFormat

val logger = LoggerFactory.getLogger("Charging")

fun main(args: Array<String>): Unit = EngineMain.main(args)

data class Charge(val msisdn: Msisdn, val chargeId: Msisdn, val amount: Int)

typealias Msisdn = String

fun Application.module() {

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
            dateFormat = DateFormat.getDateInstance()
        }
    }

    routing {
        get("/") {
            call.respond(mapOf("status" to "UP"))
        }

        post("/api/v1/charge") {
            try {
                val charge = call.receive<Charge>()
                logger.info("Received request $charge")
                val result = processCharge(charge)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "There was an error processing your request")
            }
        }
    }
}
