package mjs.premsms

import providers.{Provider, ProviderRepo, TestProvidersRepo}
import senders.{InMemorySenderRepo, Sender, SenderRepo, TestSendersRepo}

import zio.test.{Spec, TestClock, ZIOSpecDefault, assertTrue}
import zio.{Clock, Exit, RIO, UIO, ZIO}

import java.time.{LocalDate, ZoneOffset}

val today: LocalDate = LocalDate.of(2024, 12, 26)

def testSender(name: String): RIO[SenderRepo, Sender] =
  for
    repo <- ZIO.service[SenderRepo]
    maybeSender <- repo.findByName(name)
    sender <- ZIO.getOrFail(maybeSender)
  yield sender

def testProvider(name: String): RIO[ProviderRepo, Provider] =
  for
    repo <- ZIO.service[ProviderRepo]
    maybeProvider <- repo.findByName(name)
    provider <- ZIO.getOrFail(maybeProvider)
  yield provider

def setDateAtMidday(year: Int, month: Int, day: Int): UIO[Unit] =
  for
    _ <- TestClock.setTimeZone(ZoneOffset.UTC)
    _ <- TestClock.setTime(LocalDate.of(year, month, day).atTime(12, 0, 0)
      .atZone(ZoneOffset.UTC).toInstant)
  yield ()

def testData(senderName: String, providerName: String): RIO[SenderRepo & ProviderRepo, PremiumSmsData] =
  for
    timestamp <- Clock.currentDateTime
    sender <- testSender(senderName)
    provider <- testProvider(providerName)
    request = PremiumSmsRequest(timestamp, sender.msisdn, provider.number, "Test request")
  yield PremiumSmsData(request, sender, provider)

object ValidateRequestTests extends ZIOSpecDefault {
  def spec: Spec[Any, Throwable] = suite("ValidateRequest tests")(
    suite("tooYoung function")(
      test("is true for an under-18 sender requesting a provider with minimum age 18") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          sender <- testSender("Test 2010-Prepaid-5.00-yes")
          provider <- testProvider("Test provider 0.55/18")
          isTooYoung <- tooYoung(sender, provider)
        yield assertTrue(isTooYoung)
      },
      test("is false for an under-18 sender requesting a provider with minimum age 0") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          sender <- testSender("Test 2010-Prepaid-5.00-yes")
          provider <- testProvider("Test provider 0.55/0")
          isTooYoung <- tooYoung(sender, provider)
        yield assertTrue(!isTooYoung)
      },
      test("is false for an over-18 sender requesting a provider with minimum age 18") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          sender <- testSender("Test 1980-Postpaid--yes")
          provider <- testProvider("Test provider 0.55/18")
          isTooYoung <- tooYoung(sender, provider)
        yield assertTrue(!isTooYoung)
      },
    ),
    suite("checkPermissions function")(
      test("fails with SenderUnknownError if sender is not in the database") {
        for
          timestamp <- Clock.currentDateTime
          request = PremiumSmsRequest(timestamp, "61499999999", "191919", "Number 5")
          exit <- validateRequest(request).exit
        yield assertTrue(exit == Exit.fail(SenderUnknownError))
      },
      test("succeeds for a known, old-enough sender who uses premium SMS") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          data <- testData("Test 1980-Postpaid--yes", "Test provider 0.55/0")
          exit <- validateRequest(data.request).exit
        yield assertTrue(exit == Exit.succeed(data))
      },
      test("fails with UnderageError if sender is under 18") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          data <- testData("Test 2010-Prepaid-5.00-yes", "Test provider 0.55/18")
          exit <- validateRequest(data.request).exit
        yield assertTrue(exit == Exit.fail(UnderageError))
      },
      test("fails with PremiumSmsDisallowedError if old-enough sender has premium SMS disabled") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          data <- testData("Test 1980-Postpaid--no", "Test provider 0.55/18")
          exit <- validateRequest(data.request).exit
        yield assertTrue(exit == Exit.fail(PremiumSmsDisallowedError))
      }
    )
  ).provide(
    TestSendersRepo.layer,
    TestProvidersRepo.layer
  )
}