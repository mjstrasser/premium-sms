package mjs.premsms
package senders

import zio.{Random, Ref, Task, ZLayer}

import java.time.LocalDate

class InMemorySenderRepo(map: Ref[Map[String, Sender]]) extends SenderRepo:

  override def save(sender: Sender): Task[String] =
    for
      _ <- map.update(_ + (sender.msisdn -> sender))
    yield sender.msisdn

  override def findByMsisdn(msisdn: String): Task[Option[Sender]] =
    map.get.map(_.get(msisdn))

  override def findByName(name: String): Task[Option[Sender]] =
    map.get.map(_.values.find(_.name == name))

  def save(msisdn: String,
           name: String,
           dob: LocalDate,
           usePremiumSms: Boolean): Task[InMemorySenderRepo] =
    for
      id <- Random.nextUUID
      sender = Sender(
        id = id,
        msisdn = msisdn,
        name = name,
        dob = dob,
        usePremiumSms = usePremiumSms
      )
      _ <- save(sender)
    yield this

object InMemorySenderRepo {
  def layer: ZLayer[Any, Nothing, InMemorySenderRepo] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[String, Sender]).map(InMemorySenderRepo(_))
    )
}
