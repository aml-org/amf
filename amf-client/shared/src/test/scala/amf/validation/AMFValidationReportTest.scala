package amf.validation

import amf.ProfileNames
import amf.core.validation.{AMFValidationReport, AMFValidationResult}
import org.scalatest.FunSuite

class AMFValidationReportTest extends FunSuite {

  test("Test to string report with null values in results") {

    //noinspection ScalaStyle
    AMFValidationReport(
      conforms = true,
      null,
      ProfileNames.RAML,
      Seq(
        AMFValidationResult(null, null, null, None, null, null, null),
        AMFValidationResult(null, null, null, None, null, None, null)
      )
    ).toString
  }
}
