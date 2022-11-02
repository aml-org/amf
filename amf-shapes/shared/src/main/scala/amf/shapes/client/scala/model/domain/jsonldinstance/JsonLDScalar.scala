package amf.shapes.client.scala.model.domain.jsonldinstance

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.Annotations

class JsonLDScalar(override val value: Any, val dataType: String) extends AmfScalar(value) with JsonLDElement {

  /** Set of annotations for element. */
  override val annotations: Annotations = Annotations.virtual()

}
