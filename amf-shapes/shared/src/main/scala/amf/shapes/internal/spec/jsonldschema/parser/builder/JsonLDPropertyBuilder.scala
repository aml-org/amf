package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.SemanticContext
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath
import org.mulesoft.common.client.lexical.SourceLocation

case class JsonLDPropertyBuilder(
    keyTerm: String,
    key: String,
    father: Option[String],
    element: JsonLDElementBuilder,
    path: JsonPath,
    annotation: Annotations
) {
  def hasTermWithDefaultBase: Boolean = term.startsWith(SemanticContext.baseIri)
  def term: String                    = element.getOverriddenTerm.getOrElse(keyTerm)
  override def equals(obj: Any): Boolean = obj match {
    case builder: JsonLDPropertyBuilder => builder.key == key && builder.term == term
  }
}
