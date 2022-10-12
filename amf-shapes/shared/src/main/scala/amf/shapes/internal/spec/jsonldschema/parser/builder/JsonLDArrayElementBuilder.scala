package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.internal.metamodel.Type
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDArray, JsonLDElement, JsonLDElementModel}
import amf.shapes.internal.spec.jsonldschema.parser.JsonLDParserContext
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleItemNodes
import org.mulesoft.common.client.lexical.SourceLocation

class JsonLDArrayElementBuilder(location: SourceLocation) extends JsonLDElementBuilder(location) {
  private var items: Seq[JsonLDElementBuilder] = Seq.empty
  override type THIS = JsonLDArrayElementBuilder

  override def merge(
      other: JsonLDArrayElementBuilder
  )(implicit ctx: JsonLDParserContext): JsonLDArrayElementBuilder = {
    super.merge(other)
    this.withItems(mergeItems(other.items))
    this
  }

  def mergeItems(
      others: Seq[JsonLDElementBuilder]
  )(implicit ctx: JsonLDParserContext): Seq[JsonLDElementBuilder] = {
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
  def withItems(items: Seq[JsonLDElementBuilder]): this.type = {
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

  override def build(ctxBuilder: EntityContextBuilder): (JsonLDElement, Type) = {
    val array = new JsonLDArray()

    items.foreach { i => array += i.build(ctxBuilder)._1 }
    (array, Type.Array(JsonLDElementModel))
  }

  override def canEquals(other: Any): Boolean = other.isInstanceOf[JsonLDArrayElementBuilder]
}
