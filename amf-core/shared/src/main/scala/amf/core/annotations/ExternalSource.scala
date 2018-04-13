package amf.core.annotations

import amf.core.model.domain.SerializableAnnotation

case class ExternalSource(oriId: String, oriLabel: String) extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "external-source"

  /** Value as string. */
  override val value: String = oriLabel + "->" + oriId
}
