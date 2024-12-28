package mjs.premsms

import providers.{Provider, ProviderRepo, TestProvidersRepo}
import senders.{InMemorySenderRepo, Sender, SenderRepo, TestSendersRepo}

import zio.test.{Spec, TestClock, ZIOSpecDefault, assertTrue}
import zio.{Clock, Exit, RIO, UIO, ZIO}

import java.time.{LocalDate, ZoneOffset}

val today: LocalDate = LocalDate.of(2024, 12, 26)

def testSender(msisdn: String): RIO[SenderRepo, Sender] =
  for
    repo <- ZIO.service[SenderRepo]
    maybeSender <- repo.findByMsisdn(msisdn)
    sender <- ZIO.getOrFail(maybeSender)
  yield sender

def testProvider(number: String): RIO[ProviderRepo, Provider] =
  for
    repo <- ZIO.service[ProviderRepo]
    maybeProvider <- repo.findByNumber(number)
    provider <- ZIO.getOrFail(maybeProvider)
  yield provider

def setDateAtMidday(year: Int, month: Int, day: Int): UIO[Unit] =
  for
    _ <- TestClock.setTimeZone(ZoneOffset.UTC)
    _ <- TestClock.setTime(LocalDate.of(year, month, day).atTime(12, 0, 0)
      .atZone(ZoneOffset.UTC).toInstant)
  yield ()

def testRequest(sender: Sender, number: String): RIO[SenderRepo, PremiumSmsRequest] =
  for
    timestamp <- Clock.currentDateTime
  yield PremiumSmsRequest(timestamp, sender.msisdn, number, "Test request")

object ValidateRequestTests extends ZIOSpecDefault {
  def spec: Spec[Any, Throwable] = suite("ValidateRequest tests")(
    suite("tooYoung function")(
      test("is true for an under-18 sender requesting a provider with minimum age 18") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          sender <- testSender("61400100202")
          provider <- testProvider("190001")
          isTooYoung <- tooYoung(sender, provider)
        yield assertTrue(isTooYoung)
      },
      test("is false for an under-18 sender requesting a provider with minimum age 0") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          sender <- testSender("61400100202")
          provider <- testProvider("190000")
          isTooYoung <- tooYoung(sender, provider)
        yield assertTrue(!isTooYoung)
      },
      test("is false for an over-18 sender requesting a provider with minimum age 18") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          sender <- testSender("61400100200")
          provider <- testProvider("190001")
          isTooYoung <- tooYoung(sender, provider)
        yield assertTrue(!isTooYoung)
      },
    ),
    suite("checkPermissions function")(
      test("succeeds for a known, old-enough sender who uses premium SMS") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          sender <- testSender("61400100200")
          request <- testRequest(sender, "190000")
          next <- validateRequest(request).exit
        yield assertTrue(next == Exit.succeed(request))
      },
      test("fails with SenderUnknownError if sender is not in the database") {
        for
          timestamp <- Clock.currentDateTime
          request = PremiumSmsRequest(timestamp, "61499999999", "191919", "Number 5")
          exit <- validateRequest(request).exit
        yield assertTrue(exit == Exit.fail(SenderUnknownError))
      },
      test("fails with UnderageError if sender is under 18") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          sender <- testSender("61400100202")
          request <- testRequest(sender, "190001")
          exit <- validateRequest(request).exit
        yield assertTrue(exit == Exit.fail(UnderageError))
      },
      test("fails with PremiumSmsDisallowedError if old-enough sender has premium SMS disabled") {
        for
          _ <- setDateAtMidday(2024, 12, 27)
          sender <- testSender("61400100201")
          request <- testRequest(sender, "190001")
          exit <- validateRequest(request).exit
        yield assertTrue(exit == Exit.fail(PremiumSmsDisallowedError))
      }
    )
  ).provide(
    TestSendersRepo.layer,
    TestProvidersRepo.layer
  )
}