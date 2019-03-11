package mjs.kotlin.sms

import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

val logger = LoggerFactory.getLogger("Processing")

data class MoResult(val moMessage: MOMessage, val id: UUID, val timestamp: Instant)

suspend fun processMoMessage(moMessage: MOMessage): MoResult {
    val id = UUID.randomUUID()
    val timestamp = Instant.now()

    val service = premiumSmsService(moMessage.to)

    logger.info("Processing service {}", service)
    return MoResult(moMessage, id, timestamp)
}

