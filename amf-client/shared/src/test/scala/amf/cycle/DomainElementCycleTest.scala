package amf.cycle

import amf.client.convert.WebApiRegister
import amf.client.parse.DefaultErrorHandler
import amf.core.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.model.domain.{DomainElement, NamedDomainElement}
import amf.core.parser.SyamlParsedDocument
import amf.core.remote.{Hint, Vendor}
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.parser.spec.common.emitters.WebApiDomainElementEmitter
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.api.Api
import amf.plugins.syntax.SYamlSyntaxPlugin
import org.scalatest.{Assertion, AsyncFunSuite, BeforeAndAfterAll}
import org.yaml.model.{YDocument, YNode}

import scala.concurrent.{ExecutionContext, Future}

trait DomainElementCycleTest extends AsyncFunSuite with FileAssertionTest with BeforeAndAfterAll {

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
    build(config, Some(DefaultErrorHandler()))
      .map(b => extractor(b))
      .flatMap(render)
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  override protected def beforeAll(): Unit = WebApiRegister.register(platform)

  private def build(config: EmissionConfig, eh: Option[AMFErrorHandler]): Future[BaseUnit] = {
    Validation(platform).flatMap { _ =>
      AMFCompiler(s"file://${config.sourcePath}", platform, config.hint, eh = eh.getOrElse(UnhandledErrorHandler))
        .build()
    }
  }

  private def render(element: Option[DomainElement]): Future[String] = {
    Future { renderDomainElement(element) }
  }

  def renderDomainElement(element: Option[DomainElement]): String = {
    val eh     = DefaultErrorHandler()
    val node   = element.map(WebApiDomainElementEmitter.emit(_, vendor, eh)).getOrElse(YNode.Empty)
    val errors = eh.getResults
    if (errors.nonEmpty)
      errors.map(_.completeMessage).mkString("\n")
    else {
      val document = SyamlParsedDocument(document = YDocument(node))
      SYamlSyntaxPlugin.unparse("application/yaml", document).getOrElse("").toString
    }
  }

}

object CommonExtractors {

  def declaresIndex(i: Int)(b: BaseUnit): Option[DomainElement] = b match {
    case e: DeclaresModel =>
      Some(e.declares(i))
    case _ => None
  }

  def declaredWithName(name: String)(b: BaseUnit): Option[DomainElement] = b match {
    case e: DeclaresModel =>
      e.declares.collectFirst { case e: NamedDomainElement if e.name.is(name) => e }
    case _ => None
  }

  val webapi: BaseUnit => Option[Api] = {
    case e: EncodesModel =>
      Some(e.encodes.asInstanceOf[Api])
    case _ => None
  }

  val firstEndpoint: BaseUnit => Option[EndPoint] = webapi.andThen(_.flatMap(_.endPoints.headOption))

  val firstOperation: BaseUnit => Option[Operation] = firstEndpoint.andThen(_.flatMap(_.operations.headOption))

  val firstResponse: BaseUnit => Option[Response] = firstOperation.andThen(_.flatMap(_.responses.headOption))

  val firstRequest: BaseUnit => Option[Request] = firstOperation.andThen(_.flatMap(_.requests.headOption))

  val firstExample: BaseUnit => Option[Example] =
    firstResponse.andThen(_.flatMap(r => r.payloads.head.examples.headOption))

  val firstTemplatedLink: BaseUnit => Option[TemplatedLink] = firstResponse.andThen(_.flatMap(r => r.links.headOption))

  val firstCallback: BaseUnit => Option[Callback] = firstOperation.andThen(_.flatMap(o => o.callbacks.headOption))

  val firstServer: BaseUnit => Option[Server] = webapi.andThen(_.flatMap(_.servers.headOption))

}
