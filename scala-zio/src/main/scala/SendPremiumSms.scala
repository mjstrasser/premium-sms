package mjs.premsms

import accounts.AccountError
import providers.ProviderRepo
import senders.SenderRepo

import zio.ZIO
import zio.http.Client

def sendPremiumSms(request: PremiumSmsRequest): ZIO[
  SenderRepo & ProviderRepo & Client,
  Throwable | ValidateRequestError | AccountError,
  PremiumSmsResponse
] =
  for
    validated <- validateRequest(request)
    reserved <- reservePayment(validated)
    sent <- sendToProvider(reserved)
  yield sent
