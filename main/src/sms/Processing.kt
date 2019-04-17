package premiumSms.sms

import io.ktor.application.ApplicationCall
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

val logger = LoggerFactory.getLogger("Processing")

data class MoResult(val moMessage: MOMessage, val id: UUID, val timestamp: Instant)

@KtorExperimentalAPI
suspend fun ApplicationCall.processMoMessage(moMessage: MOMessage): MoResult {
    val id = UUID.randomUUID()
    val timestamp = Instant.now()

    val service = premiumSmsService(moMessage.to)
    applyCharge(service, moMessage)

    logger.info("Processing service {}", service)
    return MoResult(moMessage, id, timestamp)
}

