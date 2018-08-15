package amf.validation

import amf.RamlProfile
import amf.core.validation.{AMFValidationReport, AMFValidationResult}
import org.scalatest.FunSuite

class AMFValidationReportTest extends FunSuite {

  test("Test to string report with null values in results") {

    //noinspection ScalaStyle
    AMFValidationReport(
      conforms = true,
      null,
      RamlProfile,
      Seq(
        AMFValidationResult(null, null, null, None, null, null, None, null),
        AMFValidationResult(null, null, null, None, null, None, None, null)
      )
    ).toString
  }
}
