package amf.dialects

import amf.core.model.document.BaseUnit
import amf.core.rdf.RdfModel
import amf.core.remote.Syntax.Syntax
import amf.core.remote.{Amf, Hint, Vendor, VocabularyYamlHint}
import amf.core.unsafe.PlatformSecrets
import amf.io.FunSuiteCycleTests
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class DialectSHACLTest extends FunSuiteCycleTests with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/dialects/"

  test("shacl 1 test") {
    cycleRdf("example1.raml", s"example1.shacl")
  }

  /** Method for transforming parsed unit. Override if necessary. */
  override def transformRdf(unit: BaseUnit, config: CycleConfig): RdfModel = {
    AMLPlugin.shapesForDialect(unit.asInstanceOf[Dialect], "http://metadata.org/validations.js")
  }

  /** Method to render parsed unit. Override if necessary. */
  override def renderRdf(unit: RdfModel, config: CycleConfig): Future[String] = {
    Future {
      unit.toN3().split("\n").sorted.mkString("\n")
    }
  }

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  override def cycleRdf(source: String,
                        golden: String,
                        hint: Hint = VocabularyYamlHint,
                        target: Vendor = Amf,
                        directory: String = basePath,
                        syntax: Option[Syntax] = None,
                        pipeline: Option[String] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory, syntax, pipeline)

    build(config, None, useAmfJsonldSerialisation = true)
      .map(transformRdf(_, config))
      .flatMap(renderRdf(_, config))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }
}
