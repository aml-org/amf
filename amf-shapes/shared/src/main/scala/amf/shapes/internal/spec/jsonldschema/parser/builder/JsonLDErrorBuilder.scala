package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.internal.metamodel.Type
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDElement, JsonLDError}
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath
import org.mulesoft.common.client.lexical.{PositionRange, SourceLocation}

class JsonLDErrorBuilder(annotation: Annotations, path: JsonPath) extends JsonLDElementBuilder(annotation, path) {
  override type THIS = this.type

  override def build(ctxBuilder: EntityContextBuilder): (JsonLDElement, Type) = (JsonLDError(), Type.Null)

  override def canEquals(other: Any): Boolean = false
}

object JsonLDErrorBuilder {
  def apply(annotation: Annotations, path: JsonPath) = new JsonLDErrorBuilder(annotation, path)
}
