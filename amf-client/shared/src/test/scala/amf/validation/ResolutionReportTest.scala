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
  }

  test("Unresolve inheritance from same class") {
    checkReport("/types/unresolve-inherits-sameclass.raml", None)
  }

  test("Unresolve inheritance from different clases") {
    checkReport("/types/unresolve-inherits-differentclass.raml", Some("unresolve-inherits-differentclass.report"))
  }

  test("Invalid fields in overlay and master") {
    checkReport("/overlays/invalid-override/overlay.raml", Some("overlays-report.report"))
  }

  test("Invalid fields without override in overlay") {
    checkReport("/overlays/restricted-notoverride/overlay.raml", None)
  }

  test("Invalid type overrided") {
    checkReport("/overlays/valid-declaration/overlay.raml", None)
  }

  test("Invalid not existing node in master") {
    checkReport("/overlays/not-existing-node/overlay.raml", Some("not-existing-node.report"))
  }

  override val hint: Hint = RamlYamlHint
}
