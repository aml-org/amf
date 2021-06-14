package amf.plugins.document.apicontract.parser.spec.declaration.external.raml

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.YNodeLikeOps
import amf.plugins.document.apicontract.parser.ShapeParserContext
import amf.plugins.document.apicontract.parser.spec.common.QuickFieldParserOps
import amf.plugins.document.apicontract.parser.spec.declaration.{ExampleParser, RamlTypeEntryParser, RamlTypeSyntax}
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.validations.ShapeParserSideValidations.InvalidExternalTypeType
import org.yaml.model.YNode.MutRef
import org.yaml.model._

trait RamlExternalTypesParser
    extends QuickFieldParserOps
    with ExampleParser
    with RamlTypeSyntax
    with RamlTypeEntryParser {

  val value: YNode
  val adopt: Shape => Unit
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

  protected case class ValueAndOrigin(text: String,
                                      valueAST: YNode,
                                      originalUrlText: Option[String],
                                      errorShape: Option[AnyShape] = None)

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
    adopt(shape)
    shapeCtx.eh.violation(InvalidExternalTypeType,
                          shape.id,
                          s"Cannot parse $externalType Schema expression out of a non string value",
                          value)
    ValueAndOrigin("", value, None, Some(shape))
  }
}
