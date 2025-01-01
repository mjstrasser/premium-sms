package mjs.premsms

import accounts.AccountError
import providers.ProviderRepo
import senders.SenderRepo

import zio.ZIO

def sendPremiumSms(request: PremiumSmsRequest): ZIO[
  SenderRepo & ProviderRepo,
  Throwable | ValidateRequestError | AccountError,
  PremiumSmsResponse
] =
  for
    validated <- validateRequest(request)
    reserved <- reservePayment(validated)
  yield PremiumSmsResponse(request.timestamp,
    request.sender,
    request.recipient,
    reserved.provider.cost)
