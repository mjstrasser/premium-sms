package mjs.premsms
package senders

import accounts.AccountType

import java.time.LocalDate
import java.util.UUID

case class Sender(id: UUID,
                  msisdn: String,
                  accountType: AccountType,
                  name: String,
                  dob: LocalDate,
                  usePremiumSms: Boolean)
