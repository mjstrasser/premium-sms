package mjs.premsms

import java.time.OffsetDateTime

case class PremiumSmsRequest(timestamp: OffsetDateTime, sender: String, recipient: String, message: String)

