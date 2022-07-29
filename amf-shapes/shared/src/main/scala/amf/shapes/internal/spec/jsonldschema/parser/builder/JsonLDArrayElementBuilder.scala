package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.shapes.internal.spec.jsonldschema.parser.{JsonLDParserContext, JsonLDScalarElementBuilder}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleItemNodes
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.ShapeTransformationContext
import org.mulesoft.common.client.lexical.SourceLocation

import scala.collection.mutable

class JsonLDArrayElementBuilder(location: SourceLocation) extends JsonLDElementBuilder(location) {
  private var items: IndexedSeq[JsonLDElementBuilder] = IndexedSeq.empty

  override def merge(
      other: JsonLDArrayElementBuilder.this.type
  )(implicit ctx: JsonLDParserContext): JsonLDArrayElementBuilder.this.type = {
    super.merge(other)
    this.withItems(mergeItems(other.items))
    this
  }

  def mergeItems(
      others: IndexedSeq[JsonLDElementBuilder]
  )(implicit ctx: JsonLDParserContext): IndexedSeq[JsonLDElementBuilder] = {
    if (others.length != items.length)
      others // never should happen this, because I return .empty() if not possible to parse
    else {
      items
        .zip(others)
        .map({
          case (current, other) if (current.canEquals(other)) => mergeItem(current, other)
          case (_, other) =>
            ctx.violation(IncompatibleItemNodes, "", IncompatibleItemNodes.message, other.location)
            other
        })
    }
  }
  def withItems(items: IndexedSeq[JsonLDElementBuilder]): this.type = {
    this.items = items
    this
  }

  private def mergeItem(current: JsonLDElementBuilder, other: JsonLDElementBuilder)(implicit
      ctx: JsonLDParserContext
  ): JsonLDElementBuilder = {
    current match {
      case obj: JsonLDObjectElementBuilder    => obj.merge(other.asInstanceOf[JsonLDObjectElementBuilder])
      case arr: JsonLDArrayElementBuilder     => arr.merge(other.asInstanceOf[JsonLDArrayElementBuilder])
      case scalar: JsonLDScalarElementBuilder => scalar.merge(other.asInstanceOf[JsonLDScalarElementBuilder])
    }
  }
}

object JsonLDArrayElementBuilder {
  def empty() = new JsonLDArrayElementBuilder(SourceLocation.Unknown)
}
