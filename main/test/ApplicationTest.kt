package mjs.kotlin

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import premiumSms.module

@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest {

    @Test
    fun `a root request should return a health check`(): Unit = withTestApplication({ module() }) {
        handleRequest(HttpMethod.Get, "/").apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
            assertThat(response.content).isEqualTo("""{"status":"UP"}""")
        }
    }
}
