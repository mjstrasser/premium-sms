package mjs.premsms

import zio.*
import zio.test.*

object ValidateRequestTests extends ZIOSpecDefault {
  def spec: Spec[Any, InvalidRequestError] = suite("ValidateRequest tests")(
    test("all present and correct") {
      for {
        timestamp <- Clock.currentDateTime
        request = PremiumSmsRequest(timestamp, "61412345678", "191919", "Number 4")
        next <- validateRequest(request)
      } yield assertTrue(next == request)
    },
    test("future timestamp is invalid") {
      for {
        timestamp <- Clock.currentDateTime
        _ <- TestClock.adjust(-10.seconds)
        exit <- validateRequest(PremiumSmsRequest(timestamp, "61412345678", "191919", "Number 4")).exit
      } yield assertTrue(exit == Exit.fail(InvalidRequestError("Timestamp is in the future")))
    },
    test("empty sender is invalid") {
      for {
        timestamp <- Clock.currentDateTime
        exit <- validateRequest(PremiumSmsRequest(timestamp, "", "191919", "Number 4")).exit
      } yield assertTrue(exit == Exit.fail(InvalidRequestError("Empty sender")))
    },
    test("empty recipient is invalid") {
      for {
        timestamp <- Clock.currentDateTime
        exit <- validateRequest(PremiumSmsRequest(timestamp, "61412345678", "", "Number 4")).exit
      } yield assertTrue(exit == Exit.fail(InvalidRequestError("Empty recipient")))
    },
    test("empty message is invalid") {
      for {
        timestamp <- Clock.currentDateTime
        exit <- validateRequest(PremiumSmsRequest(timestamp, "61412345678", "191919", "")).exit
      } yield assertTrue(exit == Exit.fail(InvalidRequestError("Empty message")))
    }
  )
}
