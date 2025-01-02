package mjs.premsms

import accounts.InsufficientPrepaidFundsError
import providers.TestProvidersRepo
import senders.TestSendersRepo

import zio.http.{Client, TestClient}
import zio.Exit
import zio.test.{Spec, ZIOSpecDefault, assertTrue}

object SendPremiumSmsTests extends ZIOSpecDefault:

  def spec: Spec[Any, Any] =
    suite("Send premium SMS tests")(
      suite("sender with postpaid account")(
        test("successfully sends premium SMS if old enough and permitted") {
          for
            _ <- setDateAtMidday(2025, 1, 1)
            request <- testRequest("Test 1980-Postpaid--yes", "Test provider 0.55/0")
            response <- sendPremiumSms(request)
          yield assertTrue(response == simpleResponse(request, 0.55))
        },
        test("fails to send premium SMS if sender is not permitted to send premium SMS") {
          for
            _ <- setDateAtMidday(2025, 1, 1)
            request <- testRequest("Test 1980-Postpaid--no", "Test provider 0.55/0")
            exit <- sendPremiumSms(request).exit
          yield assertTrue(exit == Exit.fail(PremiumSmsDisallowedError))
        },
        test("fails to send premium SMS if sender is younger than minimum age for the service") {
          for
            _ <- setDateAtMidday(2025, 1, 1)
            request <- testRequest("Test 2010-Postpaid--yes", "Test provider 0.55/18")
            exit <- sendPremiumSms(request).exit
          yield assertTrue(exit == Exit.fail(UnderageError))
        },
      ),
      suite("sender with prepaid account")(
        test("successfully sends premium SMS if old enough, permitted and has sufficient funds") {
          for
            _ <- setDateAtMidday(2025, 1, 1)
            request <- testRequest("Test 2010-Prepaid-50.00-yes", "Test provider 0.55/0")
            response <- sendPremiumSms(request)
          yield assertTrue(response == simpleResponse(request, 0.55))
        },
        test("fails to send premium SMS if sender is younger than minimum age for the service") {
          for
            _ <- setDateAtMidday(2025, 1, 1)
            request <- testRequest("Test 2010-Prepaid-50.00-yes", "Test provider 0.55/18")
            exit <- sendPremiumSms(request).exit
          yield assertTrue(exit == Exit.fail(UnderageError))
        },
        test("fails to send premium SMS if sender is not permitted to send premium SMS") {
          for
            _ <- setDateAtMidday(2025, 1, 1)
            request <- testRequest("Test 1980-Prepaid-50.00-no", "Test provider 0.55/0")
            exit <- sendPremiumSms(request).exit
          yield assertTrue(exit == Exit.fail(PremiumSmsDisallowedError))
        },
        test("fails to send premium SMS if sender has insufficient funds") {
          for
            _ <- setDateAtMidday(2025, 1, 1)
            request <- testRequest("Test 2010-Prepaid-5.00-yes", "Test provider 5.50/0")
            exit <- sendPremiumSms(request).exit
          yield assertTrue(exit == Exit.fail(InsufficientPrepaidFundsError))
        },
      )
    ).provide(
      TestSendersRepo.layer,
      TestProvidersRepo.layer,
      TestClient.layer,
    )
