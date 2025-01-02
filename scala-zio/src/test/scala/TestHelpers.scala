package mjs.premsms

import providers.{Provider, ProviderRepo}
import senders.{Sender, SenderRepo}

import zio.test.TestClock
import zio.{Clock, RIO, UIO, ZIO}

import java.time.{LocalDate, ZoneOffset}

def testSender(name: String): RIO[SenderRepo, Sender] =
  for
    repo <- ZIO.service[SenderRepo]
    maybeSender <- repo.findByName(name)
    sender <- ZIO.getOrFail(maybeSender)
  yield sender

def testProvider(name: String): RIO[ProviderRepo, Provider] =
  for
    repo <- ZIO.service[ProviderRepo]
    maybeProvider <- repo.findByName(name)
    provider <- ZIO.getOrFail(maybeProvider)
  yield provider

def setDateAtMidday(year: Int, month: Int, day: Int): UIO[Unit] =
  for
    _ <- TestClock.setTimeZone(ZoneOffset.UTC)
    _ <- TestClock.setTime(LocalDate.of(year, month, day).atTime(12, 0, 0)
      .atZone(ZoneOffset.UTC).toInstant)
  yield ()

def testRequest(senderName: String,
                providerName: String,
                message: String = "Test message"): RIO[
  SenderRepo & ProviderRepo,
  PremiumSmsRequest
] =
  for
    timestamp <- Clock.currentDateTime
    sender <- testSender(senderName)
    provider <- testProvider(providerName)
  yield PremiumSmsRequest(timestamp, sender.msisdn, provider.number, message)

def testData(senderName: String,
             providerName: String,
             message: String = "Test message"): RIO[
  SenderRepo & ProviderRepo,
  PremiumSmsData
] =
  for
    timestamp <- Clock.currentDateTime
    sender <- testSender(senderName)
    provider <- testProvider(providerName)
    request = PremiumSmsRequest(timestamp, sender.msisdn, provider.number, message)
  yield PremiumSmsData(request, sender, provider)

def simpleResponse(request: PremiumSmsRequest, cost: BigDecimal) =
  PremiumSmsResponse(
    request.timestamp,
    request.sender,
    request.recipient,
    cost)
