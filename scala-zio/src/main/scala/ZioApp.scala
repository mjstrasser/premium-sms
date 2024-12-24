package mjs.premsms

import zio.*

import java.io.IOException

object ZioApp extends ZIOAppDefault {
  def run: ZIO[ZIOAppArgs & Scope, IOException, Unit] =
    for {
      name <- Console.readLine("Your name: ")
      _ <- Console.printLine(s"Hello, $name!")
    } yield ()
}
