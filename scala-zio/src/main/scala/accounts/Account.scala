package mjs.premsms
package accounts

import zio.ZIO

import java.util.UUID

enum AccountType:
  case Prepaid, Postpaid

case class Account(id: UUID,
                   senderId: UUID,
                   accountType: AccountType,
                   committedBalance: BigDecimal,
                   reservedBalance: BigDecimal) {

  def reserveCharge(charge: BigDecimal): ZIO[Any, AccountError, Account] = {
    val newBalance = reservedBalance - charge
    if newBalance >= BigDecimal(0.00) then
      ZIO.succeed(Account(id, senderId, accountType, committedBalance, newBalance))
    else
      ZIO.fail(InsufficientBalanceError)
  }
}
