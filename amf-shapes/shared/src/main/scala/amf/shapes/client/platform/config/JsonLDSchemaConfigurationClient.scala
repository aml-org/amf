package amf.shapes.client.platform.config

import amf.aml.client.platform.BaseAMLBaseUnitClient
import amf.core.internal.convert.CoreClientConverters.ClientFuture
import amf.shapes.client.platform.model.document.JsonSchemaDocument
import amf.shapes.client.platform.{JsonLDInstanceResult, JsonLDSchemaResult}
import amf.shapes.client.scala.config.{JsonLDSchemaConfigurationClient => InternalJsonLDSchemaConfigurationClient}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class JsonLDSchemaConfigurationClient private[amf] (private [amf] val _internal: InternalJsonLDSchemaConfigurationClient) extends BaseAMLBaseUnitClient(_internal){
  implicit val exec: ExecutionContext = _internal.exec

  override def getConfiguration(): JsonLDSchemaConfiguration = _internal.getConfiguration

  def parseJsonLDInstance(url: String, jsonLDSchema: JsonSchemaDocument): ClientFuture[JsonLDInstanceResult] = _internal.parseJsonLDInstance(url, jsonLDSchema).asClient

  def parseJsonLDSchema(url:String): ClientFuture[JsonLDSchemaResult] = _internal.parseJsonLDSchema(url).asClient
}
