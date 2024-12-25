package mjs.premsms

import zio.*

sealed trait InvalidRequestError

object EmptySenderError extends InvalidRequestError
object EmptyRecipientError extends InvalidRequestError
object EmptyMessageError extends InvalidRequestError
object FutureTimestampError extends InvalidRequestError

def validateRequest(request: PremiumSmsRequest): ZIO[Any, InvalidRequestError, PremiumSmsRequest] =
  if request.sender.isEmpty then
    ZIO.fail(EmptySenderError)
  else if request.recipient.isEmpty then
    ZIO.fail(EmptyRecipientError)
  else if request.message.isEmpty then
    ZIO.fail(EmptyMessageError)
  else
    for
      now <- Clock.currentDateTime
      validation <- if request.timestamp.isAfter(now) then
        ZIO.fail(FutureTimestampError)
      else
        ZIO.succeed(request)
    yield validation
