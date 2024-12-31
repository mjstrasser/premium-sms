package mjs.premsms
package senders

import accounts.{Account, AccountType}

import java.time.LocalDate
import java.util.UUID

case class Sender(id: UUID,
                  msisdn: String,
                  name: String,
                  dob: LocalDate,
                  accountType: AccountType,
                  account: Account,
                  usePremiumSms: Boolean)
