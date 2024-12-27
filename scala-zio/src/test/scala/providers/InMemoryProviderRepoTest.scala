package mjs.premsms
package providers

import zio.ZIO
import zio.test.{Spec, ZIOSpecDefault, assertTrue}

object InMemoryProviderRepoTest extends ZIOSpecDefault {

  def spec: Spec[Any, Throwable] = suite("InMemoryProviderRepo tests")(
    suite("findByNumber function")(
      test("returns empty for an unknown number") {
        for
          repo <- ZIO.service[ProviderRepo]
          found <- repo.findByNumber("1900000000")
        yield assertTrue(found.isEmpty)
      },
      test("returns the provider from a known number") {
        for
          repo <- ZIO.service[ProviderRepo]
          found <- repo.findByNumber("190000")
        yield assertTrue(found.get == Provider("Test provider 0", "190000", BigDecimal(0.55), 0))
      },
    )
  ).provide(
    TestProvidersRepo.layer
  )
}
