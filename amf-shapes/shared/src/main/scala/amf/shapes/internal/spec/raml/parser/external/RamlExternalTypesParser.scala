package amf.shapes.internal.spec.raml.parser.external

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.YNodeLikeOps
import amf.shapes.client.scala.model.domain.SchemaShape
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps
import amf.shapes.internal.spec.raml.parser.{ExampleParser, RamlTypeEntryParser, RamlTypeSyntax}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidExternalTypeType
import org.yaml.model.YNode.MutRef
import org.yaml.model._

case class ValueAndOrigin(
    text: String,
    valueAST: YNode,
    originalUrlText: Option[String],
    errorShape: Option[AnyShape] = None
)

trait RamlExternalTypesParser
    extends QuickFieldParserOps
    with ExampleParser
    with RamlTypeSyntax
    with RamlTypeEntryParser {

  val value: YNode
  val externalType: String
  val shapeCtx: ShapeParserContext

  def parseValue(origin: ValueAndOrigin): AnyShape

  def parse(): AnyShape = {
    val origin = buildTextAndOrigin()
    origin.errorShape match {
      case Some(shape) => shape
      case _           => parseValue(origin)
    }
  }

  protected def getOrigin(node: YNode): Option[String] = (node, shapeCtx) match {
    case (ref: MutRef, _)             => Some(ref.origValue.toString)
    case (_, wac: ShapeParserContext) => wac.nodeRefIds.get(node)
    case _                            => None
  }

  protected def buildTextAndOrigin(): ValueAndOrigin = {
    value.tagType match {
      case YType.Map =>
        val map = value.as[YMap]
        nestedTypeOrSchema(map) match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            ValueAndOrigin(typeEntry.value.as[YScalar].text, typeEntry.value, getOrigin(typeEntry.value))
          case _ =>
            failSchemaExpressionParser
        }
      case YType.Seq =>
        failSchemaExpressionParser
      case _ =>
        ValueAndOrigin(value.as[YScalar].text, value, getOrigin(value))
    }
  }

  private def failSchemaExpressionParser = {
    val shape = SchemaShape()
    shapeCtx.eh.violation(
      InvalidExternalTypeType,
      shape,
      s"Cannot parse $externalType Schema expression out of a non string value",
      value.location
    )
    ValueAndOrigin("", value, None, Some(shape))
  }
}
