package amf.shapes.client.scala.config

import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.parse.{AMFParser, InvalidBaseUnitTypeException}
import amf.shapes.client.scala.model.document.{JsonLDInstanceDocument, JsonSchemaDocument}
import amf.shapes.client.scala.{JsonLDInstanceResult, JsonLDSchemaResult, ShapesBaseUnitClient, ShapesConfiguration}
import amf.shapes.internal.document.metamodel.{JsonLDInstanceDocumentModel, JsonSchemaDocumentModel}

import scala.concurrent.{ExecutionContext, Future}

class JsonLDSchemaConfigurationClient private[amf] (override protected val configuration: JsonLDSchemaConfiguration)
    extends ShapesBaseUnitClient(configuration) {

  override implicit val exec: ExecutionContext = configuration.getExecutionContext

  override def getConfiguration: JsonLDSchemaConfiguration = configuration

  def parseJsonLDInstance(url: String, jsonLDSchema: JsonSchemaDocument): Future[JsonLDInstanceResult] = {
    configuration.withJsonLDSchema(jsonLDSchema).baseUnitClient().parse(url).map {
      case result: AMFParseResult if result.baseUnit.isInstanceOf[JsonLDInstanceDocument] =>
        new JsonLDInstanceResult(result.baseUnit.asInstanceOf[JsonLDInstanceDocument], result.results)
      case other =>
        throw InvalidBaseUnitTypeException.forMeta(other.baseUnit.meta, JsonLDInstanceDocumentModel)
    }
  }

  def parseJsonLDSchema(url: String): Future[JsonLDSchemaResult] = AMFParser.parse(url, configuration).map {
    case result: AMFParseResult if result.baseUnit.isInstanceOf[JsonSchemaDocument] =>
      new JsonLDSchemaResult(result.baseUnit.asInstanceOf[JsonSchemaDocument], result.results)
    case other =>
      throw InvalidBaseUnitTypeException.forMeta(other.baseUnit.meta, JsonSchemaDocumentModel)
  }
}
