package premiumSms.journey

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readBytes
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import mjs.ktor.features.zipkin.nextId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.text.DateFormat
import java.time.Instant

data class MoMessage(val from: String, val to: String, val text: String)
data class MoResponse(val moMessage: MoMessage, val id: String, val timestamp: Instant)

@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JourneyTest {

    private val client = HttpClient(CIO)
    private val json = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        dateFormat = DateFormat.getDateInstance()
    }

    @Test
    fun `an MO message should be echoed`() {
        runBlocking {
            val b3Header = "${nextId()}-${nextId()}-1"
            val message = MoMessage("61412345678", "190000", "JourneyTest")
            val response = client.post<HttpResponse>("http://main-service:8080/api/v1/premium-sms") {
                headers.append("b3", b3Header)
                body = TextContent(
                    json.writeValueAsString(message),
                    contentType = ContentType.Application.Json
                )
            }
            assertThat(response.headers["b3"]).isEqualTo(b3Header)
            val body = json.readValue<MoResponse>(response.readBytes())
            assertThat(body.moMessage).isEqualTo(message)
        }
    }
}
