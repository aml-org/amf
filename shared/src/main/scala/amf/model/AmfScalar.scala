package amf.model

import amf.domain.Annotations

/**
  * Amf Scalar
  */
case class AmfScalar(value: Any, annotations: Annotations = new Annotations()) extends AmfElement {
  override def toString: String = {
    if (value==null){
      return null;
    }
    value.toString
  }
}
