package mjs.premsms
package senders

import zio.{Ref, TaskLayer, ZLayer}

import java.time.LocalDate

object TestSendersRepo:

  def layer: TaskLayer[InMemorySenderRepo] =
    ZLayer.fromZIO(
      for
        repo <- Ref.make(Map.empty[String, Sender]).map(InMemorySenderRepo(_))
        _ <- repo.save("61400100200", "Test sender 1980/yes", LocalDate.of(1980, 4, 1), true)
        _ <- repo.save("61400100201", "Test sender 1980/no", LocalDate.of(1980, 4, 1), false)
        _ <- repo.save("61400100202", "Test sender 2010/yes", LocalDate.of(2010, 4, 1), true)
        _ <- repo.save("61400100203", "Test sender 2010/no", LocalDate.of(2010, 4, 1), false)
      yield repo
    )
