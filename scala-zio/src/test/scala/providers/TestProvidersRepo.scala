package mjs.premsms
package providers

import zio.{Ref, ZLayer}

object TestProvidersRepo:

  def layer: ZLayer[Any, Nothing, InMemoryProviderRepo] =
    ZLayer.fromZIO(
      Ref.make(
        Map(
          "190000" -> Provider("Test provider 0", "190000", BigDecimal(0.55), 0),
          "190001" -> Provider("Test provider 1", "190001", BigDecimal(0.55), 18),
          "190002" -> Provider("Test provider 2", "190002", BigDecimal(5.50), 0),
          "190003" -> Provider("Test provider 3", "190003", BigDecimal(5.50), 18),
        )
      ).map(new InMemoryProviderRepo(_))
    )
