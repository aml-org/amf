package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.model.domain._
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YScalarYRead
import amf.core.internal.parser.domain.{Annotations, _}
import amf.core.internal.remote.Spec
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.SchemaPosition.Schema
import amf.shapes.internal.spec.common.parser.{QuickFieldParserOps, YMapEntryLike}
import amf.shapes.internal.spec.common._
import amf.shapes.internal.spec.oas.parser
import amf.shapes.internal.spec.oas.parser.TypeDetector.LinkCriteria
import org.yaml.model.{IllegalTypeHandler, YMap, YMapEntry, YPart, YScalar}

/**
  * OpenAPI Type Parser.
  */
/*
 * TODO: Refactor. We should have a proper JsonSchema parser and an OAS that overrides certain parts of it.
 *  Extract different shape parsers. Possibly implement a Chain of Responsibility for each key in type map?
 */
object OasTypeParser {

  def apply(entry: YMapEntry, adopt: Shape => Unit)(implicit ctx: ShapeParserContext): OasTypeParser =
    new OasTypeParser(
      YMapEntryLike(entry),
      key(entry),
      entry.value.as[YMap],
      adopt,
      getSchemaVersion(ctx)
    )

  def apply(entry: YMapEntry, adopt: Shape => Unit, version: SchemaVersion)(
      implicit ctx: ShapeParserContext): OasTypeParser =
    new OasTypeParser(YMapEntryLike(entry), entry.key.as[String], entry.value.as[YMap], adopt, version)

  def apply(node: YMapEntryLike, name: String, adopt: Shape => Unit, version: SchemaVersion)(
      implicit ctx: ShapeParserContext): OasTypeParser =
    new OasTypeParser(node, name, node.asMap, adopt, version)

  def buildDeclarationParser(entry: YMapEntry, adopt: Shape => Unit)(implicit ctx: ShapeParserContext): OasTypeParser =
    new OasTypeParser(
      YMapEntryLike(entry),
      key(entry),
      entry.value.as[YMap],
      adopt,
      getSchemaVersion(ctx),
      true
    )

  private def key(entry: YMapEntry)(implicit errorHandler: IllegalTypeHandler) = entry.key.as[YScalar].text

  private def getSchemaVersion(ctx: ShapeParserContext) = {
    if (ctx.vendor == Spec.OAS30) OAS30SchemaVersion(Schema)
    else if (ctx.vendor == Spec.ASYNC20) JSONSchemaDraft7SchemaVersion
    else OAS20SchemaVersion(Schema)
  }
}

case class OasTypeParser(entryOrNode: YMapEntryLike,
                         name: String,
                         map: YMap,
                         adopt: Shape => Unit,
                         version: SchemaVersion,
                         isDeclaration: Boolean = false)(implicit val ctx: ShapeParserContext)
    extends QuickFieldParserOps {

  def parse(): Option[AnyShape] = {
    if (version.isBiggerThanOrEqualTo(JSONSchemaDraft201909SchemaVersion)) {
      Draft2019TypeParser(entryOrNode, name, map, adopt, version, isDeclaration).parse
    } else {
      Draft4TypeParser(entryOrNode, name, map, adopt, version, isDeclaration).parse
    }
  }
}

case class Draft2019TypeParser(entryOrNode: YMapEntryLike,
                               name: String,
                               map: YMap,
                               adopt: Shape => Unit,
                               version: SchemaVersion,
                               isDeclaration: Boolean = false)(implicit val ctx: ShapeParserContext) {

  private val ast: YPart                   = entryOrNode.ast
  private val nameAnnotations: Annotations = entryOrNode.key.map(n => Annotations(n)).getOrElse(Annotations())

  def parse: Option[AnyShape] = {
    val hasLink = LinkCriteria.detect(map).isDefined
    if (!hasLink) parser.InlineOasTypeParser(entryOrNode, name, map, adopt, version, isDeclaration).parse()
    else if (hasLink && isSingleEntryMap(map))
      new OasRefParser(map, name, nameAnnotations, ast, adopt, version).parse()
    else {
      val reffedShape = new OasRefParser(map, name, nameAnnotations, ast, adopt, version).parse()
      val restOfShape = parser.InlineOasTypeParser(entryOrNode, name, map, adopt, version, isDeclaration).parse()
      mergeLinkWithShapeAllOf(reffedShape, restOfShape)
    }
  }

  private def isSingleEntryMap(map: YMap) = map.entries.size == 1

  private def mergeLinkWithShapeAllOf(reffedShape: Option[AnyShape], restOfShape: Option[AnyShape]): Option[AnyShape] = {
    (reffedShape, restOfShape) match {
      case (Some(reffed), Some(rest)) => Some(mergeLinkWithShapeAllOf(reffed, rest))
      case (_, Some(rest))            => Some(rest)
      case (Some(reffed), _)          => Some(reffed)
      case _                          => None
    }
  }

  private def mergeLinkWithShapeAllOf(linkShape: AnyShape, container: AnyShape): AnyShape = {
    container.fields.entry(ShapeModel.And) match {
      case Some(entry @ FieldEntry(_, value)) =>
        container.set(
          ShapeModel.And,
          AmfArray(entry.arrayValues[Shape] :+ linkShape, entry.element.annotations),
          value.annotations
        )
      case None =>
        container.set(ShapeModel.And, AmfArray(Seq(linkShape), Annotations.virtual()), Annotations.inferred())
    }
    container
  }
}

case class Draft4TypeParser(entryOrNode: YMapEntryLike,
                            name: String,
                            map: YMap,
                            adopt: Shape => Unit,
                            version: SchemaVersion,
                            isDeclaration: Boolean = false)(implicit val ctx: ShapeParserContext) {

  private val ast: YPart                   = entryOrNode.ast
  private val nameAnnotations: Annotations = entryOrNode.key.map(n => Annotations(n)).getOrElse(Annotations())

  def parse: Option[AnyShape] = {
    val hasLink = LinkCriteria.detect(map).isDefined
    if (!hasLink) parser.InlineOasTypeParser(entryOrNode, name, map, adopt, version, isDeclaration).parse()
    else new OasRefParser(map, name, nameAnnotations, ast, adopt, version).parse()
  }
}
