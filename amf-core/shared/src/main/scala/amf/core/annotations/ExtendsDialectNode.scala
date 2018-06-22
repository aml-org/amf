package amf.core.annotations

import amf.core.model.domain.SerializableAnnotation

case class ExtendsDialectNode(val extendedNode: String) extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "extendsNode"

  /** Value as string. */
  override val value: String = extendedNode

}

