package premiumSms.sms

import io.ktor.application.ApplicationCall
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import mjs.kotlin.features.ClientIds
import mjs.kotlin.features.ZipkinIds

@KtorExperimentalAPI
@Suppress("UNUSED_PARAMETER")
suspend fun ApplicationCall.applyCharge(service: PremiumSmsService, moMessage: MOMessage) {

    val client = HttpClient(CIO) {
        attributes.getOrNull(ZipkinIds.traceAndSpanKey)?.let { it ->
            logger.info("Installing ClientIds for $it")
            install(ClientIds) {
                traceAndSpan = it
            }
        }
    }
    val response = client.get<String>("http://localhost:8081/health")
    logger.info("Charging health response: $response")

}