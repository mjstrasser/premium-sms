package mjs.premsms

import zio.ZIO
import zio.http.{Client, Request, Status}

def sendToProvider(data: PremiumSmsData): ZIO[
  Client,
  Throwable | SendToProviderError,
  PremiumSmsResponse
] =
  for
    httpResponse <- ZIO.scoped {
      Client.streaming(
        Request.get(data.provider.url)
          .addQueryParam("sender", data.sender.msisdn)
          .addQueryParam("number", data.provider.number)
          .addQueryParam("message", data.request.message),
      )
    }
    premSmsResponse <- httpResponse.status match
      case Status.Ok => ZIO.succeed(
        PremiumSmsResponse(data.request.timestamp, data.request.sender, data.request.recipient, data.provider.cost)
      )
      case Status.NotFound => ZIO.fail(ProviderUrlNotFoundError)
      case Status.BadRequest => ZIO.fail(SenderError)
      case _ => ZIO.fail(ProviderError)
  yield premSmsResponse
