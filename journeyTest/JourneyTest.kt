package premiumSms.journey

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import mjs.ktor.features.zipkin.SAMPLED_HEADER
import mjs.ktor.features.zipkin.SPAN_ID_HEADER
import mjs.ktor.features.zipkin.TRACE_ID_HEADER
import mjs.ktor.features.zipkin.nextId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.text.DateFormat
import java.time.Instant
import kotlin.random.Random

data class MoMessage(val from: String, val to: String, val text: String)
data class MoResponse(val moMessage: MoMessage, val id: String, val timestamp: Instant)

private val prng = Random(System.currentTimeMillis())
private fun testMsisdn(lastChar: Char) = String.format("614%07d%c", prng.nextInt(100_000_000), lastChar)
private fun postPaidMsisdn() = testMsisdn('0' + prng.nextInt(0, 5))

@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JourneyTest {

    private val client = HttpClient(CIO)
    private val json = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        dateFormat = DateFormat.getDateInstance()
    }

    @Nested
    inner class TracingHeaders {
        @Test
        fun `a b3 header should be returned`() {
            runBlocking {
                val b3Header = "${nextId()}-${nextId()}-1"
                val message = MoMessage(postPaidMsisdn(), "190000", "JourneyTest")
                val response: HttpResponse = client.post("http://main-service:8080/api/v1/premium-sms") {
                    headers.append("b3", b3Header)
                    body = moMessageJson(message)
                }
                assertThat(response.headers["b3"]).isEqualTo(b3Header)
                val body = json.readValue<MoResponse>(response.readBytes())
                assertThat(body.moMessage).isEqualTo(message)
            }
        }

        @Test
        fun `a set of X-B3 headers should be returned`() {
            runBlocking {
                val traceId = nextId()
                val spanId = nextId()
                val message = MoMessage(postPaidMsisdn(), "190000", "JourneyTest")
                val response = client.post<HttpResponse>("http://main-service:8080/api/v1/premium-sms") {
                    headers.append(TRACE_ID_HEADER, traceId)
                    headers.append(SPAN_ID_HEADER, spanId)
                    headers.append(SAMPLED_HEADER, "1")
                    body = moMessageJson(message)
                }
                assertThat(response.headers[TRACE_ID_HEADER]).isEqualTo(traceId)
                assertThat(response.headers[SPAN_ID_HEADER]).isEqualTo(spanId)
                assertThat(response.headers[SAMPLED_HEADER]).isEqualTo("1")
                val body = json.readValue<MoResponse>(response.readBytes())
                assertThat(body.moMessage).isEqualTo(message)
            }
        }
    }

    private fun moMessageJson(message: MoMessage): TextContent {
        return TextContent(
            json.writeValueAsString(message),
            contentType = ContentType.Application.Json
        )
    }
}
