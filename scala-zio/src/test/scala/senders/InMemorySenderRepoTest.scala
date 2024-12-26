package mjs.premsms
package senders

import zio.test.{Spec, ZIOSpecDefault, assertTrue}
import zio.{Random, ZIO}

import java.time.LocalDate

object InMemorySenderRepoTest extends ZIOSpecDefault {

  def spec: Spec[Any, Throwable] = suite("InMemorySenderRepo tests")(
    suite("save function")(
      test("returns the MSISDN of the saved sender") {
        val msisdn = "61412345678"
        for
          id <- Random.nextUUID
          sender = Sender(
            id = id,
            msisdn = msisdn,
            name = "Samuel Clemens",
            dob = LocalDate.of(1973, 4, 1),
            usePremiumSms = true
          )
          repo <- ZIO.service[SenderRepo]
          saved <- repo.save(sender)
        yield assertTrue(saved == msisdn)
      }
    ),
    suite("findByMsisdn function")(
      test("returns empty for an unknown MSISDN") {
        val msisdn = "61412345678"
        for
          repo <- ZIO.service[SenderRepo]
          found <- repo.findByMsisdn(msisdn)
        yield assertTrue(found.isEmpty)
      },
      test("returns a sender for a known MSISDN") {
        val msisdn = "61412345678"
        for
          id <- Random.nextUUID
          sender = Sender(
            id = id,
            msisdn = msisdn,
            name = "Samuel Clemens",
            dob = LocalDate.of(1973, 4, 1),
            usePremiumSms = true
          )
          repo <- ZIO.service[SenderRepo]
          _ <- repo.save(sender)
          found <- repo.findByMsisdn(msisdn)
        yield assertTrue(found.get == sender)
      }
    )
  ).provide(
    InMemorySenderRepo.layer
  )
}
