package amf.cycle

import amf.apicontract.client.scala.model.document.SecuritySchemeFragment
import amf.apicontract.client.scala.model.domain.security.{HttpSettings, SecurityScheme}
import amf.apicontract.client.scala.model.domain.templates.Trait
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.DeclaresModel
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.adoption.IdAdopter
import amf.core.internal.annotations.{ErrorDeclaration, TrackedElement}
import amf.core.internal.remote.{AmfJsonHint, Oas30JsonHint, Raml10YamlHint}
import amf.io.FunSuiteCycleTests
import amf.shapes.client.scala.model.domain.AnyShape
import org.scalatest.matchers.should.Matchers

class ParsedCloneTest extends FunSuiteCycleTests with Matchers {
  override def basePath: String = "amf-cli/shared/src/test/resources/clone/"

  test("Test error trait clone") {
    val config = CycleConfig("error-trait.raml", "", Raml10YamlHint, AmfJsonHint, basePath, None, None)
    for {
      model <- build(config, buildConfig(None, None))
    } yield {
      val element =
        model.cloneUnit().asInstanceOf[DeclaresModel].declares.head.asInstanceOf[Trait].effectiveLinkTarget()
      element.isInstanceOf[ErrorDeclaration[_]] should be(true)
    }
  }

  test("Test clone http settings of security scheme") {
    val config    = CycleConfig("api-key-name.json", "", Oas30JsonHint, AmfJsonHint, basePath, None, None)
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

  test("Test clone with tracked parameter at example") {
    val config = CycleConfig("security-scheme-fragment.raml", "", Raml10YamlHint, AmfJsonHint, basePath, None, None)
    for {
      model <- build(config, buildConfig(None, None))
    } yield {
      val cloned = model.cloneUnit()
      new IdAdopter(cloned, "http://fake.location.com#").adoptFromRoot()

      val param =
        cloned.asInstanceOf[SecuritySchemeFragment].encodes.headers.head

      val trackedElement =
        param.schema.asInstanceOf[AnyShape].examples.head.annotations.find(classOf[TrackedElement]).get
      val trackedObject = trackedElement.elements.left.get.head
      trackedObject shouldBe (param)
      trackedObject.id shouldBe (param.id)
      trackedObject.id.contains("security-scheme-fragment.raml") shouldBe (false)
    }
  }

  object IgnoreError extends AMFErrorHandler {
    override def report(result: AMFValidationResult): Unit = {}
  }
}
