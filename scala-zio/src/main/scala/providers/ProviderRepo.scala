package mjs.premsms
package providers

import zio.{Task, ZIO}

trait ProviderRepo:

  def findByNumber(number: String): Task[Option[Provider]]

  def findByName(name: String): Task[Option[Provider]]

object ProviderRepo:

  def findByNumber(number: String): ZIO[ProviderRepo, Throwable, Option[Provider]] =
    ZIO.serviceWithZIO[ProviderRepo](_.findByNumber(number))

  def findByName(name: String): ZIO[ProviderRepo, Throwable, Option[Provider]] =
    ZIO.serviceWithZIO[ProviderRepo](_.findByName(name))
