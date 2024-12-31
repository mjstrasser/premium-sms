package mjs.premsms

import providers.TestProvidersRepo
import senders.TestSendersRepo

import zio.test.{Spec, ZIOSpecDefault, assertTrue}

object ReservePaymentTests extends ZIOSpecDefault {

  def spec: Spec[Any, Any] = suite("reservePayment function")(
    test("succeeds and returns the same data for a postpaid account") {
      for
        data <- testData("Test 1980-Postpaid--yes", "Test provider 0.55/0")
        reserved <- reservePayment(data)
      yield assertTrue(reserved == data)
    },
    test("succeeds if sufficient funds in a prepaid account") {
      for
        data <- testData("Test 2010-Prepaid-50.00-yes", "Test provider 0.55/0")
        reserved <- reservePayment(data)
      yield assertTrue(reserved.sender.account.currentBalance == BigDecimal(49.45))
    }
  ).provide(
    TestSendersRepo.layer,
    TestProvidersRepo.layer
  )

}
