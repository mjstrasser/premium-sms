package mjs.premsms

import zio.*

case class InvalidRequestError(reason: String)

def validateRequest(request: PremiumSmsRequest): ZIO[Any, InvalidRequestError, PremiumSmsRequest] =
  if request.sender.isEmpty then
    ZIO.fail(InvalidRequestError("Empty sender"))
  else if request.recipient.isEmpty then
    ZIO.fail(InvalidRequestError("Empty recipient"))
  else if request.message.isEmpty then
    ZIO.fail(InvalidRequestError("Empty message"))
  else
    for {
      now <- Clock.currentDateTime
      validation <- if request.timestamp.isAfter(now) then
        ZIO.fail(InvalidRequestError("Timestamp is in the future"))
      else
        ZIO.succeed(request)
    } yield validation
