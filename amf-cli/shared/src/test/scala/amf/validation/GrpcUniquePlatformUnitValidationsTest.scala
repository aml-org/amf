package amf.validation

import amf.apicontract.client.scala.AMFConfiguration
import amf.grpc.client.scala.GRPCConfiguration
import org.scalatest.matchers.should.Matchers

class GrpcUniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/grpc/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/grpc/"

  val config: AMFConfiguration = GRPCConfiguration.GRPC()

  test("Invalid import should show an error") {
    validate("invalid-import.proto", Some("invalid-import.report"), configOverride = Some(config))
  }

  test("Invalid message ref should show an error") {
    validate("invalid-reference.proto", Some("invalid-reference.report"), configOverride = Some(config))
  }

  test("Invalid proto with lexical error should show an error") {
    validate("lexical-error.proto", Some("lexical-error.report"), configOverride = Some(config))
  }

}
