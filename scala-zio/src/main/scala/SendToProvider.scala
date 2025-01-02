package mjs.premsms

import zio.ZIO
import zio.http.{Client, Request}

def sendToProvider(data: PremiumSmsData): ZIO[Client, Throwable, PremiumSmsResponse] =
  for
    response <- ZIO.scoped {
      Client.streaming(Request.get(data.provider.url))
    }
    _ <- ZIO.debug(s"HTTP client response: $response")
  yield PremiumSmsResponse(data.request.timestamp,
    data.request.sender,
    data.request.recipient,
    data.provider.cost)
