package mjs.premsms
package accounts

import zio.test.{Spec, ZIOSpecDefault, assertTrue}
import zio.{Exit, Random, Task}

def testAccount(balance: BigDecimal): Task[PrepaidAccount] =
  for
    id <- Random.nextUUID
    senderId <- Random.nextUUID
  yield PrepaidAccount(id, senderId, balance)

object PrepaidAccountTests extends ZIOSpecDefault {

  def spec: Spec[Any, Any] = suite("Prepaid account tests")(
    suite("reserveCharge function")(
      test("succeeds with sufficient reserved funds") {
        for
          account <- testAccount(10.00)
          reserved <- account.reserveCharge(0.55)
        yield assertTrue(reserved.balance == BigDecimal(9.45))
      },
      test("fails with insufficient reserved funds") {
        for
          account <- testAccount(5.00)
          exit <- account.reserveCharge(5.50).exit
        yield assertTrue(exit == Exit.fail(InsufficientBalanceError))
      }
    )
  )

}
