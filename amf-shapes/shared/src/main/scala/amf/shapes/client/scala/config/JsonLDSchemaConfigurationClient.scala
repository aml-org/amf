package amf.shapes.client.scala.config

import amf.core.client.scala.AMFParseResult
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.{ShapesBaseUnitClient, ShapesConfiguration}

import scala.concurrent.{ExecutionContext, Future}

class JsonLDSchemaConfigurationClient private[amf] (override protected val configuration: JsonLDSchemaConfiguration)
    extends ShapesBaseUnitClient(configuration) {

  override implicit val exec: ExecutionContext = configuration.getExecutionContext

  override def getConfiguration: JsonLDSchemaConfiguration = configuration

  def parseJsonLDInstance(url: String, jsonLDSchema: JsonSchemaDocument): Future[AMFParseResult] = {
    configuration.withJsonLDSchema(jsonLDSchema).baseUnitClient().parse(url)
  }

}
