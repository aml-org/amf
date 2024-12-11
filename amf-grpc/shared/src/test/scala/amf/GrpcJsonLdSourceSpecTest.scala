package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.{Mimes, Spec}
import amf.grpc.client.scala.GRPCConfiguration
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class GrpcJsonLdSourceSpecTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  val path = "file://amf-cli/shared/src/test/resources/upanddown/grpc/google/empty.proto"

  test("Parsed JSON-LD from Grpc should have Grpc source spec") {
    val client = GRPCConfiguration.GRPC().baseUnitClient()
    for {
      result <- client.parse(path)
      jsonld <- Future.successful(client.render(result.baseUnit, Mimes.`application/ld+json`))
      jsonLdUnit <- client.parseContent(jsonld, "application/ld+json")
    } yield {
      jsonLdUnit.sourceSpec shouldBe Spec.GRPC
    }
  }
}
