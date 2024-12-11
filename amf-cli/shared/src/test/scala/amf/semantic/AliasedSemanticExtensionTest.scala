package amf.semantic

import amf.core.internal.remote.Raml10
import org.scalatest.funsuite.AsyncFunSuite

class AliasedSemanticExtensionTest extends AsyncFunSuite with SemanticExtensionParseTest {

  override protected val basePath = "file://amf-cli/shared/src/test/resources/semantic/aliased/"

  test("Apply semantic extension to RAML 1.0") {
    assertModel("extension.yaml", "api.raml", Raml10) { lookupResponse }
  }

}
