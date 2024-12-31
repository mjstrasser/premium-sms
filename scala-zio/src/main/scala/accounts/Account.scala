package mjs.premsms
package accounts

import zio.{IO, ZIO}

import java.util.UUID

trait Account:

  def reserveCharge(charge: BigDecimal): IO[AccountError, Account]

case class PrepaidAccount(id: UUID,
                          senderId: UUID,
                          balance: BigDecimal) extends Account {

  override def reserveCharge(charge: BigDecimal): IO[AccountError, PrepaidAccount] = {
    val newBalance = balance - charge
    if newBalance >= BigDecimal(0.00) then
      ZIO.succeed(PrepaidAccount(id, senderId, newBalance))
    else
      ZIO.fail(InsufficientBalanceError)
  }
}

case class PostpaidAccount(id: UUID,
                           senderId: UUID) extends Account {

  override def reserveCharge(charge: BigDecimal): IO[AccountError, PostpaidAccount] =
    ZIO.succeed(this)
}
