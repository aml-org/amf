package amf.semantic

import amf.aml.client.scala.{AMLConfiguration, AMLDialectResult}
import amf.aml.client.scala.model.document.Dialect
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.{AMFConfiguration, AMFLibraryResult, APIConfiguration}
import amf.core.client.scala.config.{CachedReference, UnitCache}
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{Document, Module}
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.domain.Value
import amf.core.internal.remote.Spec
import org.mulesoft.antlrast.unsafe.PlatformSecrets
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

trait SemanticExtensionParseTest extends PlatformSecrets with Matchers {
  implicit val executionContext: ExecutionContext
  protected val basePath: String

  case class CompanionLibUnitCache(library: Module) extends UnitCache {

    /** Fetch specified reference and return associated cached reference if exists. */
    override def fetch(url: String): Future[CachedReference] = {
      if (library.location().contains(url)) Future.successful(CachedReference(url, library))
      else Future.failed(new Exception(s"File $url is not the companion lib"))
    }
  }

  private def calculateCompanionName(dialect: String, spec: Spec): Option[String] = {
    if (spec.isRaml) Some(dialect.stripSuffix("yaml") + "raml")
    else None
  }

  private def getCompanionNameIfPresent(dialect: String, spec: Spec): Option[String] = {
    calculateCompanionName(dialect, spec).filter(path => platform.fs.syncFile(path.stripPrefix("file://")).exists)
  }

  private def companionLibSearch(dialect: String, spec: Spec): Option[Future[AMFLibraryResult]] = {
    getCompanionNameIfPresent(dialect, spec).map { n =>
      APIConfiguration.fromSpec(spec).baseUnitClient().parseLibrary(n)
    }
  }

  private def getDialect(path: String): Future[AMLDialectResult] = {
    AMLConfiguration.predefined().baseUnitClient().parseDialect(path)
  }

  private def extendConfig(extension: Dialect, spec: Spec): AMFConfiguration = {
    APIConfiguration
      .fromSpec(spec)
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .withDialect(extension)
  }

  private def configWithCompanion(config: AMFConfiguration, extension: Dialect, spec: Spec): Future[AMFConfiguration] = {
    companionLibSearch(extension.location().getOrElse(extension.id), spec).map { moduleResult =>
      moduleResult.map(r => config.withUnitCache(handleCompanionModule(r.library, extension)))
    } getOrElse Future.successful(config)
  }

  private def handleCompanionModule(library: Module, extension: Dialect): UnitCache = {
    CompanionLibUnitCache(extensionAtCompanion(library, extension))
  }

  private def extensionAtCompanion(library: Module, extension: Dialect): Module = {
    library.withReferences(library.references :+ extension)
  }

  protected def assertModel(dialect: String, api: String, spec: Spec)(
      assertion: Document => Assertion): Future[Assertion] = {
    for {
      extension     <- getDialect(basePath + dialect).map(_.dialect)
      parsingConfig <- configWithCompanion(extendConfig(extension, spec), extension, spec)
      instance      <- parsingConfig.baseUnitClient().parseDocument(basePath + api)
    } yield {
      assertion(instance.document)
    }
  }

  protected def lookupResponse(document: Document): Assertion = {
    val extension =
      document.encodes.asInstanceOf[Api].endPoints.head.operations.head.responses.head.customDomainProperties.head

    assertPaginationExtension(extension, 5)
  }

  protected def assertPaginationExtension(extension: DomainExtension, expectedValue: Int): Assertion = {
    val extensionValue = extension.fields.getValueAsOption("http://a.ml/vocab#pagination").get

    extension.name.value() shouldBe "pagination"
    extension.definedBy.name.value() shouldBe "pagination"

    assertAnnotations(extensionValue)

    extension.graph.containsProperty("http://a.ml/vocab#pagination") shouldBe true
    extension.graph
      .getObjectByProperty("http://a.ml/vocab#pagination")
      .head
      .graph
      .scalarByProperty("http://a.ml/vocab#PageSize")
      .head shouldBe expectedValue
  }

  private def assertAnnotations(value: Value): Unit = {
    value.annotations.nonEmpty shouldBe true
    value.annotations.find(classOf[LexicalInformation]) shouldNot be(empty)
  }
}
