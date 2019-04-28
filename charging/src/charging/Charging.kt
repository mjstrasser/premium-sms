package premiumSms.charging.charging

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import premiumSms.charging.Charge
import premiumSms.charging.Msisdn
import java.time.Instant
import java.util.*

val logger = LoggerFactory.getLogger("Charging")

data class ChargeResult(val amount: Int, val id: UUID, val timestamp: Instant)

class InsufficientFundsException(message: String) : Exception(message)

suspend fun processCharge(charge: Charge): ChargeResult {
    logger.info("Processing $charge")
    val id = UUID.randomUUID()
    val timestamp = Instant.now()

    if (isPostPaid(charge.msisdn)) {
        applyPostPaidCharge(charge.amount)
        return ChargeResult(charge.amount, id, timestamp)
    }

    if (hasSufficientFunds(charge.msisdn, charge.amount)) {
        applyPrepaidCharge(charge.amount)
        return ChargeResult(charge.amount, id, timestamp)
    }

    logger.info("Service ${charge.msisdn} has insufficient funds")
    throw InsufficientFundsException(
        "Service ${charge.msisdn} has insufficient funds for a charge of ${charge.amount} cents"
    )
}

/**
 * A MSISDN is post-paid if the last digit is 0–4 inclusive.
 */
fun isPostPaid(msisdn: Msisdn) = msisdn.last() < '5'

suspend fun applyPostPaidCharge(amount: Int) {
    delay(30)
    logger.info("Applying post-paid charge of $amount cents")
}

/**
 * A MSISDN has sufficient funds if its last digit is 7, 8 or 9.
 */
@Suppress("UNUSED_PARAMETER")
suspend fun hasSufficientFunds(msisdn: Msisdn, amount: Int): Boolean {
    delay(20)
    return msisdn.last() > '6'
}

suspend fun applyPrepaidCharge(amount: Int) {
    delay(100)
    logger.info("Applying pre-paid charge of $amount cents")
}
