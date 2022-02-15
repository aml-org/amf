package amf.shapes.internal.spec.jsonschema.semanticjsonschema

import amf.aml.client.scala.AMLDialectResult
import amf.aml.client.scala.model.document.{Dialect, Vocabulary}
import amf.core.client.scala.AMFResult
import amf.shapes.client.scala.config.{AMFSemanticSchemaResult, SemanticJsonSchemaConfiguration}

import scala.concurrent.{ExecutionContext, Future}

object SemanticSchemaParser {

  def parse(uri: String, config: SemanticJsonSchemaConfiguration): Future[AMFSemanticSchemaResult] = {
    implicit val executionContext: ExecutionContext = config.getExecutionContext

    config.baseUnitClient().parseDialect(uri).map(processResult)
  }

  def parseContent(content: String, config: SemanticJsonSchemaConfiguration): Future[AMFSemanticSchemaResult] = {
    implicit val executionContext: ExecutionContext = config.getExecutionContext

    config.baseUnitClient().parseContent(content).map(processResult)
  }

  private def getVocabularyFromReferences(dialect: Dialect): Option[Vocabulary] = {
    dialect.references.headOption.collect { case vocab: Vocabulary => vocab }
  }

  private def processResult(result: AMFResult): AMFSemanticSchemaResult = {
    val AMLDialectResult(dialect: Dialect, results) = result
    val maybeVocab                                  = getVocabularyFromReferences(dialect)
    dialect.withReferences(Nil) // remove references to avoid emission with "uses" as it is harder to use
    AMFSemanticSchemaResult(dialect, maybeVocab, results)
  }
}
