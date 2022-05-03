package amf.validation

import amf.core.internal.remote.{Hint, Oas20JsonHint}

/** Test to document regex differences between jvm and js. No specific reason for it to be OAS. Here are the differences
  * that could not be hacked in the implicit class [[RegexConverter]].
  */
class OasRegexMultiPlatformReportTest extends MultiPlatformReportGenTest {

  /** Difference between jvm (curly brace needs to be escaped when you want it to be a character to match (not part of a
    * range)) and js (matches unescaped curly brace as a character to match).
    */
  test("Test regex difference: '{' parsing") {
    validate("/regex/curly-brace.json", Some("regex/curly-brace.report"))
  }

  override val basePath    = "file://amf-cli/shared/src/test/resources/validations/"
  override val reportsPath = "amf-cli/shared/src/test/resources/validations/reports/multi-plat-model/"
  override val hint: Hint  = Oas20JsonHint
}
