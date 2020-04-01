package amf.cycle

import amf.core.annotations.ErrorDeclaration
import amf.core.model.document.DeclaresModel
import amf.core.parser.errorhandler.AmfParserErrorHandler
import amf.core.remote.{Amf, RamlYamlHint}
import amf.core.validation.AMFValidationResult
import amf.io.FunSuiteCycleTests
import amf.plugins.domain.webapi.models.templates.Trait
import org.scalatest.Matchers._

class ParsedCloneTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/clone/"

  test("Test error trait clone") {
    val config = CycleConfig("error-trait.raml", "", RamlYamlHint, Amf, basePath, None, None)
    for {
      model <- build(config, Some(IgnoreError), useAmfJsonldSerialisation = true)
    } yield {
      val element =
        model.cloneUnit().asInstanceOf[DeclaresModel].declares.head.asInstanceOf[Trait].effectiveLinkTarget()
      element.isInstanceOf[ErrorDeclaration] should be(true)
    }
  }

  object IgnoreError extends AmfParserErrorHandler {

    override def handlerAmfResult(result: AMFValidationResult): Boolean = false

    override private[amf] val parserRun = -1
  }
}
