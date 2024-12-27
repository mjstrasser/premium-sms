package mjs.premsms
package providers

import zio.{Ref, Task, ZLayer}

class InMemoryProviderRepo(map: Ref[Map[String, Provider]]) extends ProviderRepo:

  override def findByNumber(number: String): Task[Option[Provider]] =
    map.get.map(_.get(number))

object InMemoryProviderRepo {
  def layer: ZLayer[Any, Nothing, InMemoryProviderRepo] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[String, Provider]).map(new InMemoryProviderRepo(_))
    )
}
