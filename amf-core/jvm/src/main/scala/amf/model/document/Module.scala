package amf.model.document

import amf.core.model.document

/**
  * JS Module model class
  */
case class Module(private[amf] val model: document.Module) extends BaseUnit with DeclaresModel {

  override private[amf] val element = model

  def this() = this(document.Module())

}
