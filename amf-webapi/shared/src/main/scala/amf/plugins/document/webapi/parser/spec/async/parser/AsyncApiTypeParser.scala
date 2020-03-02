package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.contexts.{SpecEmitterContext, WebApiContext}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.{
  AnyDefaultType,
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaVersion,
  OAS30SchemaVersion,
  OasTypeParser,
  RAML10SchemaVersion,
  Raml10TypeParser
}
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model.{YMap, YMapEntry, YNode}
import amf.plugins.document.webapi.parser.spec.{toOas, toRaml}

object AsyncSchemaFormats {
  val async20Schema = List("application/vnd.aai.asyncapi;version=2.0.0",
                           "application/vnd.aai.asyncapi+json;version=2.0.0",
                           "application/vnd.aai.asyncapi+yaml;version=2.0.0")
  val oas30Schema = List("application/vnd.oai.openapi;version=3.0.0",
                         "application/vnd.oai.openapi+json;version=3.0.0",
                         "application/vnd.oai.openapi+yaml;version=3.0.0")
  val draft7JsonSchema = List("application/schema+json;version=draft-07", "application/schema+yaml;version=draft-07")
  val avroSchema = List("application/vnd.apache.avro;version=1.9.0",
                        "application/vnd.apache.avro+json;version=1.9.0",
                        "application/vnd.apache.avro+yaml;version=1.9.0")
  val ramlSchema = List(
    "application/vnd.rai.raml;version=1.0",
    "application/vnd.rai.raml+json;version=1.0",
    "application/vnd.rai.raml+yaml;version=1.0",
    "application/vnd.rai.raml+xml;version=1.0"
  )

  def getSchemaVersion(payload: Payload)(implicit errorHandler: ErrorHandler): JSONSchemaVersion = {
    val value = Option(payload.schemaMediaType).map(f => f.value()).orElse(None)
    getSchemaVersion(value)
  }

  def getSchemaVersion(value: Option[String])(implicit errorHandler: ErrorHandler): JSONSchemaVersion =
    value match {
      case Some(format) if oas30Schema.contains(format) => OAS30SchemaVersion("schema")(errorHandler)
      case Some(format) if ramlSchema.contains(format)  => RAML10SchemaVersion()
      // async20 schemas are handled with draft 7. Avro schema is not supported
      case _ => JSONSchemaDraft7SchemaVersion
    }
}

object AsyncApiTypeParser {
  def apply(entry: YMapEntry, adopt: Shape => Unit, version: JSONSchemaVersion)(
      implicit ctx: OasLikeWebApiContext): AsyncApiTypeParser =
    new AsyncApiTypeParser(Left(entry), entry.key.as[String], entry.value.as[YMap], adopt, version)
}

class AsyncApiTypeParser(entryOrNode: Either[YMapEntry, YNode],
                         name: String,
                         map: YMap,
                         adopt: Shape => Unit,
                         version: JSONSchemaVersion)(implicit val ctx: WebApiContext) {

  def parse(): Option[Shape] = version match {
    case RAML10SchemaVersion() =>
      Raml10TypeParser(entryOrNode, name, adopt, isAnnotation = false, AnyDefaultType)(toRaml(ctx)).parse()
    case _ => new OasTypeParser(entryOrNode, name, map, adopt, version)(toOas(ctx)).parse()
  }
}
