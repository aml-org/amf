package amf.plugins.document.webapi.parser.spec.declaration.external.raml

import amf.core.model.domain.Shape
import amf.core.parser._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.{ExampleParser, RamlTypeSyntax}
import amf.plugins.document.webapi.parser.spec.raml.RamlSpecParser
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.validations.ParserSideValidations._
import org.yaml.model.YNode.MutRef
import org.yaml.model._

trait RamlExternalTypesParser extends RamlSpecParser with ExampleParser with RamlTypeSyntax {

  val value: YNode
  val adopt: Shape => Unit
  val externalType: String
  def parseValue(origin: ValueAndOrigin): AnyShape

  def parse(): AnyShape = {
    val origin = buildTextAndOrigin()
    origin.errorShape match {
      case Some(shape) => shape
      case _           => parseValue(origin)
    }
  }

  protected def getOrigin(node: YNode): Option[String] = (node, ctx) match {
    case (ref: MutRef, _)        => Some(ref.origValue.toString)
    case (_, wac: WebApiContext) => wac.nodeRefIds.get(node)
    case _                       => None
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
    ctx.eh.violation(InvalidExternalTypeType,
                     shape.id,
                     s"Cannot parse $externalType Schema expression out of a non string value",
                     value)
    ValueAndOrigin("", value, None, Some(shape))
  }
}
