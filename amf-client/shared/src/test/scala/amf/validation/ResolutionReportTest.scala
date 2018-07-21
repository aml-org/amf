package amf.validation
import amf.core.remote.{Hint, RamlYamlHint}

class ResolutionReportTest extends ResolutionForUniquePlatformReportTest {

  override val basePath = "file://amf-client/shared/src/test/resources/validations"

  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/resolution/"

  test("Invalid property overriding") {
    checkReport("/types/invalid-property-overriding.raml", Some("invalid-property-overriding.report"))
  }

  test("Invalid recursive shape") {
    checkReport("/types/recursive-shape.raml", Some("recursive-shape.report"))

  }

  test("Inheritance facets validations become warnings") {
    checkReport("/types/inheritance-facets.raml", Some("inheritance-facets-warnings.report"))
//      assert(!report.conforms)
//      assert(report.results.count(_.level == SeverityLevels.VIOLATION) == 2)
//      assert(report.results.size == 11)
  }
  override val hint: Hint = RamlYamlHint
}
