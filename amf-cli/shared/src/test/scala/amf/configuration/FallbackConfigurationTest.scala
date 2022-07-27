package amf.configuration

import amf.core.internal.remote.{Hint, Raml08YamlHint}
import amf.validation.UniquePlatformReportGenTest

class FallbackConfigurationTest extends UniquePlatformReportGenTest {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/configuration/validation/"
  override val reportsPath: String = "file://amf-cli/shared/src/test/resources/configuration/validation/"

  test("RAML 0.8 api that references RAML 1.0 fragment should have a warning") {
    validate("warning-fallback/api.raml", Some("warning-fallback/warning-fallback.report"))
  }

  test("External Fragment as root with composite configuration throws warning") {
    validate("unrecognized-guess-root-fallback/api.raml", Some("unrecognized-guess-root-fallback/report.report"))
  }
}
