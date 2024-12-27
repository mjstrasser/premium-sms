package mjs.premsms
package providers

import zio.{Task, ZIO}

trait ProviderRepo:
  def findByNumber(number: String): Task[Option[Provider]]

object ProviderRepo:

  def findByNumber(number: String): ZIO[ProviderRepo, Throwable, Option[Provider]] =
    ZIO.serviceWithZIO[ProviderRepo](_.findByNumber(number))
