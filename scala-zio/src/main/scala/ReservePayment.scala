package mjs.premsms

import accounts.AccountError

import zio.ZIO

def reservePayment(data: PremiumSmsData): ZIO[
  Any,
  AccountError | Throwable,
  PremiumSmsData
] =
  for
    account <- data.sender.account.reserveCharge(data.provider.cost)
  yield PremiumSmsData(
    data.request,
    data.sender.copy(account = account),
    data.provider,
  )
