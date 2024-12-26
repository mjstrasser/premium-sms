package mjs.premsms

import senders.{InMemorySenderRepo, Sender, SenderRepo}

import zio.test.{Spec, TestClock, ZIOSpecDefault, assertTrue}
import zio.{Clock, Exit, Random, ZIO}

import java.time.{LocalDate, ZoneOffset}

val today: LocalDate = LocalDate.of(2024, 12, 26)

def testSender(age: Int, usePremiumSms: Boolean): ZIO[SenderRepo, Throwable, Sender] =
  for
    id <- Random.nextUUID
    sender = Sender(id, "61412345678", "Joe Blow", today.minusYears(age), usePremiumSms)
    _ <- TestClock.setTimeZone(ZoneOffset.UTC)
    _ <- TestClock.setTime(today.atTime(12, 0, 0).toInstant(ZoneOffset.UTC))
    repo <- ZIO.service[SenderRepo]
    _ <- repo.save(sender)
  yield sender

def testRequest(sender: Sender): ZIO[SenderRepo, Throwable, PremiumSmsRequest] =
  for
    timestamp <- Clock.currentDateTime
  yield PremiumSmsRequest(timestamp, sender.msisdn, "191919", "Number 5")

object CheckPermissionsTests extends ZIOSpecDefault {
  def spec: Spec[Any, Throwable] = suite("CheckPermissionsTests")(
    suite("youngerThan18 function")(
      test("returns true for senders before their 18th birthday") {
        for
          sender <- testSender(age = 17, usePremiumSms = true)
          isYounger <- youngerThan18(sender)
        yield assertTrue(isYounger)
      },
      test("returns false for senders on their 18th birthday") {
        for
          sender <- testSender(age = 18, usePremiumSms = true)
          isYounger <- youngerThan18(sender)
        yield assertTrue(!isYounger)
      },
      test("returns false for senders after their 18th birthday") {
        for
          sender <- testSender(age = 19, usePremiumSms = true)
          isYounger <- youngerThan18(sender)
        yield assertTrue(!isYounger)
      }
    ),
    suite("checkPermissions function")(
      test("succeeds for a known, over-age sender who uses premium SMS") {
        for
          sender <- testSender(age = 25, usePremiumSms = true)
          request <- testRequest(sender)
          next <- checkPermissions(request).exit
        yield assertTrue(next == Exit.succeed(request))
      },
      test("fails with SenderUnknownError if sender is not in the database") {
        for
          timestamp <- Clock.currentDateTime
          request = PremiumSmsRequest(timestamp, "61499999999", "191919", "Number 5")
          exit <- checkPermissions(request).exit
        yield assertTrue(exit == Exit.fail(SenderUnknownError))
      },
      test("fails with UnderageError if sender is under 18") {
        for
          sender <- testSender(age = 17, usePremiumSms = true)
          request <- testRequest(sender)
          exit <- checkPermissions(request).exit
        yield assertTrue(exit == Exit.fail(UnderageError))
      },
      test("fails with PremiumSmsDisallowedError if sender has it disabled") {
        for
          sender <- testSender(age = 25, usePremiumSms = false)
          request <- testRequest(sender)
          exit <- checkPermissions(request).exit
        yield assertTrue(exit == Exit.fail(PremiumSmsDisallowedError))
      }
    )
  ).provide(
    InMemorySenderRepo.layer
  )
}