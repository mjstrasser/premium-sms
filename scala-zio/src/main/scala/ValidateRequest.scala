package mjs.premsms

import zio.ZIO

case class InvalidRequestError(reason: String)

def validateRequest(request: PremiumSmsRequest): ZIO[Any, InvalidRequestError, PremiumSmsRequest] =
  if request.sender.isEmpty then
    ZIO.fail(InvalidRequestError("Empty sender"))
  else if request.recipient.isEmpty then
    ZIO.fail(InvalidRequestError("Empty recipient"))
  else if request.message.isEmpty then
    ZIO.fail(InvalidRequestError("Empty message"))
  else
    ZIO.succeed(request)
