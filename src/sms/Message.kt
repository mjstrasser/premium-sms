package mjs.kotlin.sms

typealias Msisdn = String

sealed class Message
data class MOMessage(val from: Msisdn, val to: Msisdn, val text: String): Message()
data class MTMessage(val from: Msisdn, val to: Msisdn, val text: String): Message()
