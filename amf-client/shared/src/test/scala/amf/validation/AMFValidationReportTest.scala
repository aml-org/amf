package amf.validation

import amf.RAMLProfile
import amf.core.validation.{AMFValidationReport, AMFValidationResult}
import org.scalatest.FunSuite

class AMFValidationReportTest extends FunSuite {

  test("Test to string report with null values in results") {

    //noinspection ScalaStyle
    AMFValidationReport(
      conforms = true,
      null,
      RAMLProfile,
      Seq(
        AMFValidationResult(null, null, null, None, null, null, null),
        AMFValidationResult(null, null, null, None, null, None, null)
      )
    ).toString
  }
}
