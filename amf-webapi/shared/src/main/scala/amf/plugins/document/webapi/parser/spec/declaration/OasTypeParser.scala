package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.model.domain._
import amf.core.parser.{Annotations, _}
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.SchemaPosition.Schema
import amf.plugins.document.webapi.parser.spec.declaration.types.TypeDetector.LinkCriteria
import amf.plugins.document.webapi.parser.spec.oas.OasSpecParser
import amf.plugins.document.webapi.parser.spec.oas.parser.types.InlineOasTypeParser
import amf.plugins.domain.shapes.models._
import org.yaml.model._

/**
  * OpenAPI Type Parser.
  */
/*
 * TODO: Refactor. We should have a proper JsonSchema parser and an OAS that overrides certain parts of it.
 *  Extract different shape parsers. Possibly implement a Chain of Responsibility for each key in type map?
 */
object OasTypeParser {

  def apply(entry: YMapEntry, adopt: Shape => Unit)(implicit ctx: OasLikeWebApiContext): OasTypeParser =
    new OasTypeParser(
      YMapEntryLike(entry),
      key(entry),
      entry.value.as[YMap],
      adopt,
      getSchemaVersion(ctx)
    )

  def apply(entry: YMapEntry, adopt: Shape => Unit, version: SchemaVersion)(
    implicit ctx: OasLikeWebApiContext): OasTypeParser =
    new OasTypeParser(YMapEntryLike(entry), entry.key.as[String], entry.value.as[YMap], adopt, version)

  def apply(node: YMapEntryLike, name: String, adopt: Shape => Unit, version: SchemaVersion)(
    implicit ctx: OasLikeWebApiContext): OasTypeParser =
    new OasTypeParser(node, name, node.asMap, adopt, version)

  def buildDeclarationParser(entry: YMapEntry, adopt: Shape => Unit)(
    implicit ctx: OasLikeWebApiContext): OasTypeParser =
    new OasTypeParser(
      YMapEntryLike(entry),
      key(entry),
      entry.value.as[YMap],
      adopt,
      getSchemaVersion(ctx),
      true
    )

  private def key(entry: YMapEntry) = entry.key.as[YScalar].text

  private def getSchemaVersion(ctx: OasLikeWebApiContext) = {
    if (ctx.vendor == Vendor.OAS30) OAS30SchemaVersion(Schema)
    else if (ctx.vendor == Vendor.ASYNC20) JSONSchemaDraft7SchemaVersion
    else OAS20SchemaVersion(Schema)
  }
}

case class OasTypeParser(entryOrNode: YMapEntryLike,
                         name: String,
                         map: YMap,
                         adopt: Shape => Unit,
                         version: SchemaVersion,
                         isDeclaration: Boolean = false)(implicit val ctx: OasLikeWebApiContext)
    extends OasSpecParser {

  private val ast: YPart = entryOrNode.ast

  private val nameAnnotations: Annotations = entryOrNode.key.map(n => Annotations(n)).getOrElse(Annotations())

  def parse(): Option[AnyShape] = LinkCriteria.detect(map).flatMap { _ =>
      new OasRefParser(map, name, nameAnnotations, ast, adopt, version).parse()
    }.orElse(
    InlineOasTypeParser(entryOrNode, name, map, adopt, version, isDeclaration).parse()
  )
}
