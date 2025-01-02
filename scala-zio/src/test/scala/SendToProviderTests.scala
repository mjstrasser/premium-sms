package mjs.premsms

import providers.TestProvidersRepo
import senders.TestSendersRepo

import zio.http.{Request, Response, TestClient}
import zio.test.{Spec, ZIOSpecDefault, assertTrue}

object SendToProviderTests extends ZIOSpecDefault:

  def spec: Spec[Any, Any] =
    suite("sendToProvider function")(
      test("returns a response in all cases") {
        for
          data <- testData("Test 1980-Postpaid--yes", "Test provider 0.55/0")
          _ <- TestClient.addRequestResponse(Request.get(data.provider.url), Response.ok)
          response <- sendToProvider(data)
        yield assertTrue(response == simpleResponse(data.request, data.provider.cost))
      }
    ).provide(
      TestProvidersRepo.layer,
      TestSendersRepo.layer,
      TestClient.layer
    )
