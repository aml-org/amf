package amf.core.model.domain

import amf.core.parser.Annotations

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

  def toBool: Boolean = {
    Option(value) match {
      case Some(v: String) => v.toBoolean
      case Some(true)      => true
      case Some(false)     => false
      case _               => throw new Exception("Cannot transform scalar value into Boolean")
    }
  }

  def toNumber: Number = {
    Option(value) match {
      case Some(v: String) if v.indexOf(".") > -1 => v.toFloat
      case Some(v: String)                        => v.toInt
      case Some(v)                                => v.asInstanceOf[Number]
      case None                                   => throw new Exception("Cannot transform null value into Number")
    }
  }
}
