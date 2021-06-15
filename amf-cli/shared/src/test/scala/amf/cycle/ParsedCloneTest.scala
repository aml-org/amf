package amf.cycle

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.DeclaresModel
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.annotations.ErrorDeclaration
import amf.core.internal.remote.{Amf, Oas30JsonHint, Raml10YamlHint}
import amf.io.FunSuiteCycleTests
import amf.plugins.domain.apicontract.models.security.{HttpSettings, SecurityScheme}
import amf.plugins.domain.apicontract.models.templates.Trait
import org.scalatest.Matchers._

class ParsedCloneTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/clone/"

  test("Test error trait clone") {
    val config = CycleConfig("error-trait.raml", "", Raml10YamlHint, Amf, basePath, None, None)
    for {
      model <- build(config, buildConfig(None, None))
    } yield {
      val element =
        model.cloneUnit().asInstanceOf[DeclaresModel].declares.head.asInstanceOf[Trait].effectiveLinkTarget()
      element.isInstanceOf[ErrorDeclaration[_]] should be(true)
    }
  }

  test("Test clone http settings of security scheme") {
    val config    = CycleConfig("api-key-name.json", "", Oas30JsonHint, Amf, basePath, None, None)
    val amfConfig = buildConfig(None, Some(IgnoreError))
    for {
      model <- build(config, amfConfig)
    } yield {
      val settings = model.asInstanceOf[DeclaresModel].declares.head.asInstanceOf[SecurityScheme].settings
      val clonedSettings =
        model.cloneUnit().asInstanceOf[DeclaresModel].declares.head.asInstanceOf[SecurityScheme].settings
      settings.meta.`type`.head.iri() should be(clonedSettings.meta.`type`.head.iri())
      clonedSettings.isInstanceOf[HttpSettings] should be(true)
    }
  }

  object IgnoreError extends AMFErrorHandler {
    override def report(result: AMFValidationResult): Unit = {}
  }
}
