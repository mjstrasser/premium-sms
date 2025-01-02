package mjs.premsms

import providers.TestProvidersRepo
import senders.TestSendersRepo

import zio.Exit
import zio.http.{Response, Status, TestClient}
import zio.test.{Spec, ZIOSpecDefault, assertTrue}

object SendToProviderTests extends ZIOSpecDefault:

  def spec: Spec[Any, Any] =
    suite("sendToProvider function")(
      test("returns PremiumSmsResponse if provider call returns OK") {
        for
          data <- testData("Test 1980-Postpaid--yes", "Test provider 0.55/0")
          _ <- testClient(data, Status.Ok)
          response <- sendToProvider(data)
        yield assertTrue(response == simpleResponse(data.request, data.provider.cost))
      },
      test("fails with ProviderUrlNotFoundError if provider returns 404") {
        for
          data <- testData("Test 1980-Postpaid--yes", "Test provider 0.55/0")
          _ <- testClient(data, Status.fromInt(404))
          exit <- sendToProvider(data).exit
        yield assertTrue(exit == Exit.fail(ProviderUrlNotFoundError))
      },
      test("fails with SenderError if provider returns 400") {
        for
          data <- testData("Test 1980-Postpaid--yes", "Test provider 0.55/0")
          _ <- testClient(data, Status.fromInt(400))
          exit <- sendToProvider(data).exit
        yield assertTrue(exit == Exit.fail(SenderError))
      },
      test("fails with ProviderError some other status is returned") {
        for
          data <- testData("Test 1980-Postpaid--yes", "Test provider 0.55/0")
          _ <- testClient(data, Status.fromInt(503))
          exit <- sendToProvider(data).exit
        yield assertTrue(exit == Exit.fail(ProviderError))
      },
    ).provide(
      TestProvidersRepo.layer,
      TestSendersRepo.layer,
      TestClient.layer
    )
