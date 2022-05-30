package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.model.domain._
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.client.scala.model.domain.{DomainElement, NamedDomainElement}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.plugins.syntax.SyamlSyntaxRenderPlugin
import amf.core.internal.remote.{Hint, Mimes, Spec}
import amf.core.internal.unsafe.PlatformSecrets
import amf.io.FileAssertionTest
import amf.shapes.client.scala.model.domain.Example
import amf.testing.ConfigProvider
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.yaml.model.{YDocument, YNode}

import java.io.StringWriter
import scala.concurrent.{ExecutionContext, Future}

trait DomainElementCycleTest extends AsyncFunSuite with FileAssertionTest with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  case class EmissionConfig(source: String, golden: String, hint: Hint, directory: String) {
    def goldenPath: String = directory + golden
    def sourcePath: String = directory + source
  }
  def basePath: String
  def spec: Spec

  def renderElement(
      source: String,
      extractor: BaseUnit => Option[DomainElement],
      golden: String,
      target: Hint,
      directory: String = basePath
  ): Future[Assertion] = {

    val config    = EmissionConfig(source, golden, target, directory)
    val amfConfig = ConfigProvider.configFor(target.spec)
    build(config, amfConfig)
      .map(b => extractor(b))
      .flatMap(render(_, amfConfig))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  private def build(config: EmissionConfig, amfConfig: AMFConfiguration): Future[BaseUnit] = {
    amfConfig.baseUnitClient().parse(s"file://${config.sourcePath}").map(_.baseUnit)
  }

  private def render(element: Option[DomainElement], amfConfig: AMFConfiguration): Future[String] = {
    Future.successful { renderDomainElement(element, amfConfig) }
  }

  def renderDomainElement(element: Option[DomainElement], amfConfig: AMFConfiguration): String = {
    val eh     = DefaultErrorHandler()
    val client = amfConfig.withErrorHandlerProvider(() => eh).elementClient()
    val node   = element.map(e => client.renderElement(e)).getOrElse(YNode.Empty)
    val errors = eh.getResults
    if (errors.nonEmpty)
      errors.map(_.completeMessage).mkString("\n")
    else {
      val document = SyamlParsedDocument(document = YDocument(node))
      val writer   = new StringWriter()
      SyamlSyntaxRenderPlugin.emit(Mimes.`application/yaml`, document, writer).getOrElse("").toString
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
