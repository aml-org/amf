package amf.emit

import amf.client.parse.DefaultParserErrorHandler
import amf.core.client.ParsingOptions
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.SyamlParsedDocument
import amf.core.parser.errorhandler.{ParserErrorHandler, UnhandledParserErrorHandler}
import amf.core.remote.{AsyncYamlHint, Hint, OasJsonHint, RamlYamlHint, Vendor}
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.parser.spec.common.emitters.DomainElementEmitter
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.models.{Response, WebApi}
import amf.plugins.syntax.SYamlSyntaxPlugin
import org.mulesoft.common.io.FileSystem
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.model.{YDocument, YNode}

import scala.concurrent.{ExecutionContext, Future}

class Raml08DomainElementEmission extends DomainElementEmitterTest {

  override def basePath: String = "amf-client/shared/src/test/resources/upanddown/"
  val vendor: Vendor            = Vendor.RAML08

  test("type - inlined json schema") {
    renderElement(
      "schema-position/api.raml",
      CommonExtractors.declaresIndex(0),
      "schema-position/type-emission.yaml",
      RamlYamlHint,
      directory = basePath + "cycle/raml08/"
    )
  }

  test("type - reference to external json schema with relative path") {
    renderElement(
      "raml08/json_schema_array.raml",
      CommonExtractors.declaresIndex(0),
      "raml08/json_schema_array-type.yaml",
      RamlYamlHint
    )
  }
}

class Raml10DomainElementEmission extends DomainElementEmitterTest {

  val basePath: String       = "amf-client/shared/src/test/resources/cycle/raml10/"
  val jsonSchemaPath: String = "amf-client/shared/src/test/resources/org/raml/json_schema/"
  val vendor: Vendor         = Vendor.RAML10

  test("type - multiple inheritance with union and properties") {
    renderElement(
      "type/complex-inheritance-unions.raml",
      CommonExtractors.NAMED_ROOT_IN_DECLARES,
      "type/complex-inheritance-unions.yaml",
      RamlYamlHint
    )
  }

  test("type - reference to external fragment") {
    renderElement(
      "multiple-refs/input.raml",
      CommonExtractors.NAMED_ROOT_IN_DECLARES,
      "multiple-refs/type-cycle-emission.yaml",
      RamlYamlHint,
      directory = jsonSchemaPath
    )
  }

  test("response - named example included") {
    renderElement(
      "response/input.raml",
      CommonExtractors.FIRST_RESPONSE,
      "response/output.yaml",
      RamlYamlHint
    )
  }

}

class Oas20DomainElementEmission extends DomainElementEmitterTest {

  override def basePath: String = "amf-client/shared/src/test/resources/cycle/oas20/"
  val vendor: Vendor            = Vendor.OAS20

  test("type - composition with refs and inlined") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(0),
      "type/cat-emission.yaml",
      OasJsonHint
    )
  }

  test("type - schema with properties") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(1),
      "type/pet-emission.yaml",
      OasJsonHint
    )
  }

}

class Oas30DomainElementEmission extends DomainElementEmitterTest {

  override def basePath: String = "amf-client/shared/src/test/resources/cycle/oas30/"
  val vendor: Vendor            = Vendor.OAS30

  test("type - composition with refs and inlined") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(0),
      "type/login-response-emission.yaml",
      OasJsonHint
    )
  }

}

class Async20DomainElementEmission extends DomainElementEmitterTest {

  override def basePath: String = "amf-client/shared/src/test/resources/cycle/async20/"
  val vendor: Vendor            = Vendor.ASYNC20

  test("type - composition with refs and inlined") {
    renderElement(
      "type/draft-7-schemas.yaml",
      CommonExtractors.declaresIndex(0),
      "type/schema-emission.yaml",
      AsyncYamlHint
    )
  }

}

object CommonExtractors {

  def declaresIndex(i: Int)(b: BaseUnit): Option[DomainElement] = b match {
    case e: DeclaresModel =>
      Some(e.declares(i))
    case _ => None
  }

  val FIRST_RESPONSE: BaseUnit => Option[Response] = {
    case e: EncodesModel =>
      Some(e.encodes.asInstanceOf[WebApi].endPoints.head.operations.head.responses.head)
    case _ => None
  }

  val NAMED_ROOT_IN_DECLARES: BaseUnit => Option[AnyShape] = {
    case e: DeclaresModel =>
      e.declares.collectFirst { case s: AnyShape if s.name.is("root") => s }
    case _ => None
  }

}
trait DomainElementEmitterTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  case class EmissionConfig(source: String, golden: String, hint: Hint, directory: String) {
    def goldenPath: String = directory + golden
    def sourcePath: String = directory + source
  }
  def basePath: String
  def vendor: Vendor

  def renderElement(source: String,
                    extractor: BaseUnit => Option[DomainElement],
                    golden: String,
                    hint: Hint,
                    directory: String = basePath): Future[Assertion] = {

    val config = EmissionConfig(source, golden, hint, directory)
    build(config, Some(DefaultParserErrorHandler.withRun()))
      .map(extractor)
      .flatMap(render)
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  private def build(config: EmissionConfig, eh: Option[ParserErrorHandler]): Future[BaseUnit] = {
    Validation(platform).flatMap { _ =>
      AMFCompiler(s"file://${config.sourcePath}",
                  platform,
                  config.hint,
                  eh = eh.getOrElse(UnhandledParserErrorHandler)).build()
    }
  }

  private def render(element: Option[DomainElement]): Future[String] = {
    Future { renderDomainElement(element) }
  }

  private def renderDomainElement(shape: Option[DomainElement]): String = {
    val node     = shape.map(DomainElementEmitter.emit(_, vendor)).getOrElse(YNode.Empty)
    val document = SyamlParsedDocument(document = YDocument(node))
    SYamlSyntaxPlugin.unparse("application/yaml", document).getOrElse("").toString
  }

}
