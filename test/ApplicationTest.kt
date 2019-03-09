package mjs.kotlin

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `a root request should return a health check`() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("""{"status":"UP"}""", response.content)
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
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(moMessage, response.content)
            }
        }
    }
}
