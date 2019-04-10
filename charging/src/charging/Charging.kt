package premiumSms.charging.charging

import kotlinx.coroutines.delay
import mjs.kotlin.sms.Msisdn
import org.slf4j.LoggerFactory
import premiumSms.charging.Charge
import java.time.Instant
import java.util.*

val logger = LoggerFactory.getLogger("Charging")

data class ChargeResult(val amount: Int, val id: UUID, val timestamp: Instant)

class InsufficientFundsException(message: String) : Exception(message)

suspend fun processCharge(charge: Charge): ChargeResult {
    logger.info("Processing charge $charge")
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

suspend fun hasSufficientFunds(msisdn: Msisdn, amount: Int): Boolean {
    delay(20)
    return (msisdn.substring(8).toInt() + amount) % 3 > 0
}

suspend fun applyPostPaidCharge(amount: Int) {
    logger.info("Applying post-paid charge of $amount cents")
    delay(30)
}

suspend fun applyPrepaidCharge(amount: Int) {
    logger.info("Applying pre-paid charge of $amount cents")
    delay(100)
}

fun isPostPaid(msisdn: Msisdn) = msisdn.substring(4).toInt() % 2 == 0
