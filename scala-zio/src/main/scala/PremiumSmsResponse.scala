package mjs.premsms

import java.time.OffsetDateTime

case class PremiumSmsResponse(timestamp: OffsetDateTime,
                              sender: String,
                              recipient: String,
                              cost: BigDecimal)
