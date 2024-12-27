package mjs.premsms
package providers

import zio.{Ref, ZLayer}

object TestProvidersRepo:

  def layer: ZLayer[Any, Nothing, InMemoryProviderRepo] =
    ZLayer.fromZIO(
      for
        repo <- Ref.make(Map.empty[String, Provider]).map(InMemoryProviderRepo(_))
        _ <- repo.addProvider("190000", "Test provider 0.55/0", BigDecimal(0.55), 0)
        _ <- repo.addProvider("190001", "Test provider 0.55/18", BigDecimal(0.55), 18)
        _ <- repo.addProvider("190002", "Test provider 5.50/0", BigDecimal(5.50), 0)
        _ <- repo.addProvider("190003", "Test provider 5.50/18", BigDecimal(5.50), 18)
      yield repo
    )
