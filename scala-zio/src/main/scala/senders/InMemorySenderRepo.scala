package mjs.premsms
package senders

import zio.{Ref, Task, ZLayer}

class InMemorySenderRepo(map: Ref[Map[String, Sender]]) extends SenderRepo:

  override def save(sender: Sender): Task[String] =
    for
      _ <- map.update(_ + (sender.msisdn -> sender))
    yield sender.msisdn

  override def findByMsisdn(msisdn: String): Task[Option[Sender]] =
    map.get.map(_.get(msisdn))

object InMemorySenderRepo {
  def layer: ZLayer[Any, Nothing, InMemorySenderRepo] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[String, Sender]).map(new InMemorySenderRepo(_))
    )
}
