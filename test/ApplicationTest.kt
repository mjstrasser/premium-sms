package mjs.kotlin

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest {

    @Test
    fun `a root request should return a health check`() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertThat(HttpStatusCode.OK).isEqualTo(response.status())
                assertThat("""{"status":"UP"}""").isEqualTo(response.content)
            }
        }
    }

    @Test
    fun `an MO message should be echoed`() {
        val moMessage = """{"from":"61412345678","to":"1991119","text":"This is a test"}"""
        withTestApplication({ module(testing = true) }) {
            with(handleRequest(HttpMethod.Post, "/api/v1/premium-sms") {
                addHeader("Content-Type", "application/json")
                setBody(moMessage)
            }) {
                assertThat(HttpStatusCode.OK).isEqualTo(response.status())
                assertThat(moMessage).isEqualTo(response.content)
            }
        }
    }
}
