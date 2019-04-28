package premiumSms.sms

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.ApplicationCall
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.util.KtorExperimentalAPI
import mjs.ktor.features.zipkin.ClientIds
import mjs.ktor.features.zipkin.ZipkinIds

data class ChargeRequest(val msisdn: Msisdn, val chargeId: Msisdn, val amount: Int)

@KtorExperimentalAPI
@Suppress("UNUSED_PARAMETER")
suspend fun ApplicationCall.applyCharge(service: PremiumSmsService, moMessage: MOMessage) {

    fun chargingUrl(): String {
        val config = application.environment.config.config("ktor.charging")
        return "http://${config.property("host").getString()}:${config.property("port").getString()}/api/v1/charge"
    }

    val json = jacksonObjectMapper()
    try {
        val client = HttpClient(CIO) {
            attributes.getOrNull(ZipkinIds.tracingPartsKey)?.let { it ->
                logger.info("Installing ClientIds for $it")
                install(ClientIds) {
                    tracingParts = it
                }
            }
        }
        val url = chargingUrl()
        logger.info("Posting request to $url")
        val response = client.post<String>(url) {
            body = TextContent(
                json.writeValueAsString(ChargeRequest(moMessage.from, service.serviceMsisdn, service.charge)),
                contentType = ContentType.Application.Json
            )
        }
        logger.info("Charging response: $response")
    } catch (e: Exception) {
        logger.error("Client request failure", e)
    }
}
