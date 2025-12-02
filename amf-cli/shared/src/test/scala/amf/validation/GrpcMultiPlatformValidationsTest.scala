package amf.validation

import amf.apicontract.client.scala.AMFConfiguration
import amf.grpc.client.scala.GRPCConfiguration
import org.scalatest.matchers.should.Matchers

class GrpcMultiPlatformValidationsTest extends MultiPlatformReportGenTest with Matchers {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/grpc/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/grpc/"

  val config: AMFConfiguration = GRPCConfiguration.GRPC()

  test("Invalid proto with lexical error should show an error") {
    validate("lexical-error.proto", Some("lexical-error.report"), configOverride = Some(config))
  }

  test("Invalid import should show an error") {
    validate("invalid-import.proto", Some("invalid-import.report"), configOverride = Some(config))
  }

  test("Invalid import format should not be handled correctly") {
    validate("invalid-import-format.proto", Some("invalid-import-format.report"), configOverride = Some(config))
  }

}
