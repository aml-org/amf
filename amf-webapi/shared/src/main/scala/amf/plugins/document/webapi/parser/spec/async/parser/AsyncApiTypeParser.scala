package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Declarations, SearchScope}
import amf.plugins.document.webapi.contexts.{SpecEmitterContext, WebApiContext}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
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
import amf.plugins.document.webapi.parser.spec.{RamlWebApiDeclarations, WebApiDeclarations, toOas, toRaml}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations.InvalidFragmentType

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

case class AsyncApiTypeParser(entry: YMapEntry, adopt: Shape => Unit, version: JSONSchemaVersion)(
    implicit val ctx: OasLikeWebApiContext) {

  def parse(): Option[Shape] = version match {
    case RAML10SchemaVersion() => CustomRamlReferenceParser(entry.value, adopt).parse()
    case _                     => OasTypeParser(entry, adopt, version).parse()
  }
}

case class CustomRamlReferenceParser(node: YNode, adopt: Shape => Unit)(implicit val ctx: OasLikeWebApiContext) {

  def parse(): Option[Shape] =
    ctx.link(node) match {
      case Left(refValue) => handleRef(refValue)
      case Right(node)    => parseRamlType(node)
    }

  private def parseRamlType(node: YNode): Option[Shape] =
    Raml10TypeParser(node, "schema", adopt, AnyDefaultType)(toRaml(ctx)).parse()

  private def handleRef(refValue: String): Option[Shape] = {
    val link = dataTypeFragmentRef(refValue)
      .orElse(typeDefinedInLibraryRef(refValue))
      .orElse(externalFragmentRef(refValue))

    if (link.isEmpty)
      ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $refValue", node)
    link
  }

  private def dataTypeFragmentRef(refValue: String): Option[Shape] = {
    val result = ctx.declarations.findType(refValue, SearchScope.Fragments)
    result.foreach(linkAndAdopt(_, refValue))
    result
  }

  private def typeDefinedInLibraryRef(refValue: String): Option[Shape] = {
    val values = refValue.split("#/types/").toList
    values match {
      case Seq(libUrl, typeName) =>
        val library = ctx.declarations.libraries.get(libUrl).collect { case d: WebApiDeclarations => d }
        val shape   = library.flatMap(_.shapes.get(typeName))
        shape.map(linkAndAdopt(_, refValue))
      case _ => None
    }
  }

  private def externalFragmentRef(refValue: String): Option[Shape] = {
    ctx.obtainRemoteYNode(refValue).flatMap { node =>
      parseRamlType(node)
    }
  }

  private def linkAndAdopt(s: Shape, label: String): Shape = {
    val link = s.link(label, Annotations(node)).asInstanceOf[Shape]
    adopt(link)
    link
  }
}
