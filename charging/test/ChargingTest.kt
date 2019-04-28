package premiumSms.charging.charging

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.random.Random

val prng = Random(System.currentTimeMillis())
fun testMsisdn(lastChar: Char) = String.format("614%07d%c", prng.nextInt(100_000_000), lastChar)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ChargingTest {

    @Nested
    inner class IsPostPaid {
        @Test
        fun `should return MSISDNs with last digit of 0-4 as post-paid`() {
            assertThat(isPostPaid(testMsisdn('0'))).isTrue()
            assertThat(isPostPaid(testMsisdn('1'))).isTrue()
            assertThat(isPostPaid(testMsisdn('2'))).isTrue()
            assertThat(isPostPaid(testMsisdn('3'))).isTrue()
            assertThat(isPostPaid(testMsisdn('4'))).isTrue()
        }

        @Test
        fun `should return MSISDNs with last digit of 5-9 as pre-paid`() {
            assertThat(isPostPaid(testMsisdn('5'))).isFalse()
            assertThat(isPostPaid(testMsisdn('6'))).isFalse()
            assertThat(isPostPaid(testMsisdn('7'))).isFalse()
            assertThat(isPostPaid(testMsisdn('8'))).isFalse()
            assertThat(isPostPaid(testMsisdn('9'))).isFalse()
        }
    }

    @Nested
    inner class HasSufficientFunds {
        @Test
        fun `should return MSISDNs with last digit of 7-9 as having sufficient funds`() = runBlocking {
            assertThat(hasSufficientFunds(testMsisdn('7'), 55)).isTrue()
            assertThat(hasSufficientFunds(testMsisdn('8'), 500)).isTrue()
            assertThat(hasSufficientFunds(testMsisdn('9'), 240)).isTrue()
        }

        @Test
        fun `should return MSISDNs with last digit of 5 or 6 as having insufficient funds`() = runBlocking {
            assertThat(hasSufficientFunds(testMsisdn('5'), 55)).isFalse()
            assertThat(hasSufficientFunds(testMsisdn('6'), 500)).isFalse()
        }

    }
}