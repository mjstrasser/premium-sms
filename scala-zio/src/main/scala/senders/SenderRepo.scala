package mjs.premsms
package senders

import zio.{RIO, Task, ZIO}

trait SenderRepo:
  def save(sender: Sender): Task[String]

  def findByMsisdn(msisdn: String): Task[Option[Sender]]

  def findByName(name: String): Task[Option[Sender]]

object SenderRepo:

  def save(sender: Sender): RIO[SenderRepo, String] =
    ZIO.serviceWithZIO[SenderRepo](_.save(sender))

  def findByMsisdn(msisdn: String): RIO[SenderRepo, Option[Sender]] =
    ZIO.serviceWithZIO[SenderRepo](_.findByMsisdn(msisdn))

  def findByName(name: String): RIO[SenderRepo, Option[Sender]] =
    ZIO.serviceWithZIO[SenderRepo](_.findByName(name))
