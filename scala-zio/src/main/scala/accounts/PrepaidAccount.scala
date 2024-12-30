package mjs.premsms
package accounts

import zio.{IO, ZIO}

import java.util.UUID

case class PrepaidAccount(id: UUID,
                          senderId: UUID,
                          balance: BigDecimal) {

  def reserveCharge(charge: BigDecimal): IO[AccountError, PrepaidAccount] = {
    val newBalance = balance - charge
    if newBalance >= BigDecimal(0.00) then
      ZIO.succeed(PrepaidAccount(id, senderId, newBalance))
    else
      ZIO.fail(InsufficientBalanceError)
  }
}
