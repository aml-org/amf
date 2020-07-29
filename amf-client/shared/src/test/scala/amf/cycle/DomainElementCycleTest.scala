package amf.cycle

import amf.client.parse.DefaultParserErrorHandler
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.SyamlParsedDocument
import amf.core.parser.errorhandler.{ParserErrorHandler, UnhandledParserErrorHandler}
import amf.core.remote.{Hint, Vendor}
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.parser.spec.common.emitters.DomainElementEmitter
import amf.plugins.domain.shapes.models.{AnyShape, Example}
import amf.plugins.domain.webapi.models.{
  Callback,
  EndPoint,
  Operation,
  Request,
  Response,
  Server,
  TemplatedLink,
  WebApi
}
import amf.plugins.syntax.SYamlSyntaxPlugin
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.model.{YDocument, YNode}

import scala.concurrent.{ExecutionContext, Future}

trait DomainElementCycleTest extends AsyncFunSuite with FileAssertionTest {

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
      .map(b => extractor(b))
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

object CommonExtractors {

  def declaresIndex(i: Int)(b: BaseUnit): Option[DomainElement] = b match {
    case e: DeclaresModel =>
      Some(e.declares(i))
    case _ => None
  }

  val webapi: BaseUnit => Option[WebApi] = {
    case e: EncodesModel =>
      Some(e.encodes.asInstanceOf[WebApi])
    case _ => None
  }

  val firstEndpoint: BaseUnit => Option[EndPoint] = webapi.andThen(_.flatMap(_.endPoints.headOption))

  val firstOperation: BaseUnit => Option[Operation] = firstEndpoint.andThen(_.flatMap(_.operations.headOption))

  val firstResponse: BaseUnit => Option[Response] = firstOperation.andThen(_.flatMap(_.responses.headOption))

  val firstRequest: BaseUnit => Option[Request] =
    firstOperation.andThen(_.flatMap(_.requests.headOption))

  val firstExample: BaseUnit => Option[Example] =
    firstResponse.andThen(_.flatMap(r => r.payloads(0).examples.headOption))

  val firstTemplatedLink: BaseUnit => Option[TemplatedLink] = firstResponse.andThen(_.flatMap(r => r.links.headOption))

  val firstCallback: BaseUnit => Option[Callback] = firstOperation.andThen(_.flatMap(o => o.callbacks.headOption))

  val firstServer: BaseUnit => Option[Server] = webapi.andThen(_.flatMap(_.servers.headOption))

  val namedRootInDeclares: BaseUnit => Option[AnyShape] = {
    case e: DeclaresModel =>
      e.declares.collectFirst { case s: AnyShape if s.name.is("root") => s }
    case _ => None
  }

}
