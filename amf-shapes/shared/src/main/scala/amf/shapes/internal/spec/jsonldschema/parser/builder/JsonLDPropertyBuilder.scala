package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.shapes.client.scala.model.domain.SemanticContext
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath
import org.mulesoft.common.client.lexical.SourceLocation

case class JsonLDPropertyBuilder(
    term: String,
    key: String,
    father: Option[String],
    element: JsonLDElementBuilder,
    path: JsonPath,
    location: SourceLocation
) {
  def hasTermWithDefaultBase = term.startsWith(SemanticContext.baseIri)

  override def equals(obj: Any): Boolean = obj match {
    case builder: JsonLDPropertyBuilder => builder.key == key && builder.term == term
  }
}
