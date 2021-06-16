package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.Payload
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.apicontract.internal.spec.spec.toRaml
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{AmfScalar, Shape}
import amf.core.internal.annotations.DefinedByVendor
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.core.internal.remote.Raml10
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.spec.common._
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.spec.raml.parser.{AnyDefaultType, Raml10TypeParser, TypeInfo}
import org.yaml.model.YMapEntry

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
    "application/raml+yaml;version=1.0"
  )

  def getSchemaVersion(payload: Payload)(implicit errorHandler: AMFErrorHandler): SchemaVersion = {
    val value = Option(payload.schemaMediaType).map(f => f.value()).orElse(None)
    getSchemaVersion(value)
  }

  def getSchemaVersion(value: Option[String])(implicit errorHandler: AMFErrorHandler): SchemaVersion =
    value match {
      case Some(format) if oas30Schema.contains(format) => OAS30SchemaVersion(SchemaPosition.Schema)
      case Some(format) if ramlSchema.contains(format)  => RAML10SchemaVersion
      // async20 schemas are handled with draft 7. Avro schema is not supported
      case _ => JSONSchemaDraft7SchemaVersion
    }
}

case class AsyncApiTypeParser(entry: YMapEntry, adopt: Shape => Unit, version: SchemaVersion)(
    implicit val ctx: OasLikeWebApiContext) {

  def parse(): Option[Shape] = version match {
    case RAML10SchemaVersion => CustomRamlReferenceParser(YMapEntryLike(entry), adopt).parse()
    case _                   => OasTypeParser(entry, adopt, version)(WebApiShapeParserContextAdapter(ctx)).parse()
  }
}

case class CustomRamlReferenceParser(entry: YMapEntryLike, adopt: Shape => Unit)(
    implicit val ctx: OasLikeWebApiContext) {

  def parse(): Option[Shape] = {
    val shape = ctx.link(entry.value) match {
      case Left(refValue) => handleRef(refValue)
      case Right(_)       => parseRamlType(entry)
    }
    shape.foreach(_.annotations += DefinedByVendor(Raml10))
    shape
  }

  private def parseRamlType(entry: YMapEntryLike): Option[Shape] = {
    val context = toRaml(ctx)
    context.declarations.shapes = Map.empty
    val result =
      Raml10TypeParser(entry, "schema", adopt, TypeInfo(), AnyDefaultType)(WebApiShapeParserContextAdapter(context))
        .parse()
    context.futureDeclarations.resolve()
    result
  }

  private def handleRef(refValue: String): Option[Shape] = {
    val link = dataTypeFragmentRef(refValue)
      .orElse(typeDefinedInLibraryRef(refValue))
      .orElse(externalFragmentRef(refValue))

    if (link.isEmpty)
      ctx.eh.violation(CoreValidations.UnresolvedReference,
                       "",
                       s"Cannot find link reference $refValue",
                       entry.annotations)
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
      parseRamlType(YMapEntryLike(node))
    }
  }

  private def linkAndAdopt(s: Shape, label: String): Shape = {
    val link = s.link(AmfScalar(label), entry.annotations, Annotations.synthesized()).asInstanceOf[Shape]
    adopt(link)
    link
  }
}
