package mjs.premsms
package senders

import accounts.AccountType.{Postpaid, Prepaid}

import zio.{Ref, TaskLayer, ZLayer}

import java.time.LocalDate

object TestSendersRepo:

  def layer: TaskLayer[InMemorySenderRepo] =
    ZLayer.fromZIO(
      for
        repo <- Ref.make(Map.empty[String, Sender]).map(InMemorySenderRepo(_))
        _ <- repo.save("61400100200", "Test 1980-Postpaid--yes", LocalDate.of(1980, 4, 1), Postpaid, 0.00, true)
        _ <- repo.save("61400100201", "Test 1980-Postpaid--no", LocalDate.of(1980, 4, 1), Postpaid, 0.00, false)
        _ <- repo.save("61400100202", "Test 1980-Prepaid-50.00-no", LocalDate.of(1980, 4, 1), Prepaid, 50.00, false)
        _ <- repo.save("61400100203", "Test 2010-Prepaid-50.00-yes", LocalDate.of(2010, 4, 1), Prepaid, 50.00, true)
        _ <- repo.save("61400100204", "Test 2010-Prepaid-5.00-yes", LocalDate.of(2010, 4, 1), Prepaid, 5.00, true)
      yield repo
    )
