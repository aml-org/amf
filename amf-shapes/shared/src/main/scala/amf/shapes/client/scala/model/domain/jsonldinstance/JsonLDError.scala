package amf.shapes.client.scala.model.domain.jsonldinstance

import amf.core.client.scala.model.domain.{AmfElement, AmfObject}
import amf.core.internal.parser.domain.Annotations

import scala.collection.mutable

case class JsonLDError() extends JsonLDElement {

  /** Set of annotations for element. */
  override val annotations: Annotations = Annotations.empty

  override def cloneElement(branch: mutable.Map[AmfObject, AmfObject]): AmfElement = JsonLDError()
}
