package premiumSms.sms

import io.ktor.application.ApplicationCall
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import mjs.ktor.features.zipkin.ClientIds
import mjs.ktor.features.zipkin.ZipkinIds

@KtorExperimentalAPI
@Suppress("UNUSED_PARAMETER")
suspend fun ApplicationCall.applyCharge(service: PremiumSmsService, moMessage: MOMessage) {

    val client = HttpClient(CIO) {
        attributes.getOrNull(ZipkinIds.tracingPartsKey)?.let { it ->
            logger.info("Installing ClientIds for $it")
            install(ClientIds) {
                tracingParts = it
            }
        }
    }
    val response = client.get<String>("http://localhost:8081/health")
    logger.info("Charging health response: $response")

}