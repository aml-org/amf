package amf.parser

import amf.apicontract.client.scala.AMFBaseUnitClient
import amf.core.client.common.transform.PipelineId
import amf.core.internal.remote.{AmfJsonHint, GrpcProtoHint, Spec}
import amf.grpc.client.scala.GRPCConfiguration
import org.scalatest.Assertion

import scala.concurrent.Future

class GrpcValidTCKTest extends GrpcFunSuiteCycleTest {
  override def basePath: String = s"amf-cli/shared/src/test/resources/grpc/tck/apis/valid/"

  val client: AMFBaseUnitClient = GRPCConfiguration.GRPC().baseUnitClient()

  def assertValidate(api: String): Future[Assertion] = {
    for {
      parsing <- client.parse(s"file://$api")
      transformation = client.transform(parsing.baseUnit, PipelineId.Cache)
      validation <- client.validate(transformation.baseUnit)
    } yield {
      assert(parsing.conforms && transformation.conforms && validation.conforms)
    }
  }

  /** parse, transform, validate and cycle valid APIs */
    // cycle valid APIs
    fs.syncFile(s"$basePath").list.foreach { api =>
      if (api.endsWith(".proto") && !api.endsWith(".dumped.proto")) {
        test(s"Grpc TCK > Apis > Valid > $api: dumped JSON matches golden") {
          cycle(api, api.replace(".proto", ".jsonld"), GrpcProtoHint, AmfJsonHint)
        }

        test(s"Grpc TCK > Apis > Valid > $api: dumped Grpc matches golden") {
          cycle(api, api.replace(".proto", ".dumped.proto"), GrpcProtoHint, GrpcProtoHint)
        }
      }
    }

    // cycle transformed valid APIs
    fs.syncFile(s"$basePath").list.foreach { api =>
      if (api.endsWith(".proto") && !api.endsWith(".dumped.proto")) {
        test(s"Grpc TCK > Apis > Valid > $api: resolved dumped JSON matches golden") {
          cycle(
            api,
            api.replace(".proto", ".resolved.jsonld"),
            GrpcProtoHint,
            AmfJsonHint,
            transformWith = Some(Spec.GRPC)
          )
        }
      }
    }

  // validate valid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".proto") && !api.endsWith(".dumped.proto")) {
      test(s"Grpc TCK > Apis > Valid > $api: should conform") { assertValidate(s"$basePath/$api") }
    }
  }
}

class GrpcInvalidParseTCKTest extends GrpcFunSuiteCycleTest {
  override def basePath: String = s"amf-cli/shared/src/test/resources/grpc/tck/apis/invalid-parse/"

  val client: AMFBaseUnitClient = GRPCConfiguration.GRPC().baseUnitClient()

  def assertParse(api: String): Future[Assertion] = {
    val client = GRPCConfiguration.GRPC().baseUnitClient()
    for {
      parsing <- client.parse(s"file://$api")
    } yield {
      assert(!parsing.conforms)
    }
  }

  /** parse, transform, validate and cycle invalid APIs */

  // validate invalid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".proto") && !api.endsWith(".dumped.proto")) {
      test(s"Grpc TCK > Apis > invalid > $api: should not conform") { assertParse(s"$basePath/$api") }
    }
  }
}

class GrpcInvalidValidateTCKTest extends GrpcFunSuiteCycleTest {
  override def basePath: String = s"amf-cli/shared/src/test/resources/grpc/tck/apis/invalid-validate/"

  val client: AMFBaseUnitClient = GRPCConfiguration.GRPC().baseUnitClient()

  def assertValidation(api: String): Future[Assertion] = {
    val client = GRPCConfiguration.GRPC().baseUnitClient()
    for {
      parsing  <- client.parse(s"file://$api")
      validate <- client.validate(parsing.baseUnit)
    } yield {
      // Some of the validations are implemented in parsing, so we need to check both
      assert(!parsing.conforms || !validate.conforms)
    }
  }

  /** parse, transform, validate and cycle invalid APIs */

  // validate invalid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".proto") && !api.endsWith(".dumped.proto")) {
      test(s"Grpc TCK > Apis > invalid > $api: should not conform") { assertValidation(s"$basePath/$api") }
    }
  }
}
