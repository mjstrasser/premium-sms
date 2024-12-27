package mjs.premsms

import providers.{Provider, ProviderRepo}
import senders.{Sender, SenderRepo}

import zio.{Clock, UIO, ZIO}

sealed trait CheckPermissionsError

object SenderUnknownError extends CheckPermissionsError

object ProviderUnknownError extends CheckPermissionsError

object UnderageError extends CheckPermissionsError

object PremiumSmsDisallowedError extends CheckPermissionsError

def tooYoung(sender: Sender, provider: Provider): UIO[Boolean] =
  Clock.localDateTime
    .map(_.toLocalDate)
    .map(_.isBefore(sender.dob.plusYears(provider.minimumAge)))

def checkPermissions(request: PremiumSmsRequest): ZIO[
  SenderRepo & ProviderRepo,
  CheckPermissionsError | Throwable,
  PremiumSmsRequest
] =
  for
    senderRepo <- ZIO.service[SenderRepo]
    providerRepo <- ZIO.service[ProviderRepo]

    maybeSender <- senderRepo.findByMsisdn(request.sender)
    sender <- ZIO.fromOption(maybeSender).orElseFail(SenderUnknownError)

    maybeProvider <- providerRepo.findByNumber(request.recipient)
    provider <- ZIO.fromOption(maybeProvider).orElseFail(ProviderUnknownError)

    isTooYoung <- tooYoung(sender, provider)
    next <- if isTooYoung then
      ZIO.fail(UnderageError)
    else if sender.usePremiumSms then
      ZIO.succeed(request)
    else
      ZIO.fail(PremiumSmsDisallowedError)
  yield next
