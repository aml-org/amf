package amf.model

import amf.domain.Annotations

/**
  * Created by pedro.colunga on 8/15/17.
  */
case class AmfScalar(value: Any, annotations: Annotations = new Annotations()) extends AmfElement {

  /** String value. */
  def asStr(): String = value.asInstanceOf[String]

  /** Boolean value. */
  def asBool(): Boolean = value.asInstanceOf[Boolean]

  override def toString: String = value.toString
}
