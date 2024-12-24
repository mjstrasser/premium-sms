package mjs.premsms

import zio.Exit
import zio.test.*

object ValidateRequestTests extends ZIOSpecDefault {
  def spec: Spec[Any, Nothing] = suite("ValidateRequest tests") (
    test("empty sender is invalid") {
      for {
        exit <- validateRequest(PremiumSmsRequest("", "191919", "Number 4")).exit
      } yield assertTrue(exit == Exit.fail(InvalidRequestError("Empty sender")))
    },
    test("empty recipient is invalid") {
      for {
        exit <- validateRequest(PremiumSmsRequest("614123456", "", "Number 4")).exit
      } yield assertTrue(exit == Exit.fail(InvalidRequestError("Empty recipient")))
    },
    test("empty message is invalid") {
      for {
        exit <- validateRequest(PremiumSmsRequest("614123456", "191919", "")).exit
      } yield assertTrue(exit == Exit.fail(InvalidRequestError("Empty message")))
    }
  )
}
