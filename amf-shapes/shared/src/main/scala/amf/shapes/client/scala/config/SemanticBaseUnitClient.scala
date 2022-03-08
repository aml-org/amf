package amf.shapes.client.scala.config

import amf.aml.client.scala.model.document.{Dialect, Vocabulary}
import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.validation.AMFValidationResult
import amf.shapes.client.scala.ShapesBaseUnitClient
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticSchemaParser

import scala.concurrent.{ExecutionContext, Future}

class SemanticBaseUnitClient private[amf] (override protected val configuration: SemanticJsonSchemaConfiguration)
    extends ShapesBaseUnitClient(configuration) {

  override implicit val exec: ExecutionContext = configuration.getExecutionContext

  override def getConfiguration: SemanticJsonSchemaConfiguration = configuration

  def parseSemanticSchema(url: String): Future[AMFSemanticSchemaResult] = {
    SemanticSchemaParser.parse(url, configuration)
  }

  def parseSemanticSchemaContent(content: String): Future[AMFSemanticSchemaResult] = {
    SemanticSchemaParser.parseContent(content, configuration)
  }
}

class AMFSemanticSchemaResult(override val baseUnit: Dialect,
                              val vocabulary: Option[Vocabulary],
                              override val results: Seq[AMFValidationResult])
    extends AMFParseResult(baseUnit, results)

object AMFSemanticSchemaResult {
  def apply(baseUnit: Dialect, vocabulary: Option[Vocabulary], results: Seq[AMFValidationResult]) =
    new AMFSemanticSchemaResult(baseUnit, vocabulary, results)

  def unapply(result: AMFSemanticSchemaResult) = Some((result.baseUnit, result.vocabulary, result.results))
}
