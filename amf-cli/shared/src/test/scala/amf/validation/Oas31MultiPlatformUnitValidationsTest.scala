package amf.validation

class Oas31MultiPlatformUnitValidationsTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/oas31/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/oas31/"

  test("Server Variable Object default value must match an enum value (only if enum is defined)") {
    validate("server-variables-enum.yaml", Some("server-variables-enum.report"))
  }
}
