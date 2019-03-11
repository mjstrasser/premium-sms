package mjs.kotlin.sms

data class PremiumSmsService(
    val name: String,
    val description: String,
    val serviceMsisdn: Msisdn,
    val charge: Int,
    val providerEndpoint: String
)

// TODO: replace with database lookup
val SERVICE_CHARGES = mapOf(
    "191191" to PremiumSmsService(
        "EuroVision2019",
        "Voting for EuroVision",
        "191191",
        55,
        "https://euro.vision/voting"
    ),
    "190000" to PremiumSmsService(
        "RandomDonate5",
        "Donate $5 to a random charity",
        "190000",
        500,
        "https://random-charity.com/donate"
    )
)

class UnknownServiceException(message: String) : Exception(message)

suspend fun premiumSmsService(serviceMsisdn: Msisdn): PremiumSmsService {
    return SERVICE_CHARGES[serviceMsisdn]
        ?: throw UnknownServiceException("No premium SMS service for $serviceMsisdn")
}