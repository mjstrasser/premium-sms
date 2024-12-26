package mjs.premsms

import senders.{Sender, SenderRepo}

import zio.{Clock, ZIO}

sealed trait CheckPermissionsError

object SenderUnknownError extends CheckPermissionsError

object UnderageError extends CheckPermissionsError

object PremiumSmsDisallowedError extends CheckPermissionsError

def youngerThan18(sender: Sender): ZIO[Any, Nothing, Boolean] =
  Clock.localDateTime
    .map(_.toLocalDate)
    .map(_.isBefore(sender.dob.plusYears(18)))

def checkPermissions(request: PremiumSmsRequest): ZIO[SenderRepo, CheckPermissionsError | Throwable, PremiumSmsRequest] =
  for
    repo <- ZIO.service[SenderRepo]
    maybeFound <- repo.findByMsisdn(request.sender)
    sender <- ZIO.fromOption(maybeFound).orElseFail(SenderUnknownError)
    isYounger <- youngerThan18(sender)
    next <- if isYounger then
      ZIO.fail(UnderageError)
    else if sender.usePremiumSms then
      ZIO.succeed(request)
    else
      ZIO.fail(PremiumSmsDisallowedError)
  yield next
