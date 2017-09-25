package amf.model

import amf.domain.Annotations

/**
  * Amf Scalar
  */
case class AmfScalar(value: Any, annotations: Annotations = new Annotations()) extends AmfElement {
  override def toString: String = {
    Option(value) match {
      case Some(v) => v.toString
      case None    => ""
    }
  }
}
