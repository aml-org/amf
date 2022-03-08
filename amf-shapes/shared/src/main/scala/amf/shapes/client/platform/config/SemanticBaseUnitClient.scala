package amf.shapes.client.platform.config

import amf.aml.client.platform.AMLBaseUnitClient
import amf.aml.client.platform.model.document.{Dialect, Vocabulary}
import amf.core.client.platform.AMFParseResult
import amf.shapes.client.scala.config.{
  AMFSemanticSchemaResult => InternalAMFSemanticSchemaResult,
  SemanticBaseUnitClient => InternalSemanticBaseUnitClient
}
import amf.shapes.internal.convert.ShapeClientConverters.{ClientFuture, ClientOption}

import scala.scalajs.js.annotation.JSExportAll

import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
class SemanticBaseUnitClient private[amf] (private val _internal: InternalSemanticBaseUnitClient)
    extends AMLBaseUnitClient(_internal) {

  def parseSemanticSchema(url: String): ClientFuture[AMFSemanticSchemaResult] = {
    _internal.parseSemanticSchema(url).asClient
  }

  def parseSemanticSchemaContent(content: String): ClientFuture[AMFSemanticSchemaResult] = {
    _internal.parseSemanticSchemaContent(content).asClient
  }
}

@JSExportAll
class AMFSemanticSchemaResult(private[amf] override val _internal: InternalAMFSemanticSchemaResult)
    extends AMFParseResult(_internal) {

  override def baseUnit: Dialect           = _internal.baseUnit
  def vocabulary: ClientOption[Vocabulary] = _internal.vocabulary.asClient
}
