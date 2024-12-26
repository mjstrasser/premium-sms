package mjs.premsms
package senders

import zio.{Task, ZIO}

trait SenderRepo:
  def save(sender: Sender): Task[String]

  def findByMsisdn(msisdn: String): Task[Option[Sender]]

object SenderRepo:

  def save(sender: Sender): ZIO[SenderRepo, Throwable, String] =
    ZIO.serviceWithZIO[SenderRepo](_.save(sender))

  def findByMsisdn(msisdn: String): ZIO[SenderRepo, Throwable, Option[Sender]] =
    ZIO.serviceWithZIO[SenderRepo](_.findByMsisdn(msisdn))
