package mjs.premsms
package providers

import zio.{Random, Ref, Task, UIO, ZLayer}

class InMemoryProviderRepo(map: Ref[Map[String, Provider]]) extends ProviderRepo:

  override def findByNumber(number: String): Task[Option[Provider]] =
    map.get.map(_.get(number))

  override def findByName(name: String): Task[Option[Provider]] =
    map.get.map(_.values.find(_.name == name))

  def save(number: String,
           name: String,
           cost: BigDecimal,
           minimumAge: Int): UIO[InMemoryProviderRepo] =
    for
      id <- Random.nextUUID
      provider = Provider(
        id = id,
        number = number,
        name = name,
        cost = cost,
        minimumAge = minimumAge
      )
      _ <- map.update(_ + (number -> provider))
    yield this

object InMemoryProviderRepo {
  def layer: ZLayer[Any, Nothing, InMemoryProviderRepo] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[String, Provider]).map(InMemoryProviderRepo(_))
    )
}
