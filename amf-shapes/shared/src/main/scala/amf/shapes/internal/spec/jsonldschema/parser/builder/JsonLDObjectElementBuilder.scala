package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.shapes.internal.spec.jsonldschema.parser.JsonLDParserContext
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleNodes
import org.mulesoft.common.client.lexical.SourceLocation

import scala.collection.mutable

class JsonLDObjectElementBuilder(location: SourceLocation) extends JsonLDElementBuilder(location) {
  type KEY = String
  val properties: mutable.Map[KEY, JsonLDPropertyBuilder] = mutable.Map()

  def +(property: JsonLDPropertyBuilder) = {

    // TODO: handle terms colitions.
    properties += property.key -> property
  }

  override def merge(
      other: JsonLDObjectElementBuilder.this.type
  )(implicit ctx: JsonLDParserContext): JsonLDObjectElementBuilder.this.type = {
    super.merge(other)

    other.properties.foreach { t =>
      if (properties.contains(t._2.key)) mergeAndReplaceProperty(properties(t._2.key), t._2)
      else this + t._2
    }
    this
  }

  def mergeAndReplaceProperty(current: JsonLDPropertyBuilder, other: JsonLDPropertyBuilder)(implicit
      ctx: JsonLDParserContext
  ): Any = {
    val merged = current.element match {
      case obj: JsonLDObjectElementBuilder if other.element.isInstanceOf[JsonLDObjectElementBuilder] =>
        obj.merge(other.element.asInstanceOf[JsonLDObjectElementBuilder])
      case array: JsonLDArrayElementBuilder if other.element.isInstanceOf[JsonLDArrayElementBuilder] =>
        array.merge(other.element.asInstanceOf[JsonLDArrayElementBuilder])
      case scalar: JsonLDScalarElementBuilder if other.element.isInstanceOf[JsonLDScalarElementBuilder] =>
        scalar.merge(other.element.asInstanceOf[JsonLDScalarElementBuilder])
      case _ =>
        ctx.violation(IncompatibleNodes, "", IncompatibleNodes.message, current.location)
        other
    }
    properties.remove(current.key)
    this + merged
  }
}

object JsonLDObjectElementBuilder {
  def empty() = new JsonLDObjectElementBuilder(SourceLocation.Unknown)
}

case class JsonLDPropertyBuilder(
    term: String,
    key: String,
    father: Option[String],
    element: JsonLDElementBuilder,
    location: SourceLocation
)
