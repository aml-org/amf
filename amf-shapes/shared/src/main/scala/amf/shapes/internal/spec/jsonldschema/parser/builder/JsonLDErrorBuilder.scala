package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.internal.metamodel.Type
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDElement, JsonLDError}
import org.mulesoft.common.client.lexical.SourceLocation

class JsonLDErrorBuilder extends JsonLDElementBuilder(SourceLocation.Unknown) {
  override type THIS = this.type

  override def build(ctxBuilder: EntityContextBuilder): (JsonLDElement, Type) = (JsonLDError(), Type.Null)

  override def canEquals(other: Any): Boolean = false
}

object JsonLDErrorBuilder {
  def apply() = new JsonLDErrorBuilder()
}