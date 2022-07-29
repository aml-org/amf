package amf.shapes.client.scala.model.document

import amf.aml.client.scala.model.document.DialectInstance
import amf.core.internal.parser.domain.{Annotations, Fields}

class JsonLDInstanceDocument(fields: Fields, annotations: Annotations) extends DialectInstance(fields, annotations) {}

object JsonLDInstanceDocument {
  def apply() = new JsonLDInstanceDocument(Fields(), Annotations())
}
