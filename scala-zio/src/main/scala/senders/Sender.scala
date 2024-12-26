package mjs.premsms
package senders

import java.time.LocalDate
import java.util.UUID

case class Sender(id: UUID, msisdn: String, name: String, dob: LocalDate, usePremiumSms: Boolean)
